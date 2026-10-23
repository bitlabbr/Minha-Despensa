/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.data

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.MovementType
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import com.bitlabbr.minhadespensa.data.remote.InvoiceRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceItemDto
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceResponseDto
import com.bitlabbr.minhadespensa.data.repository.RoomInvoiceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoomInvoiceRepositoryTest : BaseTest() {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomInvoiceRepository
    private val logger = ConsoleLogger("Test")

    private val fakeRemoteDataSource = object : InvoiceRemoteDataSource {
        var returnDto: InvoiceResponseDto = InvoiceResponseDto(
            storeName = "Mercadinho do Bairro",
            items = emptyList()
        )

        override suspend fun parseInvoice(qrCodeContent: String): InvoiceResponseDto {
            return returnDto
        }
    }

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        repository = RoomInvoiceRepository(
            remoteDataSource = fakeRemoteDataSource,
            db = db,
            logger = logger,
        )
    }

    @Test
    fun `parseInvoiceFromQrCode should delegate to remote data source and return domain Invoice`() = runTest {
        fakeRemoteDataSource.returnDto = InvoiceResponseDto(
            accessKey = "2624109999",
            storeName = "Atacadão",
            items = listOf(
                InvoiceItemDto(
                    ean = "7891111222233",
                    name = "Café Melitta 500g",
                    quantity = 2.0,
                    unit = "PCT",
                    unitPriceInCents = 1899L,
                    totalPriceInCents = 3798L,
                )
            )
        )

        val result = repository.parseInvoiceFromQrCode("http://nfce.sefaz.pe.gov.br")
        assertTrue(result.isSuccess)
        val invoice = result.getOrThrow()
        assertEquals("Atacadão", invoice.storeName)
        assertEquals(1, invoice.items.size)
        assertEquals("Café Melitta 500g", invoice.items[0].name)
    }

    @Test
    fun `importInvoiceToPantry should insert new catalog product pantry item price entry and movement`() = runTest {
        val items = listOf(
            InvoiceItem(
                id = "item-1",
                ean = "7891234567890",
                name = "Açúcar Cristal 1kg",
                quantity = 3.0,
                measureUnit = MeasureUnit.PACKAGE,
                unitPriceInCents = 450L,
                totalPriceInCents = 1350L,
            )
        )

        val result = repository.importInvoiceToPantry("Supermercado Central", items)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())

        // 1. Check Catalog
        val product = db.catalogDao().findSnapshotByEan("7891234567890")
        assertNotNull(product)
        assertEquals("Açúcar Cristal 1kg", product.name)
        assertEquals(CoreConstants.Product.DEFAULT_CATEGORY, product.category)

        // 2. Check Pantry
        val pantryItems = db.pantryDao().getPantryItemsByProductId(product.id).first()
        assertEquals(1, pantryItems.size)
        assertEquals(3.0, pantryItems[0].quantity)
        assertEquals(450L, pantryItems[0].unitPriceInCents)

        // 3. Check Price Entry
        val priceEntry = db.priceDao().getLatestPriceForProductId(product.id).first()
        assertNotNull(priceEntry)
        assertEquals(450L, priceEntry.priceInCents)
        assertEquals("Supermercado Central", priceEntry.storeName)

        // 4. Check Pantry Movement
        val movements = db.movementDao().getAllMovements().first()
        assertEquals(1, movements.size)
        assertEquals(MovementType.ENTRY_PURCHASE.name, movements[0].type)
        assertEquals(3.0, movements[0].quantity)
        assertEquals(450L, movements[0].unitPriceInCents)
        assertEquals(1350L, movements[0].totalPriceInCents)
        assertEquals("Nota Fiscal: Supermercado Central", movements[0].storeOrReason)

        // 5. Check Pending Sync Outbox (must enqueue PRODUCT, PANTRY_ITEM, PRICE_ENTRY, MOVEMENT)
        val pending = db.pendingSyncDao().getAllPending(20)
        assertEquals(4, pending.size)
        assertTrue(pending.any { it.entityType == "PRODUCT" && it.entityId == product.id })
        assertTrue(pending.any { it.entityType == "PANTRY_ITEM" && it.entityId == pantryItems[0].id })
        assertTrue(pending.any { it.entityType == "PRICE_ENTRY" && it.entityId == priceEntry.id })
        assertTrue(pending.any { it.entityType == "MOVEMENT" && it.entityId == movements[0].id })
    }

    @Test
    fun `importInvoiceToPantry should reuse existing catalog product when matching EAN exists`() = runTest {
        // Pre-insert product with same EAN
        val existingProduct = CatalogProduct(
            id = "existing-id-123",
            ean = "7899999999999",
            name = "Leite Integral Piracanjuba",
            category = "Laticínios e Ovos",
            updatedAt = 1000L,
        )
        db.catalogDao().insert(existingProduct.toEntity())

        val items = listOf(
            InvoiceItem(
                id = "item-from-invoice",
                ean = "7899999999999",
                name = "LEITE INTEGRAL 1L",
                quantity = 6.0,
                unitPriceInCents = 489L,
            )
        )

        val result = repository.importInvoiceToPantry("Hiper Bompreço", items)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())

        // Verify product was not duplicated
        val allProducts = db.catalogDao().getAllActiveSnapshot()
        assertEquals(1, allProducts.size)
        assertEquals("existing-id-123", allProducts[0].id)
        assertEquals("Laticínios e Ovos", allProducts[0].category)

        // Verify pantry item is linked to existing product ID
        val pantryItems = db.pantryDao().getPantryItemsByProductId("existing-id-123").first()
        assertEquals(1, pantryItems.size)
        assertEquals(6.0, pantryItems[0].quantity)
    }

    @Test
    fun `importInvoiceToPantry with item having no unit price should still insert pantry item and movement`() = runTest {
        val items = listOf(
            InvoiceItem(
                id = "item-no-price",
                ean = null,
                name = "Fruta Maçã Gala",
                quantity = 1.5,
                measureUnit = MeasureUnit.KILOGRAM,
                unitPriceInCents = null,
                totalPriceInCents = null,
            )
        )

        val result = repository.importInvoiceToPantry("Feira Livre", items)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())

        val product = db.catalogDao().findByName("Fruta Maçã Gala")
        assertNotNull(product)

        val pantryItems = db.pantryDao().getPantryItemsByProductId(product.id).first()
        assertEquals(1, pantryItems.size)
        assertEquals(1.5, pantryItems[0].quantity)
        assertEquals(null, pantryItems[0].unitPriceInCents)

        // Price entry should NOT be inserted when price is null
        val priceEntry = db.priceDao().getLatestPriceForProductId(product.id).first()
        assertEquals(null, priceEntry)

        val movements = db.movementDao().getAllMovements().first()
        assertEquals(1, movements.size)
        assertEquals(1.5, movements[0].quantity)
        assertEquals(null, movements[0].unitPriceInCents)
    }

    @Test
    fun `importInvoiceToPantry should group duplicate products into a single pantry item price entry and movement`() = runTest {
        val items = listOf(
            InvoiceItem(
                id = "dup-1",
                ean = "7898123456789",
                name = "PALMEIRON EXTRATO DE TOMATE",
                quantity = 1.0,
                measureUnit = MeasureUnit.UNIT,
                unitPriceInCents = 279L,
                totalPriceInCents = 279L,
            ),
            InvoiceItem(
                id = "dup-2",
                ean = "7898123456789",
                name = "PALMEIRON EXTRATO DE TOMATE",
                quantity = 2.0,
                measureUnit = MeasureUnit.UNIT,
                unitPriceInCents = 279L,
                totalPriceInCents = 558L,
            ),
        )

        val result = repository.importInvoiceToPantry("Estrada de Belém", items)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())

        val allProducts = db.catalogDao().getAllActiveSnapshot()
        assertEquals(1, allProducts.size)

        val pantryItems = db.pantryDao().getPantryItemsByProductId(allProducts[0].id).first()
        assertEquals(1, pantryItems.size)
        assertEquals(3.0, pantryItems[0].quantity)
        assertEquals(279L, pantryItems[0].unitPriceInCents)

        val priceHistory = db.priceDao().getPriceHistoryByProductId(allProducts[0].id).first()
        assertEquals(1, priceHistory.size)
        assertEquals(279L, priceHistory[0].priceInCents)

        val movements = db.movementDao().getAllMovements().first()
        assertEquals(1, movements.size)
        assertEquals(3.0, movements[0].quantity)
        assertEquals(279L, movements[0].unitPriceInCents)
        assertEquals(837L, movements[0].totalPriceInCents)
    }
}

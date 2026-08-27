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

import androidx.room.Transactor
import androidx.room.useWriterConnection
import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomCatalogRepositoryTest : BaseTest() {
    private lateinit var catalogRepository: RoomCatalogRepository
    private lateinit var db: AppDatabase
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        catalogRepository = RoomCatalogRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should SAVE product successfully and GET by ID and EAN `() = runTest {
        val productId = Uuid.random().toString()
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef, null)
        catalogRepository.getProductById(productId).test {
            val product = awaitItem()
            assertProduct(product, productRef)
            cancelAndIgnoreRemainingEvents()
        }
        catalogRepository.getProductByEan(productEan).test {
            val product = awaitItem()
            assertProduct(product, productRef)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product WITH INVALID TIMESTAMP`() = runTest {
        val productId = Uuid.random().toString()
        val now = 92929L
        val productEan = "7891234567890"
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without name as EMPTY`() = runTest {
        val productId = Uuid.random().toString()
        val now = getCurrentTime()
        val productEan = "7891234567890"

        val productRef = CatalogProduct(
            id = productId,
            name = "",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @Test
    fun `should NOT SAVE product without a valid uuid as ID`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productRef = CatalogProduct(
            id = "invalid product id",
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without a valid BRAND`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia".repeat(100),
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without a valid EAN`() = runTest {
        val now = getCurrentTime()
        val productEan = "AAAAAAAAAAAAA"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without a valid netWeight`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia".repeat(100),
            measureUnit = MeasureUnit.LITER,
            netWeight = -1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without a valid name`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "A".repeat(31),
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalArgumentException> {
            catalogRepository.insertProduct(productRef, null)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without DUPLICATED ID`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef, null)

        val now_2 = getCurrentTime()
        val productEan_2 = "7891234567890"
        val productRef_2 = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now_2,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan_2,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef_2, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT SAVE product without DUPLICATED EAN`() = runTest {
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productId = Uuid.random().toString()
        val productRef = CatalogProduct(
            id = productId,
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef, null)

        val now_2 = getCurrentTime()
        val productId_2 = Uuid.random().toString()
        val productRef_2 = CatalogProduct(
            id = productId_2,
            name = "Leite Integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now_2,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )

        assertFailsWith<IllegalStateException> {
            runTest {
                catalogRepository.insertProduct(productRef_2, null)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should SAVE PRODUCT and ITS IMAGE blob atomically`() = runTest {
        val productId = Uuid.random().toString()
        val productEan = "0000000000000"
        val productRef = CatalogProduct(
            id = productId,
            name = "Café",
            brand = "Pilão",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 0.5,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        val fakeImageBlob = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

        catalogRepository.insertProduct(productRef, fakeImageBlob)

        val savedProduct = catalogRepository.getProductById(productId).first()
        assertNotNull(savedProduct)
        val savedMedia = db.productMediaDao().getByProductId(productId)
        assertNotNull(savedMedia)
        assertTrue(
            fakeImageBlob.contentEquals(savedMedia.blob),
            "Saved BLOB should be equals to sanding one"
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should FIND two devices by SEARCH`() = runTest {
        val productId = Uuid.random().toString()
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productRef_1 = CatalogProduct(
            id = productId,
            name = "Leite integral",
            brand = "Betânia",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = now,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef_1, null)

        val productId_2 = Uuid.random().toString()
        val now_2 = getCurrentTime()
        val productEan_2 = "7891234567891"
        val productRef_2 = CatalogProduct(
            id = productId_2,
            name = "Leite integral UAT",
            brand = "Bom Leite",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.5,
            updatedAt = now_2,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan_2,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef_2, null)

        val productId_3 = Uuid.random().toString()
        val now_3 = getCurrentTime()
        val productEan_3 = "7891234567892"
        val productRef_3 = CatalogProduct(
            id = productId_3,
            name = "Café",
            brand = "Santa clara",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 0.5,
            updatedAt = now_3,
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan_3,
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(productRef_3, null)

        catalogRepository.searchProductsByNameOrBrand("leite").test {
            val searchResults = awaitItem()

            assertEquals(2, searchResults.size, "2 products are expected")

            val foundIds = searchResults.map { it.id }
            assertTrue(foundIds.contains(productId), "Product 1 not found")
            assertTrue(foundIds.contains(productId_2), "Product 2 not found")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should UPDATE product BASIC INFO successfully`() = runTest {
        val productId = Uuid.random().toString()
        val initialProduct = CatalogProduct(
            id = productId,
            name = "Produto Original",
            brand = "Marca A",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 0.5,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true,
            ean = "1111111111111",
            thumbnailUrl = null
        )
        catalogRepository.insertProduct(initialProduct, null)

        val updatedProduct = initialProduct.copy(
            name = "Produto Atualizado",
            brand = "Marca B",
            netWeight = 0.75,
            updatedAt = getCurrentTime()
        )
        catalogRepository.updateForProductIfNewer(updatedProduct, null)

        catalogRepository.getProductById(productId).test {
            val result = awaitItem()
            assertEquals("Produto Atualizado", result?.name)
            assertEquals("Marca B", result?.brand)
            assertEquals(0.75, result?.netWeight)
            assertEquals(productId, result?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT trigger CASCADE deletion of media during a simple metadata update`() = runTest {
        val productId = Uuid.random().toString()
        val product = CatalogProduct(
            id = productId,
            name = "Leite",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true
        )

        val imageBytes = byteArrayOf(1, 2, 3)
        catalogRepository.insertProduct(product, imageBytes)

        val updatedProduct = product.copy(name = "Leite Desnatado")
        catalogRepository.updateForProductIfNewer(updatedProduct, null)

        val mediaExists = db.productMediaDao().getByProductId(productId)
        assertTrue(mediaExists != null, "Media should still exist")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should mark product as DELETED but keep it in database`() = runTest {
        val productId = Uuid.random().toString()
        val product = CatalogProduct(
            id = productId,
            name = "Produto para Deletar",
            measureUnit = MeasureUnit.UNIT,
            netWeight = 1.0,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true
        )

        catalogRepository.insertProduct(product, null)

        val deletedProduct = product.copy(isDeleted = true, updatedAt = getCurrentTime() + 1)
        catalogRepository.updateForProductIfNewer(deletedProduct, null)

        val result = catalogRepository.getProductById(productId).first()
        assertNotNull(result)
        assertEquals(result.isDeleted, true, "Product should be marked as deleted")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should return all ACTIVE items on db`() = runTest {
        val productId = Uuid.random().toString()
        val product = CatalogProduct(
            id = productId,
            name = "Produto para Deletar",
            measureUnit = MeasureUnit.UNIT,
            netWeight = 1.0,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true
        )

        catalogRepository.insertProduct(product, null)

        val otherItem = product.copy(
            id = Uuid.random().toString(),
            name = "other product",
            updatedAt = getCurrentTime(),
        )
        catalogRepository.insertProduct(otherItem, null)

        val otherItem2 = product.copy(
            id = Uuid.random().toString(),
            name = "other product again",
            updatedAt = getCurrentTime(),
        )
        catalogRepository.insertProduct(otherItem2, null)

        val deletedProduct = product.copy(isDeleted = true, updatedAt = getCurrentTime() + 1)
        catalogRepository.updateForProductIfNewer(deletedProduct, null)

        val result = catalogRepository.getAllActives().first()
        assertNotNull(result)
        assertEquals(result.size, 2)
        result.forEach { item -> assertTrue { !item.isDeleted } }
    }

    @Test
    fun `should OVERWRITE existing media when updating with NEW image`() = runTest {
        val product = createDummyProduct()
        val imageA = byteArrayOf(1, 1, 1)
        catalogRepository.insertProduct(product, imageA)

        val initialMedia = db.productMediaDao().getByProductId(product.id)
        assertNotNull(initialMedia)
        assertTrue(imageA.contentEquals(initialMedia.blob), "It should be the same image")

        val imageB = byteArrayOf(2, 2, 2)
        val updatedProduct = product.copy(name = "Produto com Nova Foto", updatedAt = getCurrentTime())
        catalogRepository.updateForProductIfNewer(updatedProduct, imageB)

        val finalMedia = db.productMediaDao().getByProductId(product.id)
        assertNotNull(finalMedia)
        assertTrue(imageB.contentEquals(finalMedia.blob), "The image A should be overwritten by image B")
        assertFalse(imageA.contentEquals(finalMedia.blob), "The image A should not exist anymore")

        assertEquals(product.id, finalMedia.productId)
    }

    @Test
    fun `searchProducts should NOT return products marked as DELETED`() = runTest {
        val searchTerm = "Leite"

        val activeProduct = createDummyProduct(name = "Leite Condensado")
        val deletedProduct = createDummyProduct(name = "Leite em Pó", isDeleted = true)

        catalogRepository.insertProduct(activeProduct, null)
        catalogRepository.insertProduct(deletedProduct, null)

        catalogRepository.searchProductsByNameOrBrand(searchTerm).test {
            val results = awaitItem()

            assertEquals(1, results.size, "Only one product should be found ")
            assertEquals(activeProduct.id, results[0].id, "The returned product should be active")
            assertFalse(results.any { it.isDeleted }, "No products should be found")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should emit NULL when getProductById is called with non-existent ID`() = runTest {
        val nonExistentId = Uuid.random().toString()

        catalogRepository.getProductById(nonExistentId).test {
            val result = awaitItem()
            assertNull(result, "The repository should respond with null for an non-existent ID")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit NULL when getProductByEan is called with non-existent EAN`() = runTest {
        val nonExistentEan = "9999999999999"
        catalogRepository.getProductByEan(nonExistentEan).test {
            val result = awaitItem()
            assertNull(result, "The repository should respond with null for an non-existent ean")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchProducts should return EMPTY LIST when no matches are found`() = runTest {
        catalogRepository.searchProductsByNameOrBrand("termo_que_nao_existe").test {
            val results = awaitItem()
            assertTrue(results.isEmpty(), "It should return a emptyList()")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchProducts should return products matching the BRAND name`() = runTest {
        val brandToSearch = "Nestlé"

        val product1 = createDummyProduct(name = "Leite em Pó", brand = brandToSearch)
        val product2 = createDummyProduct(name = "Chocolate", brand = brandToSearch)
        val product3 = createDummyProduct(name = "Café Solúvel", brand = "Três Corações")

        catalogRepository.insertProduct(product1, null)
        catalogRepository.insertProduct(product2, null)
        catalogRepository.insertProduct(product3, null)

        catalogRepository.searchProductsByNameOrBrand(brandToSearch).test {
            val results = awaitItem()

            assertEquals(2, results.size, "It should return 2 products of brand: $brandToSearch")

            val foundBrands = results.map { it.brand }
            assertTrue(foundBrands.all { it == brandToSearch }, "All results should be from the searched brand")
            assertFalse(foundBrands.contains("Três Corações"), "It should not return products form other brands")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `tests if user exists`() = runTest {
        val id = Uuid.random().toString()
        val product1 = createDummyProduct(id = id)

        catalogRepository.insertProduct(product1, null)

        catalogRepository.exists(id).test {
            val result = awaitItem()
            assertTrue { result }
        }

        catalogRepository.deleteProductById(id)

        catalogRepository.exists(id).test {
            val result = awaitItem()
            assertTrue { !result }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should PHYSICAL DELETE product and its media by ID`() = runTest {
        val productId = Uuid.random().toString()
        val product = createDummyProduct(id = productId)
        val imageBlob = byteArrayOf(1, 2, 3)

        catalogRepository.insertProduct(product, imageBlob)

        val savedProduct = catalogRepository.getProductById(productId).first()
        assertNotNull(savedProduct)
        assertNotNull(db.productMediaDao().getByProductId(productId))

        catalogRepository.deleteProductById(productId)

        val resultProduct = catalogRepository.getProductById(productId).first()
        assertNull(resultProduct, " The product should be removed from db")

        val resultMedia = db.productMediaDao().getByProductId(productId)
        assertNull(resultMedia, "Media should be removed by CASCADE")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should IGNORE catalog product UPDATE if INCOME timestamp is OLDER`() = runTest {
        val productId = Uuid.random().toString()
        val olderRemoteTime = getCurrentTime()
        val localTime = getCurrentTime() + 20
        val localProduct = createDummyProduct(id = productId, name = "Versão Recente", brand = "Marca A")
            .copy(updatedAt = localTime)

        catalogRepository.insertProduct(localProduct, null)
        val remoteProduct = localProduct.copy(name = "Versão Antiga", updatedAt = olderRemoteTime)

        catalogRepository.updateForProductIfNewer(remoteProduct, null)

        val result = catalogRepository.getProductById(productId).first()
        assertEquals("Versão Recente", result?.name, "Should not accept older records overwrite (Last Write Wins)")
        assertEquals(localTime, result?.updatedAt)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT resurrect a logically deleted product with an older sync update`() = runTest {
        val productId = Uuid.random().toString()
        val product = createDummyProduct(id = productId, isDeleted = false)
        catalogRepository.insertProduct(product, null)

        val deleteTime = getCurrentTime()
        catalogRepository.updateForProductIfNewer(product.copy(isDeleted = true, updatedAt = deleteTime), null)

        val oldSyncTime = deleteTime - 1000
        val resurrectedProduct = product.copy(isDeleted = false, updatedAt = oldSyncTime)

        catalogRepository.updateForProductIfNewer(resurrectedProduct, null)

        val result = catalogRepository.getProductById(productId).first()
        assertEquals(result?.isDeleted, true, "An old synchronization record should not 'delete' a product.")
    }

    @Test
    fun `should ROLLBACK product insertion if media storage fails`() = runTest {
        val product = createDummyProduct(name = "Transaction Test")
        try {
            db.useWriterConnection { conn ->
                conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    db.catalogDao().insert(product.toEntity())
                    throw RuntimeException("Simulated Media Failure")
                }
            }
        } catch (e: Exception) {

        }
        val result = catalogRepository.getProductById(product.id).first()
        assertNull(result, "Product should not exist if media transaction failed")
    }

    // -------------------------------------------------------------------------
    // 1. VALIDAÇÃO DE EAN (8, 13 e 14 DÍGITOS E TAMANHOS INVÁLIDOS)
    // -------------------------------------------------------------------------

    @Test
    fun `should save product with valid EAN-8 EAN-13 and EAN-14`() = runTest {
        val validEans = listOf(
            "12345670",            // 8 dígitos
            "7891234567890",       // 13 dígitos
            "17891234567897"       // 14 dígitos
        )

        validEans.forEach { validEan ->
            val product = createDummyProduct(ean = validEan)
            catalogRepository.insertProduct(product, null)

            val saved = catalogRepository.getProductById(product.id).first()
            assertNotNull(saved)
            assertEquals(validEan, saved.ean)
        }
    }

    @Test
    fun `should fail validation when EAN length is invalid`() = runTest {
        val invalidEans = listOf(
            "1234567",             // 7 dígitos (< 8)
            "123456789",           // 9 dígitos (entre 8 e 13)
            "123456789012",        // 12 dígitos
            "123456789012345"      // 15 dígitos (> 14)
        )

        invalidEans.forEach { invalidEan ->
            val product = createDummyProduct(ean = invalidEan)
            assertFails {
                catalogRepository.insertProduct(product, null)
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. VALIDAÇÃO DE CATEGORIA (VAZIA E LIMITE > 20 CARACTERES)
    // -------------------------------------------------------------------------

    @Test
    fun `should fail validation when category is blank or empty`() = runTest {
        val blankCategories = listOf("", "   ")
        blankCategories.forEach { blankCategory ->
            val product = createDummyProduct(category = blankCategory)
            assertFails {
                catalogRepository.insertProduct(product, null)
            }
        }
    }

    @Test
    fun `should fail validation when category exceeds 20 characters`() = runTest {
        val categoryExceedingLimit = "A".repeat(21)
        val product = createDummyProduct(category = categoryExceedingLimit)
        assertFails {
            catalogRepository.insertProduct(product, null)
        }
    }

    // -------------------------------------------------------------------------
    // 3. LIMITES EXATOS DE NOME (100) E MARCA (50)
    // -------------------------------------------------------------------------
    @Test
    fun `should save product with name having exactly 30 characters`() = runTest {
        val exact30Name = "A".repeat(30)
        val product = createDummyProduct(name = exact30Name)

        catalogRepository.insertProduct(product, null)

        val result = catalogRepository.getProductById(product.id).first()
        assertNotNull(result)
        assertEquals(exact30Name, result.name)
        assertEquals(30, result.name.length)
    }

    @Test
    fun `should save product with brand having exactly 30 characters`() = runTest {
        val exact30Brand = "B".repeat(30)
        val product = createDummyProduct(brand = exact30Brand)

        catalogRepository.insertProduct(product, null)

        val result = catalogRepository.getProductById(product.id).first()
        assertNotNull(result)
        assertEquals(exact30Brand, result.brand)
        assertEquals(30, result.brand?.length)
    }

    // -------------------------------------------------------------------------
    // 4. LIMITES DE TAMANHO DE IMAGEM (100 KB E > 100 KB)
    // -------------------------------------------------------------------------
    @Test
    fun `should save image when size is exactly 100 KB`() = runTest {
        val product = createDummyProduct()
        val exact100KbImage = ByteArray(100 * 1024) { 1 }

        catalogRepository.insertProduct(product, exact100KbImage)

        val media = db.productMediaDao().getByProductId(product.id)
        assertNotNull(media)
        assertEquals(100 * 1024, media.blob.size)
    }

    @Test
    fun `should reject image when size is strictly greater than 100 KB`() = runTest {
        val product = createDummyProduct()
        val over100KbImage = ByteArray(101 * 1024) { 1 } // 101 KB

        assertFails {
            catalogRepository.insertProduct(product, over100KbImage)
        }

        val media = db.productMediaDao().getByProductId(product.id)
        assertNull(media, "Media should not be persisted when size exceeds limit")
    }

    // -------------------------------------------------------------------------
    // 5. BUSCA COM QUERIES DE 49 E 50 CARACTERES
    // -------------------------------------------------------------------------

    @Test
    fun `should find product when searching with queries of 29 and 30 characters`() = runTest {
        val name30Chars = "A".repeat(30)
        val product = createDummyProduct(name = name30Chars)
        catalogRepository.insertProduct(product, null)

        val query29 = name30Chars.take(29)
        val query30 = name30Chars

        catalogRepository.searchProductsByNameOrBrand(query29).test {
            val result = awaitItem()
            assertEquals(1, result.size, "Deve encontrar o produto com query de 29 caracteres")
            assertEquals(product.id, result[0].id)
            cancelAndIgnoreRemainingEvents()
        }

        catalogRepository.searchProductsByNameOrBrand(query30).test {
            val result = awaitItem()
            assertEquals(1, result.size, "Deve encontrar o produto com query de 30 caracteres")
            assertEquals(product.id, result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // 6. LWW REJEITADO NÃO DEVE SUBSTITUIR OU ATUALIZAR A IMAGEM
    // -------------------------------------------------------------------------

    @Test
    fun `should NOT overwrite existing media when incoming LWW update is rejected`() = runTest {
        val localTime = getCurrentTime()
        val olderRemoteTime = localTime - 5_000

        val initialProduct = createDummyProduct(
            name = "Produto Original",
            updatedAt = localTime
        )
        val originalImage = byteArrayOf(1, 2, 3, 4)
        catalogRepository.insertProduct(initialProduct, originalImage)

        val outdatedRemoteProduct = initialProduct.copy(
            name = "Produto Desatualizado",
            updatedAt = olderRemoteTime
        )
        val newRejectedImage = byteArrayOf(9, 9, 9, 9)

        // Executa update defasado (LWW deve ignorar metadados e imagem)
        catalogRepository.updateForProductIfNewer(outdatedRemoteProduct, newRejectedImage)

        val savedProduct = catalogRepository.getProductById(initialProduct.id).first()
        assertEquals("Produto Original", savedProduct?.name, "O produto deve manter os metadados locais mais novos")
        assertEquals(localTime, savedProduct?.updatedAt)

        val mediaInDb = db.productMediaDao().getByProductId(initialProduct.id)
        assertNotNull(mediaInDb)
        assertTrue(
            originalImage.contentEquals(mediaInDb.blob),
            "A imagem original deve ser mantida; a nova imagem não pode ser aplicada se o LWW rejeitou o produto"
        )
        assertFalse(
            newRejectedImage.contentEquals(mediaInDb.blob),
            "A nova imagem não deve ter sido gravada no banco"
        )
    }

    @Test
    fun `should fail validation when search query exceeds 50 characters`() = runTest {
        val query51Chars = "X".repeat(51)

        assertFailsWith<IllegalArgumentException> {
            catalogRepository.searchProductsByNameOrBrand(query51Chars)
        }
    }

    private fun assertProduct(value: CatalogProduct?, ref: CatalogProduct) {
        assertTrue { value != null }
        val netWeight = value?.netWeight ?: 0.0
        assertEquals(ref.id, value?.id)
        assertEquals(ref.name, value?.name)
        assertEquals(ref.brand, value?.brand)
        assertEquals(ref.measureUnit, value?.measureUnit)
        assertEquals(ref.netWeight, netWeight, 0.001)
        assertEquals(ref.updatedAt, value?.updatedAt)
        assertEquals(ref.isDeleted, value?.isDeleted)
        assertEquals(ref.manuallyAdded, value?.manuallyAdded)
        assertEquals(ref.ean, value?.ean)
        assertEquals(ref.thumbnailUrl, value?.thumbnailUrl)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyProduct(
        id: String = Uuid.random().toString(),
        name: String = "Produto Teste",
        ean: String? = null,
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false,
        category: String = "Outros",
        brand: String = ""
    ) = CatalogProduct(
        id = id,
        name = name,
        brand = brand,
        measureUnit = MeasureUnit.KILOGRAM,
        netWeight = 1.0,
        category = category,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        manuallyAdded = true,
        ean = ean,
        thumbnailUrl = null
    )
}
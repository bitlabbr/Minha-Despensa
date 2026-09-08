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

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemConsumption
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomPantryRepositoryTest : BaseTest() {
    private lateinit var pantryRepository: RoomPantryRepository
    private lateinit var catalogRepository: RoomCatalogRepository
    private lateinit var db: AppDatabase
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        pantryRepository = RoomPantryRepository(db, logger)
        catalogRepository = RoomCatalogRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun `should insert and retrieve pantry item successfully`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val pantryItem = createDummyPantryItem(productId = product.id, quantity = 5.0)
        pantryRepository.insertPantryItem(pantryItem)

        pantryRepository.getPantryItemsByID(pantryItem.id).test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals(5.0, item.quantity)
            assertEquals(product.id, item.productId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should ignore older sync updates for pantry items following LWW`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val oldTimestamp = getCurrentTime()
        val newTimestamp = getCurrentTime() + 100

        val localItem = createDummyPantryItem(productId = product.id, quantity = 10.0, updatedAt = newTimestamp)
        pantryRepository.insertPantryItem(localItem)

        val olderSyncItem = localItem.copy(quantity = 2.0, updatedAt = oldTimestamp)
        pantryRepository.updatePantryItemIfNewer(olderSyncItem)

        val result = pantryRepository.getPantryItemsByID(localItem.id).first()
        assertNotNull(result)
        assertEquals(10.0, result.quantity, "Should ignore old records")
    }

    @Test
    fun `should fail when inserting pantry item with invalid product ID `() = runTest {
        val ghostItem = createDummyPantryItem(productId = "invalid-uuid")
        assertFails {
            pantryRepository.insertPantryItem(ghostItem)
        }
    }

    @Test
    fun `should logically delete pantry item and filter it from active inventory`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)
        val item = createDummyPantryItem(productId = product.id)
        pantryRepository.insertPantryItem(item)

        pantryRepository.markPantryItemAsDeleted(item.id, getCurrentTime())

        pantryRepository.getAllActivePantryItems().test {
            val list = awaitItem()
            assertTrue(list.isEmpty(), "Deleted items logically should not appear in active items list")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should catch validation errors like negative quantities`() = runTest {
        val invalidItem = createDummyPantryItem(quantity = -1.0)
        assertFailsWith<IllegalArgumentException> {
            pantryRepository.insertPantryItem(invalidItem)
        }
    }

    @Test
    fun `should physical delete item from database`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)
        val item = createDummyPantryItem(productId = product.id)
        pantryRepository.insertPantryItem(item)

        pantryRepository.deletePantryItemById(item.id)

        pantryRepository.getPantryItemsByID(item.id).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should delete all pantry items via CASCADE when product is removed from catalog`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)
        val item = createDummyPantryItem(productId = product.id)
        pantryRepository.insertPantryItem(item)

        catalogRepository.deleteProductById(product.id)

        pantryRepository.getPantryItemsByID(item.id).test {
            assertNull(awaitItem(), "The item should be deleted")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should update pantry item when incoming sync data is STRICTLY newer`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val localItem = createDummyPantryItem(productId = product.id, quantity = 1.0, updatedAt = getCurrentTime())
        pantryRepository.insertPantryItem(localItem)

        val newerSyncItem = localItem.copy(quantity = 5.0, updatedAt = getCurrentTime() + 100)
        pantryRepository.updatePantryItemIfNewer(newerSyncItem)

        val result = pantryRepository.getPantryItemsByID(localItem.id).first()
        assertNotNull(result)
        assertEquals(5.0, result.quantity, "The db should accept the newest value")
    }

    @Test
    fun `should persist and retrieve expiration date and batch number correctly`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val expiry = getCurrentTime() + 100000L
        val batch = "BATCH-2026-XYZ"
        val item = createDummyPantryItem(productId = product.id).copy(
            expirationDate = expiry,
            batchNumber = batch
        )

        pantryRepository.insertPantryItem(item)
        val saved = pantryRepository.getPantryItemsByID(item.id).first()

        assertNotNull(saved)
        assertEquals(expiry, saved.expirationDate)
        assertEquals(batch, saved.batchNumber)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should return empty list when searching for non-existent product ID`() = runTest {
        pantryRepository.getPantryItemsByProductID(Uuid.random().toString()).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should correctly filter multiple items by Product ID`() = runTest {
        val productA = createDummyProduct(name = "Arroz")
        val productB = createDummyProduct(name = "Feijão")
        catalogRepository.insertProduct(productA, null)
        catalogRepository.insertProduct(productB, null)

        pantryRepository.insertPantryItem(createDummyPantryItem(productId = productA.id))
        pantryRepository.insertPantryItem(createDummyPantryItem(productId = productA.id))
        pantryRepository.insertPantryItem(createDummyPantryItem(productId = productB.id))

        pantryRepository.getPantryItemsByProductID(productA.id).test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertTrue(list.all { it.productId == productA.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // 1. JOINS E PROJEÇÃO COM CATEGORIAS
    // -------------------------------------------------------------------------

    @Test
    fun `getAllActivePantryItemsWithCategory should return items combined with catalog category and name`() = runTest {
        val product = createDummyProduct(name = "Arroz Integral", category = "Grãos")
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 3.0)
        pantryRepository.insertPantryItem(item)

        pantryRepository.getAllActivePantryItemsWithCategory().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Arroz Integral", list[0].name)
            assertEquals("Grãos", list[0].category)
            assertEquals(3.0, list[0].pantryItem.quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPantryItemWithCategoryByID should return full joined details for valid item`() = runTest {
        val product = createDummyProduct(name = "Feijão Preto", category = "Grãos")
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 2.0)
        pantryRepository.insertPantryItem(item)

        pantryRepository.getPantryItemWithCategoryByID(item.id).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(item.id, result.pantryItem.id)
            assertEquals("Feijão Preto", result.name)
            assertEquals("Grãos", result.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should filter out pantry items if associated catalog product is soft-deleted`() = runTest {
        val product = createDummyProduct(name = "Leite", category = "Laticínios")
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 1.0)
        pantryRepository.insertPantryItem(item)

        // Soft-delete no produto do catálogo
        catalogRepository.updateForProductIfNewer(
            product.copy(isDeleted = true, updatedAt = getCurrentTime() + 100),
            null
        )

        pantryRepository.getAllActivePantryItemsWithCategory().test {
            val activeList = awaitItem()
            assertTrue(activeList.isEmpty(), "Item da despensa cujo produto pai foi soft-deleted não deve aparecer no JOIN")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // 2. QUANTIDADE ZERO E VALIDAÇÕES DE ESTOQUE
    // -------------------------------------------------------------------------

    @Test
    fun `should allow pantry item with zero quantity for tracking out of stock`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val zeroQuantityItem = createDummyPantryItem(productId = product.id, quantity = 0.0)
        pantryRepository.insertPantryItem(zeroQuantityItem)

        val result = pantryRepository.getPantryItemsByID(zeroQuantityItem.id).first()
        assertNotNull(result)
        assertEquals(0.0, result.quantity)
    }

    // -------------------------------------------------------------------------
    // 3. LWW: EMPATE DE TIMESTAMPS E FORCE UPDATE
    // -------------------------------------------------------------------------

    @Test
    fun `should ignore update when incoming timestamp is EQUAL to current timestamp`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val timestamp = getCurrentTime()
        val localItem = createDummyPantryItem(productId = product.id, quantity = 5.0, updatedAt = timestamp)
        pantryRepository.insertPantryItem(localItem)

        val sameTimestampUpdate = localItem.copy(quantity = 10.0, updatedAt = timestamp)
        pantryRepository.updatePantryItemIfNewer(sameTimestampUpdate)

        val result = pantryRepository.getPantryItemsByID(localItem.id).first()
        assertNotNull(result)
        assertEquals(5.0, result.quantity, "Atualizações com timestamp igual não devem sobrescrever estado local")
    }

    @Test
    fun `forceUpdatePantryItem should overwrite record unconditionally even with older timestamp`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val now = getCurrentTime()
        val newerLocalTimestamp = now - 1_000
        val olderForcedTimestamp = now - 10_000

        val initialItem = createDummyPantryItem(productId = product.id, quantity = 5.0, updatedAt = newerLocalTimestamp)
        pantryRepository.insertPantryItem(initialItem)

        val forcedItem = initialItem.copy(quantity = 1.0, updatedAt = olderForcedTimestamp)
        pantryRepository.forceUpdatePantryItem(forcedItem)

        val result = pantryRepository.getPantryItemsByID(initialItem.id).first()
        assertNotNull(result)
        assertEquals(1.0, result.quantity)
        assertEquals(olderForcedTimestamp, result.updatedAt)
    }

    // -------------------------------------------------------------------------
    // 4. ITENS VENCIDOS VS PRÓXIMOS DE VENCER
    // -------------------------------------------------------------------------

    @Test
    fun `getExpiringPantryItems should strictly separate already expired items from expiring soon`() = runTest {
        val now = getCurrentTime()
        val dayMillis = 86_400_000L

        val prodExpired = createDummyProduct(name = "Vencido")
        val prodExpiring = createDummyProduct(name = "Vencendo")
        val prodFarFuture = createDummyProduct(name = "Validade Longa")

        listOf(prodExpired, prodExpiring, prodFarFuture).forEach {
            catalogRepository.insertProduct(it, null)
        }

        // Vencido há 1 dia (não deve entrar em 'próximos de vencer')
        pantryRepository.insertPantryItem(
            createDummyPantryItem(productId = prodExpired.id, expirationDate = now - dayMillis)
        )
        // Vencendo em 3 dias (deve entrar no threshold de 7 dias)
        pantryRepository.insertPantryItem(
            createDummyPantryItem(productId = prodExpiring.id, expirationDate = now + (3 * dayMillis))
        )
        // Vence em 15 dias (fora do threshold)
        pantryRepository.insertPantryItem(
            createDummyPantryItem(productId = prodFarFuture.id, expirationDate = now + (15 * dayMillis))
        )

        pantryRepository.getExpiringPantryItems(thresholdDays = 7).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(prodExpiring.id, result[0].pantryItem.productId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // FLUXOS DE CONSUMO (SINGLE ITEM & BATCH/RECEITAS)
    // -------------------------------------------------------------------------

    @Test
    fun `consumePantryItem should partially deduct stock and update timestamp`() = runTest {
        val product = createDummyProduct(name = "Arroz")
        catalogRepository.insertProduct(product, null)

        val initialTime = getCurrentTime() - 5_000
        val item = createDummyPantryItem(productId = product.id, quantity = 5.0, updatedAt = initialTime)
        pantryRepository.insertPantryItem(item)

        pantryRepository.consumePantryItem(pantryItemId = item.id, quantityToConsume = 1.5)

        val updated = pantryRepository.getPantryItemsByID(item.id).first()
        assertNotNull(updated)
        assertEquals(3.5, updated.quantity, 0.001)
        assertTrue(updated.updatedAt > initialTime, "updatedAt must be updated after consumption")
    }

    @Test
    fun `consumePantryItem should reach zero quantity when full amount is consumed`() = runTest {
        val product = createDummyProduct(name = "Leite")
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 2.0)
        pantryRepository.insertPantryItem(item)

        pantryRepository.consumePantryItem(pantryItemId = item.id, quantityToConsume = 2.0)

        val updated = pantryRepository.getPantryItemsByID(item.id).first()
        assertNotNull(updated)
        assertEquals(0.0, updated.quantity, "the pantry should be zeroed immediately")
        assertFalse(
            updated.isDeleted,
            "The item must remain active with a quantity of zero to receive a replenishment notification."
        )
    }

    @Test
    fun `consumePantryItem should fail when attempting to consume more than available stock`() = runTest {
        val product = createDummyProduct(name = "Açúcar")
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 1.0)
        pantryRepository.insertPantryItem(item)

        assertFailsWith<IllegalArgumentException> {
            pantryRepository.consumePantryItem(pantryItemId = item.id, quantityToConsume = 1.5)
        }

        val intact = pantryRepository.getPantryItemsByID(item.id).first()
        assertNotNull(intact)
        assertEquals(1.0, intact.quantity, "The balance cannot be changed if validation fails.")
    }

    @Test
    fun `consumePantryItem should reject zero or negative consumption amounts`() = runTest {
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val item = createDummyPantryItem(productId = product.id, quantity = 3.0)
        pantryRepository.insertPantryItem(item)

        assertFailsWith<IllegalArgumentException> {
            pantryRepository.consumePantryItem(pantryItemId = item.id, quantityToConsume = 0.0)
        }

        assertFailsWith<IllegalArgumentException> {
            pantryRepository.consumePantryItem(pantryItemId = item.id, quantityToConsume = -1.0)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `consumePantryItem should fail when target item is non-existent or soft-deleted`() = runTest {
        // Item inexistente
        val nonExistentId = Uuid.random().toString()
        assertFailsWith<IllegalStateException> {
            pantryRepository.consumePantryItem(nonExistentId, 1.0)
        }

        // Item com soft-delete
        val product = createDummyProduct()
        catalogRepository.insertProduct(product, null)

        val deletedItem = createDummyPantryItem(productId = product.id, quantity = 2.0)
        pantryRepository.insertPantryItem(deletedItem)
        pantryRepository.markPantryItemAsDeleted(deletedItem.id, getCurrentTime())

        assertFailsWith<IllegalArgumentException> {
            pantryRepository.consumePantryItem(deletedItem.id, 1.0)
        }
    }

    @Test
    fun `consumeBatch should deduct multiple recipe ingredients atomically`() = runTest {
        val prodRice = createDummyProduct(name = "Arroz")
        val prodOil = createDummyProduct(name = "Óleo")
        val prodEgg = createDummyProduct(name = "Ovo")

        listOf(prodRice, prodOil, prodEgg).forEach { catalogRepository.insertProduct(it, null) }

        val itemRice = createDummyPantryItem(productId = prodRice.id, quantity = 5.0)
        val itemOil = createDummyPantryItem(productId = prodOil.id, quantity = 2.0)
        val itemEgg = createDummyPantryItem(productId = prodEgg.id, quantity = 12.0)

        listOf(itemRice, itemOil, itemEgg).forEach { pantryRepository.insertPantryItem(it) }

        val recipeConsumptions = listOf(
            PantryItemConsumption(pantryItemId = itemRice.id, quantityToConsume = 1.0),
            PantryItemConsumption(pantryItemId = itemOil.id, quantityToConsume = 0.5),
            PantryItemConsumption(pantryItemId = itemEgg.id, quantityToConsume = 3.0)
        )

        pantryRepository.consumeBatch(recipeConsumptions)

        assertEquals(4.0, pantryRepository.getPantryItemsByID(itemRice.id).first()?.quantity)
        assertEquals(1.5, pantryRepository.getPantryItemsByID(itemOil.id).first()?.quantity)
        assertEquals(9.0, pantryRepository.getPantryItemsByID(itemEgg.id).first()?.quantity)
    }

    @Test
    fun `consumeBatch should rollback entire recipe consumption if one ingredient has insufficient stock`() = runTest {
        val prodRice = createDummyProduct(name = "Arroz")
        val prodOil = createDummyProduct(name = "Óleo")

        catalogRepository.insertProduct(prodRice, null)
        catalogRepository.insertProduct(prodOil, null)

        val itemRice = createDummyPantryItem(productId = prodRice.id, quantity = 5.0)
        val itemOil = createDummyPantryItem(productId = prodOil.id, quantity = 0.2) // Saldo insuficiente

        pantryRepository.insertPantryItem(itemRice)
        pantryRepository.insertPantryItem(itemOil)

        val recipeConsumptions = listOf(
            PantryItemConsumption(pantryItemId = itemRice.id, quantityToConsume = 1.0),
            PantryItemConsumption(pantryItemId = itemOil.id, quantityToConsume = 1.0) // Falhará aqui
        )

        assertFailsWith<IllegalArgumentException> {
            pantryRepository.consumeBatch(recipeConsumptions)
        }

        // Rollback verificado: o arroz não pode ter sido descontado
        assertEquals(
            5.0,
            pantryRepository.getPantryItemsByID(itemRice.id).first()?.quantity,
            "The rice must undergo rollback."
        )
        assertEquals(
            0.2,
            pantryRepository.getPantryItemsByID(itemOil.id).first()?.quantity,
            "The oil should remain unchanged."
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyPantryItem(
        id: String = Uuid.random().toString(),
        productId: String = Uuid.random().toString(),
        quantity: Double = 1.0,
        expirationDate: Long? = null,
        updatedAt: Long = getCurrentTime()
    ) = PantryItem(
        id = id,
        productId = productId,
        quantity = quantity,
        updatedAt = updatedAt,
        isDeleted = false,
        expirationDate = expirationDate,
        batchNumber = null
    )

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyProduct(
        id: String = Uuid.random().toString(),
        name: String = "Produto Teste",
        isDeleted: Boolean = false,
        brand: String = "",
        category: String = CoreConstants.Product.DEFAULT_CATEGORY,
        updatedAt: Long = getCurrentTime()
    ) = CatalogProduct(
        id = id,
        name = name,
        brand = brand,
        category = category,
        measureUnit = MeasureUnit.KILOGRAM,
        netWeight = 1.0,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        manuallyAdded = true,
        ean = null,
        thumbnailUrl = null
    )
}

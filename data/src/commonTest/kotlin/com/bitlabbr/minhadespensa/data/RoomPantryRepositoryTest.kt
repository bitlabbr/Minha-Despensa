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
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyPantryItem(
        id: String = Uuid.random().toString(),
        productId: String = Uuid.random().toString(),
        quantity: Double = 1.0,
        updatedAt: Long = getCurrentTime()
    ) = PantryItem(
        id = id,
        productId = productId,
        quantity = quantity,
        updatedAt = updatedAt,
        isDeleted = false,
        expirationDate = null,
        batchNumber = null
    )

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyProduct(
        id: String = Uuid.random().toString(),
        name: String = "Produto Teste",
        isDeleted: Boolean = false,
        brand: String = "",
        updatedAt: Long = getCurrentTime()
    ) = CatalogProduct(
        id = id,
        name = name,
        brand = brand,
        measureUnit = MeasureUnit.KILOGRAM,
        netWeight = 1.0,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        manuallyAdded = true,
        ean = null,
        thumbnailUrl = null
    )
}

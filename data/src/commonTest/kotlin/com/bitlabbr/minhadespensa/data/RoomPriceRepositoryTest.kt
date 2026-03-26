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
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPriceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RoomPriceRepositoryTest : BaseTest() {

    private lateinit var priceRepository: RoomPriceRepository
    private lateinit var productRepository: RoomCatalogRepository
    private lateinit var db: AppDatabase
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        priceRepository = RoomPriceRepository(db, logger)
        productRepository = RoomCatalogRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should insert and retrieve price history for a specific product`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val entry1 = createDummyPriceEntry(productId = productId, price = 1000, updatedAt = getCurrentTime())
        val entry2 = createDummyPriceEntry(productId = productId, price = 1200, updatedAt = getCurrentTime() + 100)

        priceRepository.insertPriceEntry(entry1)
        priceRepository.insertPriceEntry(entry2)

        priceRepository.getPriceHistoryByProductId(productId).test {
            val history = awaitItem()
            assertEquals(2, history.size)
            assertEquals(1200L, history[0].priceInCents)
            assertEquals(1000L, history[1].priceInCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should get the LATEST PRICE entry for a product`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val oldEntry = createDummyPriceEntry(productId = productId, price = 500, updatedAt = getCurrentTime())
        val newEntry = createDummyPriceEntry(productId = productId, price = 900, updatedAt = getCurrentTime() + 100)

        priceRepository.insertPriceEntry(oldEntry)
        priceRepository.insertPriceEntry(newEntry)

        priceRepository.getLatestPriceForProductID(productId).test {
            val latest = awaitItem()
            assertEquals(900L, latest?.priceInCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should ignore older sync updates following LWW strategy`() = runTest {
        val oldTimestamp = getCurrentTime()
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val localEntry = createDummyPriceEntry(productId = productId, price = 1000, updatedAt = getCurrentTime() + 100)
        priceRepository.insertPriceEntry(localEntry)

        val olderRemoteEntry = localEntry.copy(priceInCents = 500, updatedAt = oldTimestamp)
        priceRepository.updatePriceEntryIfNewer(olderRemoteEntry)

        val result = priceRepository.getLatestPriceForProductID(productId).first()
        assertEquals(1000L, result?.priceInCents, "Should keep the newer local price")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should FORCE UPDATE price entry regardless of timestamp`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val oldTimestamp = getCurrentTime()
        val newTimestamp = getCurrentTime() + 100

        val entry = createDummyPriceEntry(productId = productId, price = 1000, updatedAt = oldTimestamp)
        priceRepository.insertPriceEntry(entry)

        val olderForcedEntry = entry.copy(priceInCents = 500, updatedAt = newTimestamp)
        priceRepository.forceUpdatePriceEntry(olderForcedEntry)

        val result = priceRepository.getLatestPriceForProductID(productId).first()
        assertEquals(500L, result?.priceInCents, "Force update should overwrite based on ID")
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should mark price entry as logically deleted`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val entry = createDummyPriceEntry(productId = productId)
        priceRepository.insertPriceEntry(entry)

        priceRepository.markPriceEntryAsDeletedById(entry.id)

        priceRepository.getPriceHistoryByProductId(entry.productId).test {
            val history = awaitItem()
            assertTrue(history.isEmpty(), "Deleted items should not appear in history")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should fail validation when price is zero or negative`() = runTest {
        val invalidEntry = createDummyPriceEntry(price = 0)
        assertFailsWith<IllegalArgumentException> {
            priceRepository.insertPriceEntry(invalidEntry)
        }
    }

    @Test
    fun `should fail validation for invalid UUIDs`() = runTest {
        val invalidEntry = createDummyPriceEntry(id = "not-a-uuid")
        assertFailsWith<IllegalArgumentException> {
            priceRepository.insertPriceEntry(invalidEntry)
        }
    }

    @Test
    fun `should fail validation for empty store name`() = runTest {
        val invalidEntry = createDummyPriceEntry(storeName = "")
        assertFailsWith<IllegalArgumentException> {
            priceRepository.insertPriceEntry(invalidEntry)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should NOT leak price history between different products`() = runTest {
        val idProdA = Uuid.random().toString()
        val productA = createDummyProduct(id = idProdA)
        productRepository.insertProduct(productA, null)

        val idProdB = Uuid.random().toString()
        val productB = createDummyProduct(id = idProdB)
        productRepository.insertProduct(productB, null)

        priceRepository.insertPriceEntry(createDummyPriceEntry(productId = idProdA, price = 100))
        priceRepository.insertPriceEntry(createDummyPriceEntry(productId = idProdB, price = 200))

        priceRepository.getPriceHistoryByProductId(idProdA).test {
            val history = awaitItem()
            assertEquals(1, history.size)
            assertEquals(100L, history[0].priceInCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should physically delete price entry from database`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val entry = createDummyPriceEntry(productId = productId)
        priceRepository.insertPriceEntry(entry)

        priceRepository.deletePriceEntryById(entry.id)

        val result = priceRepository.getLatestPriceForProductID(entry.productId).first()
        assertNull(result, "Entry should no longer exist in DB")
    }

    @Test
    fun `should fail validation when store name exceeds 50 characters`() = runTest {
        val longStoreName = "A".repeat(51)
        val invalidEntry = createDummyPriceEntry(storeName = longStoreName)

        assertFailsWith<IllegalArgumentException> {
            priceRepository.insertPriceEntry(invalidEntry)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should ignore update when incoming timestamp is equal to local timestamp`() = runTest {
        val productId = Uuid.random().toString()

        val product = createDummyProduct(id = productId)
        productRepository.insertProduct(product, null)

        val now = getCurrentTime()
        val entry = createDummyPriceEntry(productId = productId, price = 1000, updatedAt = now)
        priceRepository.insertPriceEntry(entry)

        val sameTimeUpdate = entry.copy(priceInCents = 2000, updatedAt = now)
        priceRepository.updatePriceEntryIfNewer(sameTimeUpdate)

        val result = priceRepository.getLatestPriceForProductID(entry.productId).first()
        assertEquals(1000L, result?.priceInCents, "Should not update if timestamp is not strictly greater")
    }

    @Test
    fun `should fail validation for invalid timestamps`() = runTest {
        val invalidEntry = createDummyPriceEntry(updatedAt = 0L)
        assertFailsWith<IllegalArgumentException> {
            priceRepository.insertPriceEntry(invalidEntry)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `getLatestPrice should emit null when no prices are available`() = runTest {
        priceRepository.getLatestPriceForProductID(Uuid.random().toString()).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyPriceEntry(
        id: String = Uuid.random().toString(),
        productId: String = Uuid.random().toString(),
        price: Long = 100,
        storeName: String? = "Store Test",
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = PriceEntry(
        id = id,
        productId = productId,
        priceInCents = price,
        storeName = storeName,
        updatedAt = updatedAt,
        isDeleted = isDeleted
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
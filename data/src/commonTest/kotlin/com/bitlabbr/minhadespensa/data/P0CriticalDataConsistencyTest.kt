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

import androidx.room.execSQL
import androidx.room.useWriterConnection
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPriceRepository
import com.bitlabbr.minhadespensa.data.repository.RoomShoppingListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class P0CriticalDataConsistencyTest : BaseTest() {

    private lateinit var db: AppDatabase
    private lateinit var catalogRepository: RoomCatalogRepository
    private lateinit var pantryRepository: RoomPantryRepository
    private lateinit var priceRepository: RoomPriceRepository
    private lateinit var shoppingListRepository: RoomShoppingListRepository

    private val logger = ConsoleLogger("P0Test")

    @BeforeTest
    fun setup() {
        db = createInMemoryDatabase(getTestDatabaseBuilder())
        catalogRepository = RoomCatalogRepository(db, logger)
        pantryRepository = RoomPantryRepository(db, logger)
        priceRepository = RoomPriceRepository(db, logger)
        shoppingListRepository = RoomShoppingListRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    // -------------------------------------------------------------------------
    // EXPIRATION WINDOW
    // -------------------------------------------------------------------------

    @Test
    fun `expiring pantry items should contain only non-expired items inside requested day window`() = runTest {
        val now = getCurrentTime()

        val expiredProduct = createProduct(name = "Expired")
        val twoDaysProduct = createProduct(name = "2 days")
        val sixDaysProduct = createProduct(name = "6 days")
        val tenDaysProduct = createProduct(name = "10 days")

        listOf(expiredProduct, twoDaysProduct, sixDaysProduct, tenDaysProduct).forEach {
            catalogRepository.insertProduct(it, null)
        }

        pantryRepository.insertPantryItem(
            createPantryItem(expiredProduct.id, expirationDate = now - DAY_MILLIS)
        )
        pantryRepository.insertPantryItem(
            createPantryItem(twoDaysProduct.id, expirationDate = now + 2 * DAY_MILLIS)
        )
        pantryRepository.insertPantryItem(
            createPantryItem(sixDaysProduct.id, expirationDate = now + 6 * DAY_MILLIS)
        )
        pantryRepository.insertPantryItem(
            createPantryItem(tenDaysProduct.id, expirationDate = now + 10 * DAY_MILLIS)
        )

        val result = pantryRepository.getExpiringPantryItems(7).first()
        val productIds = result.map { it.pantryItem.productId }.toSet()

        assertEquals(2, result.size)
        assertTrue(twoDaysProduct.id in productIds)
        assertTrue(sixDaysProduct.id in productIds)
        assertFalse(expiredProduct.id in productIds)
        assertFalse(tenDaysProduct.id in productIds)
    }

    @Test
    fun `negative expiration threshold should be rejected`() = runTest {
        assertFails {
            pantryRepository.getExpiringPantryItems(-1)
        }
    }

    // -------------------------------------------------------------------------
    // LWW TIE BREAKER: equal timestamp => tombstone wins
    // -------------------------------------------------------------------------

    @Test
    fun `catalog equal timestamp should ignore normal update but accept tombstone and never resurrect`() = runTest {
        val timestamp = getCurrentTime()
        val product = createProduct(name = "Original", updatedAt = timestamp)
        catalogRepository.insertProduct(product, null)

        catalogRepository.updateForProductIfNewer(
            product.copy(name = "Same timestamp update", updatedAt = timestamp),
            null
        )
        assertEquals("Original", catalogRepository.getProductById(product.id).first()?.name)

        catalogRepository.updateForProductIfNewer(
            product.copy(isDeleted = true, updatedAt = timestamp),
            null
        )
        assertTrue(catalogRepository.getProductById(product.id).first()?.isDeleted == true)

        catalogRepository.updateForProductIfNewer(
            product.copy(name = "Resurrected", isDeleted = false, updatedAt = timestamp),
            null
        )
        val final = catalogRepository.getProductById(product.id).first()
        assertTrue(final?.isDeleted == true)
        assertEquals("Original", final?.name)
    }

    @Test
    fun `pantry equal timestamp should ignore normal update but accept tombstone and never resurrect`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val timestamp = getCurrentTime()
        val item = createPantryItem(product.id, quantity = 2.0, updatedAt = timestamp)
        pantryRepository.insertPantryItem(item)

        pantryRepository.updatePantryItemIfNewer(
            item.copy(quantity = 99.0, updatedAt = timestamp)
        )
        assertEquals(2.0, pantryRepository.getPantryItemsByID(item.id).first()?.quantity)

        pantryRepository.updatePantryItemIfNewer(
            item.copy(isDeleted = true, updatedAt = timestamp)
        )
        assertTrue(pantryRepository.getPantryItemsByID(item.id).first()?.isDeleted == true)

        pantryRepository.updatePantryItemIfNewer(
            item.copy(quantity = 50.0, isDeleted = false, updatedAt = timestamp)
        )
        val final = pantryRepository.getPantryItemsByID(item.id).first()
        assertTrue(final?.isDeleted == true)
        assertEquals(2.0, final?.quantity)
    }

    @Test
    fun `price equal timestamp should ignore normal update but accept tombstone and never resurrect`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val timestamp = getCurrentTime()
        val price = createPriceEntry(product.id, price = 1000, updatedAt = timestamp)
        priceRepository.insertPriceEntry(price)

        priceRepository.updatePriceEntryIfNewer(
            price.copy(priceInCents = 2000, updatedAt = timestamp)
        )
        assertEquals(1000L, priceRepository.getLatestPriceForProductID(product.id).first()?.priceInCents)

        priceRepository.updatePriceEntryIfNewer(
            price.copy(isDeleted = true, updatedAt = timestamp)
        )
        assertNull(priceRepository.getLatestPriceForProductID(product.id).first())

        priceRepository.updatePriceEntryIfNewer(
            price.copy(priceInCents = 3000, isDeleted = false, updatedAt = timestamp)
        )
        assertNull(priceRepository.getLatestPriceForProductID(product.id).first())
    }

    @Test
    fun `shopping list equal timestamp should ignore normal update but accept tombstone and never resurrect`() = runTest {
        val timestamp = getCurrentTime()
        val list = createShoppingList(name = "Original", updatedAt = timestamp)
        shoppingListRepository.insertShoppingList(list)

        shoppingListRepository.updateShoppingListIfNewer(
            list.copy(name = "Same timestamp update", updatedAt = timestamp)
        )
        assertEquals("Original", shoppingListRepository.getShoppingListById(list.id).first()?.name)

        shoppingListRepository.updateShoppingListIfNewer(
            list.copy(isDeleted = true, updatedAt = timestamp)
        )
        assertTrue(shoppingListRepository.getShoppingListById(list.id).first()?.isDeleted == true)

        shoppingListRepository.updateShoppingListIfNewer(
            list.copy(name = "Resurrected", isDeleted = false, updatedAt = timestamp)
        )
        val final = shoppingListRepository.getShoppingListById(list.id).first()
        assertTrue(final?.isDeleted == true)
        assertEquals("Original", final?.name)
    }

    @Test
    fun `shopping item equal timestamp should ignore normal update but accept tombstone and never resurrect`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val timestamp = getCurrentTime()
        val listId = Uuid.random().toString()
        val item = createShoppingItem(
            productId = product.id,
            listId = listId,
            quantity = 2.0,
            updatedAt = timestamp
        )
        shoppingListRepository.insertShoppingList(
            createShoppingList(id = listId, items = listOf(item), updatedAt = timestamp)
        )

        shoppingListRepository.updateShoppingItemIfNewer(
            item.copy(quantity = 99.0, updatedAt = timestamp)
        )
        assertEquals(
            2.0,
            shoppingListRepository.getShoppingListById(listId).first()?.items?.first()?.quantity
        )

        shoppingListRepository.updateShoppingItemIfNewer(
            item.copy(isDeleted = true, updatedAt = timestamp)
        )
        assertTrue(
            shoppingListRepository.getShoppingListById(listId).first()?.items?.first()?.isDeleted == true
        )

        shoppingListRepository.updateShoppingItemIfNewer(
            item.copy(quantity = 50.0, isDeleted = false, updatedAt = timestamp)
        )
        val final = shoppingListRepository.getShoppingListById(listId).first()?.items?.first()
        assertTrue(final?.isDeleted == true)
        assertEquals(2.0, final?.quantity)
    }

    // -------------------------------------------------------------------------
    // DELETE TIMESTAMP MUST NOT MOVE BACKWARDS
    // -------------------------------------------------------------------------

    @Test
    fun `pantry delete with older timestamp should not overwrite newer local state`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val now = getCurrentTime()
        val newerTimestamp = now - 1_000
        val olderTimestamp = now - 10_000

        val item = createPantryItem(
            product.id,
            updatedAt = newerTimestamp
        )

        pantryRepository.insertPantryItem(item)

        pantryRepository.markPantryItemAsDeleted(
            item.id,
            olderTimestamp
        )

        val result = pantryRepository
            .getPantryItemsByID(item.id)
            .first()

        assertNotNull(result)
        assertFalse(result.isDeleted)
        assertEquals(newerTimestamp, result.updatedAt)
    }

    @Test
    fun `shopping list delete with older timestamp should not overwrite newer local state`() = runTest {
        val now = getCurrentTime()
        val newerTimestamp = now - 1_000
        val olderTimestamp = now - 10_000

        val list = createShoppingList(
            updatedAt = newerTimestamp
        )

        shoppingListRepository.insertShoppingList(list)

        shoppingListRepository.markShoppingListAsDeleted(
            list.id,
            olderTimestamp
        )

        val result = shoppingListRepository
            .getShoppingListById(list.id)
            .first()

        assertNotNull(result)
        assertFalse(result.isDeleted)
        assertEquals(newerTimestamp, result.updatedAt)
    }

    // -------------------------------------------------------------------------
    // REAL FOREIGN KEY CONSTRAINTS
    // UUID must be valid so repository validation does not intercept the test.
    // -------------------------------------------------------------------------

    @Test
    fun `pantry should reject valid UUID that references non-existent product`() = runTest {
        val ghostProductId = Uuid.random().toString()
        val item = createPantryItem(productId = ghostProductId)

        assertFails {
            pantryRepository.insertPantryItem(item)
        }

        assertNull(pantryRepository.getPantryItemsByID(item.id).first())
    }

    @Test
    fun `price should reject valid UUID that references non-existent product`() = runTest {
        val ghostProductId = Uuid.random().toString()
        val entry = createPriceEntry(productId = ghostProductId)

        assertFails {
            priceRepository.insertPriceEntry(entry)
        }

        assertTrue(priceRepository.getPriceHistoryByProductId(ghostProductId).first().isEmpty())
    }

    @Test
    fun `should allow product substitution in shopping item durit trip`() = runTest {
        val productA = createDummyCatalogProduct(
            name = "Café A",
            ean = "1111111111111",
            netWeight = 3.0,
            measureUnit = MeasureUnit.LITER
        )

        val productB = createDummyCatalogProduct(
            name = "Café B",
            ean = "2222222222222",
            netWeight = 3.0,
            measureUnit = MeasureUnit.LITER
        )

        catalogRepository.insertProduct(productA, null)
        catalogRepository.insertProduct(productB, null)

        val now = getCurrentTime()

        // Ambos válidos e no passado, mas com ordem inequívoca.
        val initialTimestamp = now - 20_000
        val updateTimestamp = now - 10_000

        val listId = Uuid.random().toString()

        val listItem = createDummyShoppingItem(
            productId = productA.id,
            listId = listId,
            quantity = 1.0,
            updatedAt = initialTimestamp
        )

        shoppingListRepository.insertShoppingList(
            createDummyShoppingList(
                id = listId,
                name = "Lista do mês",
                items = listOf(listItem),
                updatedAt = initialTimestamp
            )
        )

        // Confirma primeiro o estado realmente persistido.
        val beforeUpdate = shoppingListRepository
            .getShoppingListById(listId)
            .first()

        assertNotNull(beforeUpdate)
        assertEquals(productA.id, beforeUpdate.items.first().productId)
        assertEquals(initialTimestamp, beforeUpdate.items.first().updatedAt)

        val updatedItem = listItem.copy(
            productId = productB.id,
            quantity = 10.0,
            updatedAt = updateTimestamp
        )

        shoppingListRepository.updateShoppingItemIfNewer(updatedItem)

        val afterUpdate = shoppingListRepository
            .getShoppingListById(listId)
            .first()

        assertNotNull(afterUpdate)

        val resultItem = afterUpdate.items.first()

        assertEquals(
            "Product should have been replaced by Product B",
            productB.id,
            resultItem.productId
        )

        assertEquals(
            10.0,
            resultItem.quantity,
            "Quantity should have been updated"
        )

        assertEquals(
            updateTimestamp,
            resultItem.updatedAt,
            "updatedAt should be the incoming newer timestamp"
        )
    }

    @Test
    fun `shopping item should reject valid non-existent product foreign key`() = runTest {
        val list = createShoppingList()
        shoppingListRepository.insertShoppingList(list)

        val ghostProductId = Uuid.random().toString()
        val item = createShoppingItem(
            productId = ghostProductId,
            listId = list.id,
            quantity = 1.0
        )

        assertFails {
            shoppingListRepository.insertShoppingItem(item)
        }

        assertTrue(shoppingListRepository.getShoppingListById(list.id).first()?.items.isNullOrEmpty())
    }

    @Test
    fun `shopping item should reject valid non-existent list foreign key`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val ghostListId = Uuid.random().toString()
        val item = createShoppingItem(
            productId = product.id,
            listId = ghostListId,
            quantity = 1.0
        )

        assertFails {
            shoppingListRepository.insertShoppingItem(item)
        }

        assertTrue(db.shoppingItemDao().getActiveItems().first().isEmpty())
    }

    // -------------------------------------------------------------------------
    // ATOMICITY
    // -------------------------------------------------------------------------

    @Test
    fun `shopping list insertion should rollback parent list when one item violates foreign key`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val validItem = createShoppingItem(
            productId = product.id,
            listId = listId,
            quantity = 1.0
        )
        val invalidItem = createShoppingItem(
            productId = Uuid.random().toString(),
            listId = listId,
            quantity = 1.0
        )
        val list = createShoppingList(
            id = listId,
            items = listOf(validItem, invalidItem)
        )

        assertFails {
            shoppingListRepository.insertShoppingList(list)
        }

        assertNull(
            shoppingListRepository.getShoppingListById(listId).first(),
            "The parent list must be rolled back if any child insert fails"
        )
        assertTrue(db.shoppingItemDao().getActiveItems().first().isEmpty())
    }

    @Test
    fun `finalize purchase should rollback pantry price checklist and list timestamp when one write fails`() = runTest {
        val product = createProduct()
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val initialTimestamp = getCurrentTime()
        val item = createShoppingItem(
            productId = product.id,
            listId = listId,
            quantity = 3.0,
            priceAtTime = 1500,
            isChecked = true,
            updatedAt = initialTimestamp
        )
        val list = createShoppingList(
            id = listId,
            items = listOf(item),
            updatedAt = initialTimestamp
        )
        shoppingListRepository.insertShoppingList(list)

        db.useWriterConnection { connection ->
            connection.execSQL(
                """
                CREATE TRIGGER fail_price_insert
                BEFORE INSERT ON price_entries
                BEGIN
                    SELECT RAISE(ABORT, 'forced price insert failure');
                END
                """.trimIndent()
            )
        }

        try {
            assertFails {
                shoppingListRepository.finalizePurchase(listId)
            }
        } finally {
            db.useWriterConnection { connection ->
                connection.execSQL("DROP TRIGGER IF EXISTS fail_price_insert")
            }
        }

        assertTrue(
            db.pantryDao().getAllActivePantryItems().first().isEmpty(),
            "Pantry insert must rollback when price insertion fails"
        )
        assertTrue(
            db.priceDao().getPriceHistoryByProductId(product.id).first().isEmpty(),
            "No price record may survive a failed checkout"
        )

        val afterFailure = shoppingListRepository.getShoppingListById(listId).first()
        assertNotNull(afterFailure)
        assertTrue(afterFailure.items.single().isChecked, "Checklist state must rollback")
        assertEquals(initialTimestamp, afterFailure.updatedAt, "List timestamp must rollback")
    }

    // -------------------------------------------------------------------------
    // FIXTURES
    // -------------------------------------------------------------------------

    private fun createProduct(
        id: String = Uuid.random().toString(),
        name: String = "P0 Product",
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = CatalogProduct(
        id = id,
        name = name,
        brand = "P0",
        category = "Outros",
        measureUnit = MeasureUnit.UNITY,
        netWeight = 1.0,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        manuallyAdded = true,
        ean = null,
        thumbnailUrl = null
    )

    private fun createPantryItem(
        productId: String,
        id: String = Uuid.random().toString(),
        quantity: Double = 1.0,
        expirationDate: Long? = null,
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = PantryItem(
        id = id,
        productId = productId,
        quantity = quantity,
        expirationDate = expirationDate,
        batchNumber = null,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private fun createPriceEntry(
        productId: String,
        id: String = Uuid.random().toString(),
        price: Long = 1000,
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = PriceEntry(
        id = id,
        productId = productId,
        priceInCents = price,
        storeName = "P0 Store",
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private fun createShoppingList(
        id: String = Uuid.random().toString(),
        name: String = "P0 List",
        items: List<ShoppingItem> = emptyList(),
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = ShoppingList(
        id = id,
        name = name,
        budgetInCents = 10_000,
        items = items,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private fun createShoppingItem(
        productId: String,
        listId: String,
        id: String = Uuid.random().toString(),
        quantity: Double,
        priceAtTime: Long? = null,
        isChecked: Boolean = false,
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ) = ShoppingItem(
        id = id,
        productId = productId,
        listID = listId,
        quantity = quantity,
        priceAtTime = priceAtTime,
        isChecked = isChecked,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private companion object {
        const val DAY_MILLIS = 86_400_000L
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun createDummyShoppingList(
    id: String = Uuid.random().toString(),
    name: String = "name",
    items: List<ShoppingItem> = emptyList(),
    updatedAt: Long = getCurrentTime(),
    isDeleted: Boolean = false,
    budgetInCents: Long? = null
) = ShoppingList(
    id = id,
    name = name,
    budgetInCents = budgetInCents,
    items = items,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

@OptIn(ExperimentalUuidApi::class)
private fun createDummyShoppingItem(
    id: String = Uuid.random().toString(),
    productId: String,
    listId: String,
    quantity: Double,
    isChecked: Boolean = false,
    priceAtTime: Long? = null,
    updatedAt: Long = getCurrentTime()
) = ShoppingItem(
    id = id,
    productId = productId,
    listID = listId,
    quantity = quantity,
    isChecked = isChecked,
    priceAtTime = priceAtTime,
    updatedAt = updatedAt,
    isDeleted = false
)

@OptIn(ExperimentalUuidApi::class)
private fun createDummyCatalogProduct(
    id: String = Uuid.random().toString(),
    name: String = "Test",
    ean: String? = null,
    updatedAt: Long = getCurrentTime(),
    brand: String? = null,
    isDeleted: Boolean = false,
    measureUnit: MeasureUnit = MeasureUnit.UNITY,
    manuallyAdded: Boolean = true,
    netWeight: Double = 1.0,
    thumbnailUrl: String? = null
) = CatalogProduct(
    id = id,
    name = name,
    brand = brand,
    measureUnit = measureUnit,
    netWeight = netWeight,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    manuallyAdded = manuallyAdded,
    ean = ean,
    thumbnailUrl = thumbnailUrl
)
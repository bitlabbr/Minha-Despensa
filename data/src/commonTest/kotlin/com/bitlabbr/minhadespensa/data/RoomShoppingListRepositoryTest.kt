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
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomShoppingListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class RoomShoppingListRepositoryTest : BaseTest() {
    private lateinit var db: AppDatabase
    private lateinit var catalogRepository: RoomCatalogRepository
    private lateinit var shoppingListRepository: RoomShoppingListRepository
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        catalogRepository = RoomCatalogRepository(db, logger)
        shoppingListRepository = RoomShoppingListRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    @Test
    fun `should finalize purchase moving items to pantry and resetting list for future use`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 3.0, measureUnit = MeasureUnit.LITER)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val item = createDummyShoppingItem(productId = product.id, listId = listId, quantity = 5.0, priceAtTime = 1000)
        val shoppingList = createDummyShoppingList(id = listId, name = "Lista Mensal", items = listOf(item))

        shoppingListRepository.insertShoppingList(shoppingList)

        shoppingListRepository.toggleItemCheck(item.id, true)
        val startTime = getCurrentTime()
        shoppingListRepository.finalizePurchase(listId)

        val pantryItems = db.pantryDao().getAllActive().first()
        assertEquals(1, pantryItems.size)
        assertEquals(product.id, pantryItems[0].productId)
        assertEquals(5.0, pantryItems[0].quantity)

        val prices = db.priceDao().getHistoryByProduct(product.id).first()
        assertEquals(1, prices.size)
        assertEquals(1000L, prices[0].priceInCents)

        val updatedList = shoppingListRepository.getShoppingListById(listId).first()
        assertNotNull(updatedList)
        assertFalse(updatedList.items[0].isChecked, "O item deveria ter sido desmarcado após o checkout")
        assertTrue(updatedList.updatedAt >= startTime, "O timestamp da lista deveria ter sido atualizado")
    }

    @Test
    fun `should allow product substitution in shopping item during market trip`() = runTest {
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

        val listId = Uuid.random().toString()
        val listItem = createDummyShoppingItem(productId = productA.id, listId = listId, quantity = 1.0)
        shoppingListRepository.insertShoppingList(
            createDummyShoppingList(
                id = listId,
                name = "Lista do mês",
                items = listOf(listItem)
            )
        )
        val updatedItem = listItem.copy(productId = productB.id, updatedAt = getCurrentTime(), quantity = 10.0)
        shoppingListRepository.updateShoppingItemIfNewer(updatedItem)
        val list = shoppingListRepository.getShoppingListById(listId).first()
        assertEquals(productB.id, list?.items?.get(0)?.productId, "O item deveria ter sido atualizado para o Produto B")
        assertEquals(10.0, list?.items?.get(0)?.quantity, "O item deveria ter sido atualizado para o Produto B")
    }

    @Test
    fun `should delete all items via CASCADE when shopping list is physically deleted`() = runTest {
        val product = createDummyCatalogProduct(measureUnit = MeasureUnit.LITER, netWeight = 1.0)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val item = createDummyShoppingItem(productId = product.id, listId = listId, quantity = 2.0)
        shoppingListRepository.insertShoppingList(
            createDummyShoppingList(
                id = listId,
                items = listOf(item)
            )
        )

        shoppingListRepository.deleteShoppingListById(listId)

        val itemsInDb = db.shoppingItemDao().getActiveItems().first()
        assertTrue(itemsInDb.isEmpty(), "Os itens deveriam ter sido removidos pelo CASCADE")
    }

    @Test
    fun `should NOT return a shopping list marked as DELETED`() = runTest {
        val listId1 = Uuid.random().toString()
        val listId2 = Uuid.random().toString()
        val list1 = createDummyShoppingList(id = listId1, name = "Groceries")
        val list2 = createDummyShoppingList(id = listId2, name = "Hardware Store")

        shoppingListRepository.insertShoppingList(list1)
        shoppingListRepository.insertShoppingList(list2)

        val deleteTime = getCurrentTime()
        shoppingListRepository.markShoppingListAsDeleted(listId1, deleteTime)

        shoppingListRepository.getAllActiveShoppingLists().test {
            val activeLists = awaitItem()
            assertEquals(1, activeLists.size, "Active lists should contain exactly one item")
            assertEquals(listId2, activeLists[0].id, "The remaining list should be the one not deleted")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should do nothing and keep state when finalizing purchase with NO checked items`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 1.0, measureUnit = MeasureUnit.UNITY)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val item = createDummyShoppingItem(productId = product.id, listId = listId, quantity = 10.0, isChecked = false)
        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listId, items = listOf(item)))

        shoppingListRepository.finalizePurchase(listId)

        val pantryItems = db.pantryDao().getAllActive().first()
        assertTrue(pantryItems.isEmpty(), "Pantry should be empty because no items were checked")

        val prices = db.priceDao().getHistoryByProduct(product.id).first()
        assertTrue(prices.isEmpty(), "Price history should not be created for unchecked items")
    }

    @Test
    fun `should return null when getting a non-existent shopping list by ID`() = runTest {
        val randomId = Uuid.random().toString()
        shoppingListRepository.getShoppingListById(randomId).test {
            val result = awaitItem()
            assertNull(result, "Repository should return null for non-existent IDs")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should update shopping list basic details name and budget`() = runTest {
        val listId = Uuid.random().toString()
        val initialList = createDummyShoppingList(id = listId, name = "Old Name", budgetInCents = 1000)
        shoppingListRepository.insertShoppingList(initialList)

        val updatedList = initialList.copy(name = "New Name", budgetInCents = 5000, updatedAt = getCurrentTime())
        shoppingListRepository.updateShoppingListIfNewer(updatedList)

        val result = shoppingListRepository.getShoppingListById(listId).first()
        assertEquals("New Name", result?.name)
        assertEquals(5000L, result?.budgetInCents)
    }

    @Test
    fun `should DELETE a shopping ITEM within a LIST`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 1.0, measureUnit = MeasureUnit.UNITY)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val item1ID = Uuid.random().toString()
        val item2ID = Uuid.random().toString()
        val item1 = createDummyShoppingItem(id = item1ID, productId = product.id, listId = listId, quantity = 1.0)
        val item2 = createDummyShoppingItem(id = item2ID, productId = product.id, listId = listId, quantity = 2.0)

        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listId, items = listOf(item1, item2)))

        shoppingListRepository.markAsDeleted(item1ID)

        val updatedList = shoppingListRepository.getShoppingListById(listId).first()
        val activeItems = updatedList?.items?.filter { !it.isDeleted } ?: emptyList()
        assertEquals(1, activeItems.size)
        assertEquals(item2ID, activeItems[0].id)
    }

    @Test
    fun `should NOT affect items in other lists when finalizing a specific purchase`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 1.0, measureUnit = MeasureUnit.UNITY)
        catalogRepository.insertProduct(product, null)

        val listIdA = Uuid.random().toString()
        val listIdB = Uuid.random().toString()

        val itemAId = Uuid.random().toString()
        val itemBId = Uuid.random().toString()

        val itemA = createDummyShoppingItem(id = itemAId, productId = product.id, listId = listIdA, quantity = 1.0, isChecked = true)
        val itemB = createDummyShoppingItem(id = itemBId, productId = product.id, listId = listIdB, quantity = 5.0, isChecked = true)

        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listIdA, items = listOf(itemA)))
        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listIdB, items = listOf(itemB)))

        shoppingListRepository.finalizePurchase(listIdA)

        val pantryItems = db.pantryDao().getAllActive().first()
        assertEquals(1, pantryItems.size)
        assertEquals(1.0, pantryItems[0].quantity, "Only items from List A should be moved to pantry")

        val listB = shoppingListRepository.getShoppingListById(listIdB).first()
        assertEquals(listB?.items?.first()?.isChecked, true, "Items in List B should remain checked and untouched")
    }

    @Test
    fun `should move ONLY checked items to pantry and leave others untouched in the list`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 1.0, measureUnit = MeasureUnit.UNITY)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val checkedId = Uuid.random().toString()
        val uncheckedId = Uuid.random().toString()
        val checkedItem = createDummyShoppingItem(id = checkedId, productId = product.id, listId = listId, quantity = 2.0, isChecked = true)
        val uncheckedItem = createDummyShoppingItem(id = uncheckedId, productId = product.id, listId = listId, quantity = 10.0, isChecked = false)

        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listId, items = listOf(checkedItem, uncheckedItem)))

        shoppingListRepository.finalizePurchase(listId)

        val pantryItems = db.pantryDao().getAllActive().first()
        assertEquals(2.0, pantryItems[0].quantity)

        val updatedList = shoppingListRepository.getShoppingListById(listId).first()
        val remainingItem = updatedList?.items?.find { it.id == uncheckedId }
        assertNotNull(remainingItem)
        assertFalse(remainingItem.isChecked, "The unchecked item should remain in the list as unchecked")
        assertEquals(10.0, remainingItem.quantity)
    }

    @Test
    fun `should FAIL when inserting a shopping item with a non-existent product ID`() = runTest {
        val listId = Uuid.random().toString()
        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listId))

        val ghostItem = createDummyShoppingItem(
            productId = "non-existent-id",
            listId = listId,
            quantity = 1.0
        )
        assertFails {
            shoppingListRepository.insertShoppingItem(ghostItem)
        }
    }

    @Test
    fun `should ignore updates if the incoming timestamp is older than local timestamp`() = runTest {
        val listId = Uuid.random().toString()
        val olderRemoteTime = getCurrentTime()
        val localTime = getCurrentTime() + 100
        val localList = createDummyShoppingList(id = listId, name = "Local Version", updatedAt = localTime)
        shoppingListRepository.insertShoppingList(localList)

        val remoteList = localList.copy(name = "Older Remote Version", updatedAt = olderRemoteTime)

        shoppingListRepository.updateShoppingListIfNewer(remoteList)

        val result = shoppingListRepository.getShoppingListById(listId).first()
        assertEquals("Local Version", result?.name, "Sync should not overwrite newer local data with older remote data")
    }

    @Test
    fun `should maintain isDeleted state even if a sync update tries to reset it incorrectly`() = runTest {
        val product = createDummyCatalogProduct(netWeight = 1.0, measureUnit = MeasureUnit.UNITY)
        catalogRepository.insertProduct(product, null)

        val listId = Uuid.random().toString()
        val itemId = Uuid.random().toString()
        val deletedItem = createDummyShoppingItem(id = itemId, productId = product.id, listId = listId, quantity = 1.0)

        shoppingListRepository.insertShoppingList(createDummyShoppingList(id = listId, items = listOf(deletedItem)))
        shoppingListRepository.markAsDeleted(itemId)

        val syncUpdate = deletedItem.copy(isDeleted = false, updatedAt = getCurrentTime() - 1000)
        shoppingListRepository.updateShoppingItemIfNewer(syncUpdate)

        val updatedList = shoppingListRepository.getShoppingListById(listId).first()
        val itemResult = updatedList?.items?.find { it.id == itemId }
        assertEquals(
            itemResult?.isDeleted,
            true,
            "Soft-deleted items should stay deleted during sync unless explicitly restored"
        )
    }

    @Test
    fun `getAllActiveShoppingLists should return lists sorted by updatedAt DESC`() = runTest {
        val list1 = createDummyShoppingList(name = "Old List", updatedAt = getCurrentTime())
        val list2 = createDummyShoppingList(name = "New List", updatedAt = getCurrentTime() + 100)

        shoppingListRepository.insertShoppingList(list1)
        shoppingListRepository.insertShoppingList(list2)

        shoppingListRepository.getAllActiveShoppingLists().test {
            val lists = awaitItem()
            assertEquals("New List", lists[0].name, "Newer list should come first")
            assertEquals("Old List", lists[1].name, "Older list should come second")
            cancelAndIgnoreRemainingEvents()
        }
    }

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

    private fun createDummyShoppingItem(
        id: String = Uuid.random().toString(),
        productId: String,
        listId: String,
        quantity: Double,
        isChecked: Boolean = false,
        priceAtTime: Long? = null
    ) = ShoppingItem(
        id = id,
        productId = productId,
        listID = listId,
        quantity = quantity,
        isChecked = isChecked,
        priceAtTime = priceAtTime,
        updatedAt = getCurrentTime(),
        isDeleted = false
    )

    private fun createDummyCatalogProduct(
        id: String = Uuid.random().toString(),
        name: String = "Test",
        ean: String? = null,
        updatedAt: Long = getCurrentTime(),
        brand: String? = null,
        isDeleted: Boolean = false,
        measureUnit: MeasureUnit,
        manuallyAdded: Boolean = true,
        netWeight: Double,
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
}
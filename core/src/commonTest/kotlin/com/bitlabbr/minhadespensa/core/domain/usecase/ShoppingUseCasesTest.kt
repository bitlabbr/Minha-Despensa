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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.fakes.FakeShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShoppingUseCasesTest {

    private val repository = FakeShoppingListRepository()

    private val createQuickListUseCase = CreateQuickShoppingListUseCase(repository)
    private val createPlannedListUseCase = CreatePlannedShoppingListUseCase(repository)
    private val addOrUpdateCartItemUseCase = AddOrUpdateCartItemUseCase(repository)
    private val startShoppingSessionUseCase = StartShoppingSessionUseCase(repository)
    private val finalizeShoppingSessionUseCase = FinalizeShoppingSessionUseCase(repository)
    private val addCatalogItemUseCase = AddCatalogItemToShoppingListUseCase(repository)
    private val replaceCartItemUseCase = ReplaceCartItemUseCase(repository)
    private val removeCartItemUseCase = RemoveCartItemUseCase(repository)

    @Test
    fun `CreateQuickShoppingListUseCase should create scratchpad list with parsed items`() = runTest {
        val rawInput = """
            Banana Nanica
            
            2x Leite Integral
            Detergente Líquido
        """.trimIndent()

        val result = createQuickListUseCase(rawContent = rawInput, customTitle = "Feira do Sábado")
        assertTrue(result.isSuccess)

        val list = result.getOrThrow()
        assertEquals("Feira do Sábado", list.name)
        assertEquals(ShoppingListType.SCRATCHPAD, list.type)
        assertEquals(ShoppingListStatus.DRAFT, list.status)
        assertEquals(3, list.items.size)
        assertNull(list.items[0].productId)
        assertEquals("Banana Nanica", list.items[0].rawText)
    }

    @Test
    fun `CreateQuickShoppingListUseCase should reject blank content`() = runTest {
        val blankResult = createQuickListUseCase(rawContent = "   \n  \n")
        assertTrue(blankResult.isFailure)
    }

    @Test
    fun `CreateQuickShoppingListUseCase should use default title when customTitle is null or blank`() = runTest {
        val resultNullTitle = createQuickListUseCase(rawContent = "Arroz\nFeijão", customTitle = null)
        assertTrue(resultNullTitle.isSuccess)
        assertTrue(resultNullTitle.getOrThrow().name.startsWith("Lista rápida de "))

        val resultBlankTitle = createQuickListUseCase(rawContent = "Arroz\nFeijão", customTitle = "   ")
        assertTrue(resultBlankTitle.isSuccess)
        assertTrue(resultBlankTitle.getOrThrow().name.startsWith("Lista rápida de "))
    }

    @Test
    fun `CreateQuickShoppingListUseCase should save budgetInCents and reject negative budget`() = runTest {
        val result = createQuickListUseCase(
            rawContent = "Arroz\nFeijão",
            customTitle = "Feira com Teto",
            budgetInCents = 15000L,
        )
        assertTrue(result.isSuccess)
        assertEquals(15000L, result.getOrThrow().budgetInCents)

        val negativeResult = createQuickListUseCase(
            rawContent = "Arroz\nFeijão",
            budgetInCents = -100L,
        )
        assertTrue(negativeResult.isFailure)
    }

    @Test
    fun `CreateQuickShoppingListUseCase should reject customTitle exceeding NAME_MAX_LENGTH`() = runTest {
        val longTitle = "A".repeat(CoreConstants.ShoppingList.NAME_MAX_LENGTH + 1)
        val result = createQuickListUseCase(
            rawContent = "Arroz\nFeijão",
            customTitle = longTitle,
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `CreatePlannedShoppingListUseCase should enforce items and store budget in cents`() = runTest {
        val drafts = listOf(
            PlannedItemDraft(productId = "prod-1", quantity = 2.0),
            PlannedItemDraft(productId = "prod-2", quantity = 1.0),
        )

        val result = createPlannedListUseCase(
            name = "Compras Planejadas",
            budgetInCents = 15000L, // 15000 cents (R$ 150.00)
            itemsDraft = drafts,
        )

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(ShoppingListType.PLANNED, list.type)
        assertEquals(15000L, list.budgetInCents)
        assertEquals(2, list.items.size)
        assertEquals("prod-1", list.items[0].productId)
        assertNull(list.items[0].priceAtTime)
    }

    @Test
    fun `CreatePlannedShoppingListUseCase should reject invalid inputs`() = runTest {
        val validDrafts = listOf(PlannedItemDraft(productId = "prod-1", quantity = 1.0))

        // Blank name
        val blankNameResult = createPlannedListUseCase(
            name = "   ",
            itemsDraft = validDrafts,
        )
        assertTrue(blankNameResult.isFailure)

        // Negative budget
        val negativeBudgetResult = createPlannedListUseCase(
            name = "Compras",
            budgetInCents = -500L,
            itemsDraft = validDrafts,
        )
        assertTrue(negativeBudgetResult.isFailure)

        // Empty drafts
        val emptyDraftResult = createPlannedListUseCase(
            name = "Compras",
            itemsDraft = emptyList(),
        )
        assertTrue(emptyDraftResult.isFailure)

        // Invalid item quantity in draft
        val invalidQtyResult = createPlannedListUseCase(
            name = "Compras",
            itemsDraft = listOf(PlannedItemDraft(productId = "prod-1", quantity = 0.0)),
        )
        assertTrue(invalidQtyResult.isFailure)
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should insert new item when existingItemId is null`() = runTest {
        val list = ShoppingList(
            id = "list-1",
            name = "Compras de Teste",
            type = ShoppingListType.ASSISTANT,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        // 1. Insert first scanned item into cart
        val firstAddResult = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 2.0,
            priceAtTimeInCents = 750L, // R$ 7.50
        )
        assertTrue(firstAddResult.isSuccess)

        // 2. Scan the same product again, adding it as a new item into the cart
        val secondAddResult = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTimeInCents = 800L, // R$ 8.00
        )
        assertTrue(secondAddResult.isSuccess)

        val updatedList = repository.getShoppingListById("list-1").first()
        assertNotNull(updatedList)
        assertEquals(2, updatedList.items.size)
        val item1 = updatedList.items[0]
        assertEquals(2.0, item1.quantity)
        assertEquals(750L, item1.priceAtTime)
        assertTrue(item1.isChecked)

        val item2 = updatedList.items[1]
        assertEquals(1.0, item2.quantity)
        assertEquals(800L, item2.priceAtTime)
        assertTrue(item2.isChecked)
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should update existing item by existingItemId`() = runTest {
        val initialItem = ShoppingItem(
            id = "item-edit",
            listId = "list-1",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTime = 500L,
            isChecked = true,
            updatedAt = 1000L,
        )
        val list = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(initialItem),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 4.0, // Replaces quantity when existingItemId is provided
            priceAtTimeInCents = 600L,
            existingItemId = "item-edit",
        )
        assertTrue(result.isSuccess)

        val updatedList = repository.getShoppingListById("list-1").first()
        val item = updatedList?.items?.find { it.id == "item-edit" }
        assertNotNull(item)
        assertEquals(4.0, item.quantity)
        assertEquals(600L, item.priceAtTime)
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should fail when existingItemId does not exist instead of duplicating other item`() = runTest {
        val initialItem = ShoppingItem(
            id = "item-1",
            listId = "list-1",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTime = 500L,
            isChecked = true,
            updatedAt = 1000L,
        )
        val list = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(initialItem),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 10.0,
            priceAtTimeInCents = 999L,
            existingItemId = "invalid-or-ghost-id",
        )
        assertTrue(result.isFailure, "Must fail when existingItemId is not found")

        val currentList = repository.getShoppingListById("list-1").first()
        assertNotNull(currentList)
        assertEquals(1, currentList.items.size, "Must not create a duplicate item")
        assertEquals(1.0, currentList.items.first().quantity, "Must not corrupt existing item")
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should reject negative price or invalid quantity`() = runTest {
        val list = ShoppingList(
            id = "list-1",
            name = "Lista",
            type = ShoppingListType.ASSISTANT,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val negativePrice = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTimeInCents = -100L,
        )
        assertTrue(negativePrice.isFailure)

        val zeroQty = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 0.0,
            priceAtTimeInCents = 100L,
        )
        assertTrue(zeroQty.isFailure)

        // Missing both productId and rawText
        val missingBoth = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = null,
            rawText = "   ",
            quantity = 1.0,
            priceAtTimeInCents = 100L,
        )
        assertTrue(missingBoth.isFailure)
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should support items with only rawText and null price`() = runTest {
        val list = ShoppingList(
            id = "list-cart-raw",
            name = "Lista",
            type = ShoppingListType.ASSISTANT,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = addOrUpdateCartItemUseCase(
            listId = "list-cart-raw",
            productId = null,
            rawText = "Maçã Gala",
            quantity = 2.0,
            priceAtTimeInCents = null,
        )
        assertTrue(result.isSuccess)

        val item = result.getOrThrow()
        assertNull(item.productId)
        assertEquals("Maçã Gala", item.rawText)
        assertNull(item.priceAtTime)
        assertTrue(item.isChecked)
    }

    @Test
    fun `AddOrUpdateCartItemUseCase should reject operations on deleted or non-existent list`() = runTest {
        val deletedList = ShoppingList(
            id = "list-cart-del",
            name = "Lista Excluída",
            type = ShoppingListType.ASSISTANT,
            isDeleted = true,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        // Deleted list
        val onDeletedResult = addOrUpdateCartItemUseCase(
            listId = "list-cart-del",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTimeInCents = 100L,
        )
        assertTrue(onDeletedResult.isFailure)

        // Non-existent list
        val onNonExistentResult = addOrUpdateCartItemUseCase(
            listId = "non-existent-list",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTimeInCents = 100L,
        )
        assertTrue(onNonExistentResult.isFailure)
    }

    @Test
    fun `FinalizeShoppingSessionUseCase should fail on deleted shopping lists`() = runTest {
        val deletedList = ShoppingList(
            id = "list-del",
            name = "Deletada",
            type = ShoppingListType.ASSISTANT,
            isDeleted = true,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        val result = finalizeShoppingSessionUseCase("list-del")
        assertTrue(result.isFailure)
        assertFailsWith<IllegalArgumentException> {
            result.getOrThrow()
        }
        assertNull(repository.purchaseFinalizedListId)
    }

    @Test
    fun `FinalizeShoppingSessionUseCase should fail on non-existent shopping list`() = runTest {
        val result = finalizeShoppingSessionUseCase("non-existent-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `FinalizeShoppingSessionUseCase should trigger repository finalize and mark COMPLETED`() = runTest {
        val activeList = ShoppingList(
            id = "list-ok",
            name = "Finalizando",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(activeList)

        val result = finalizeShoppingSessionUseCase("list-ok")
        assertTrue(result.isSuccess)
        assertEquals("list-ok", repository.purchaseFinalizedListId)

        val completed = repository.getShoppingListById("list-ok").first()
        assertEquals(ShoppingListStatus.COMPLETED, completed?.status)
    }

    @Test
    fun `StartShoppingSessionUseCase should create new ASSISTANT list when existingListId is null`() = runTest {
        val result = startShoppingSessionUseCase(existingListId = null)
        assertTrue(result.isSuccess)

        val list = result.getOrThrow()
        assertEquals(ShoppingListType.ASSISTANT, list.type)
        assertEquals(ShoppingListStatus.SHOPPING, list.status)
        assertTrue(list.name.startsWith("Compras de "))

        val inRepo = repository.getShoppingListById(list.id).first()
        assertNotNull(inRepo)
        assertEquals(ShoppingListStatus.SHOPPING, inRepo.status)
    }

    @Test
    fun `StartShoppingSessionUseCase should update status to SHOPPING for existing list`() = runTest {
        val existingList = ShoppingList(
            id = "list-draft",
            name = "Feira",
            type = ShoppingListType.PLANNED,
            status = ShoppingListStatus.DRAFT,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(existingList)

        val result = startShoppingSessionUseCase(existingListId = "list-draft")
        assertTrue(result.isSuccess)

        val updated = repository.getShoppingListById("list-draft").first()
        assertNotNull(updated)
        assertEquals(ShoppingListStatus.SHOPPING, updated.status)
    }

    @Test
    fun `StartShoppingSessionUseCase should preserve COMPLETED status for already finished list`() = runTest {
        val completedList = ShoppingList(
            id = "list-finished",
            name = "Compras Passadas",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(completedList)

        val result = startShoppingSessionUseCase(existingListId = "list-finished")
        assertTrue(result.isSuccess)

        val retrieved = repository.getShoppingListById("list-finished").first()
        assertNotNull(retrieved)
        assertEquals(ShoppingListStatus.COMPLETED, retrieved.status)
    }

    @Test
    fun `StartShoppingSessionUseCase should fail on deleted or non-existent list`() = runTest {
        val deletedList = ShoppingList(
            id = "list-del",
            name = "Excluída",
            type = ShoppingListType.PLANNED,
            isDeleted = true,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        // Logically deleted list
        val delResult = startShoppingSessionUseCase(existingListId = "list-del")
        assertTrue(delResult.isFailure)

        // Non-existent list
        val notFoundResult = startShoppingSessionUseCase(existingListId = "non-existent")
        assertTrue(notFoundResult.isFailure)
    }

    @Test
    fun `AddCatalogItemToShoppingListUseCase should add item or increment quantity if product exists`() = runTest {
        val list = ShoppingList(
            id = "list-catalog",
            name = "Planejada",
            type = ShoppingListType.PLANNED,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        // 1. Insert new item
        val addResult = addCatalogItemUseCase(listId = "list-catalog", productId = "prod-1", quantity = 2.0)
        assertTrue(addResult.isSuccess)

        val listAfterFirstAdd = repository.getShoppingListById("list-catalog").first()
        assertEquals(1, listAfterFirstAdd?.items?.size)
        assertEquals(2.0, listAfterFirstAdd?.items?.first()?.quantity)

        // 2. Add the same product again (should accumulate quantity: 2.0 + 3.0 = 5.0)
        val secondAddResult = addCatalogItemUseCase(listId = "list-catalog", productId = "prod-1", quantity = 3.0)
        assertTrue(secondAddResult.isSuccess)

        val listAfterSecondAdd = repository.getShoppingListById("list-catalog").first()
        assertEquals(1, listAfterSecondAdd?.items?.size)
        assertEquals(5.0, listAfterSecondAdd?.items?.first()?.quantity)
    }

    @Test
    fun `AddCatalogItemToShoppingListUseCase should reject invalid inputs`() = runTest {
        val list = ShoppingList(
            id = "list-val",
            name = "Lista",
            type = ShoppingListType.PLANNED,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        // Invalid quantity
        val zeroQty = addCatalogItemUseCase(listId = "list-val", productId = "prod-1", quantity = 0.0)
        assertTrue(zeroQty.isFailure)

        // Blank product ID
        val blankProd = addCatalogItemUseCase(listId = "list-val", productId = "   ", quantity = 1.0)
        assertTrue(blankProd.isFailure)

        // Non-existent list
        val noList = addCatalogItemUseCase(listId = "unknown-id", productId = "prod-1", quantity = 1.0)
        assertTrue(noList.isFailure)
    }

    @Test
    fun `AddCatalogItemToShoppingListUseCase should reject deleted list`() = runTest {
        val deletedList = ShoppingList(
            id = "list-del",
            name = "Lista Excluída",
            type = ShoppingListType.PLANNED,
            isDeleted = true,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        val result = addCatalogItemUseCase(listId = "list-del", productId = "prod-1", quantity = 1.0)
        assertTrue(result.isFailure)
    }

    @Test
    fun `AddCatalogItemToShoppingListUseCase should create new item if existing item with same product is deleted`() = runTest {
        val deletedItem = ShoppingItem(
            id = "item-deleted",
            listId = "list-with-del-item",
            productId = "prod-1",
            quantity = 2.0,
            isDeleted = true,
            updatedAt = 1000L,
        )
        val list = ShoppingList(
            id = "list-with-del-item",
            name = "Lista",
            type = ShoppingListType.PLANNED,
            items = listOf(deletedItem),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = addCatalogItemUseCase(listId = "list-with-del-item", productId = "prod-1", quantity = 1.0)
        assertTrue(result.isSuccess)

        val updatedList = repository.getShoppingListById("list-with-del-item").first()
        assertEquals(2, updatedList?.items?.size)
        val activeItem = updatedList?.items?.find { !it.isDeleted }
        assertNotNull(activeItem)
        assertEquals(1.0, activeItem.quantity)
        assertEquals("prod-1", activeItem.productId)
    }

    @Test
    fun `ReplaceCartItemUseCase should replace free-text item with catalog product and set price`() = runTest {
        val initialItem = ShoppingItem(
            id = "item-text-milk",
            listId = "list-replace-1",
            productId = null,
            rawText = "milk",
            quantity = 2.0,
            priceAtTime = null,
            isChecked = false,
            updatedAt = 1000L,
        )
        val list = ShoppingList(
            id = "list-replace-1",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(initialItem),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = replaceCartItemUseCase(
            listId = "list-replace-1",
            itemId = "item-text-milk",
            newProductId = "prod-ninho",
            newRawText = "Leite Ninho 1L",
            quantity = 3.0,
            priceAtTimeInCents = 550L,
        )
        assertTrue(result.isSuccess)

        val updated = result.getOrThrow()
        assertEquals("prod-ninho", updated.productId)
        assertEquals("Leite Ninho 1L", updated.rawText)
        assertEquals(3.0, updated.quantity)
        assertEquals(550L, updated.priceAtTime)
        assertTrue(updated.isChecked)

        val listInRepo = repository.getShoppingListById("list-replace-1").first()
        val itemInRepo = listInRepo?.items?.find { it.id == "item-text-milk" }
        assertNotNull(itemInRepo)
        assertEquals("prod-ninho", itemInRepo.productId)
        assertEquals("Leite Ninho 1L", itemInRepo.rawText)
        assertEquals(3.0, itemInRepo.quantity)
        assertEquals(550L, itemInRepo.priceAtTime)
    }

    @Test
    fun `ReplaceCartItemUseCase should substitute catalog product A with catalog product B preserving quantity if null`() = runTest {
        val initialItem = ShoppingItem(
            id = "item-prod-a",
            listId = "list-replace-2",
            productId = "prod-brand-A",
            rawText = "Leite A",
            quantity = 2.0,
            priceAtTime = 400L,
            isChecked = true,
            updatedAt = 1000L,
        )
        val list = ShoppingList(
            id = "list-replace-2",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(initialItem),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = replaceCartItemUseCase(
            listId = "list-replace-2",
            itemId = "item-prod-a",
            newProductId = "prod-brand-B",
            newRawText = "Leite B",
            quantity = null, // preserves 2.0
            priceAtTimeInCents = 480L,
        )
        assertTrue(result.isSuccess)

        val updated = result.getOrThrow()
        assertEquals("prod-brand-B", updated.productId)
        assertEquals("Leite B", updated.rawText)
        assertEquals(2.0, updated.quantity)
        assertEquals(480L, updated.priceAtTime)

        val listInRepo = repository.getShoppingListById("list-replace-2").first()
        val itemInRepo = listInRepo?.items?.find { it.id == "item-prod-a" }
        assertNotNull(itemInRepo)
        assertEquals("prod-brand-B", itemInRepo.productId)
        assertEquals(2.0, itemInRepo.quantity)
        assertEquals(480L, itemInRepo.priceAtTime)
    }

    @Test
    fun `ReplaceCartItemUseCase should fail when item does not exist or list is deleted`() = runTest {
        val list = ShoppingList(
            id = "list-replace-fail",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            isDeleted = false,
            items = emptyList(),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val resultNonExistent = replaceCartItemUseCase(
            listId = "list-replace-fail",
            itemId = "ghost-item",
            newProductId = "prod-x",
        )
        assertTrue(resultNonExistent.isFailure)

        val deletedList = ShoppingList(
            id = "list-replace-deleted",
            name = "Lista Excluída",
            type = ShoppingListType.ASSISTANT,
            isDeleted = true,
            items = listOf(
                ShoppingItem(
                    id = "item-in-del",
                    listId = "list-replace-deleted",
                    rawText = "Item",
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        val resultOnDeletedList = replaceCartItemUseCase(
            listId = "list-replace-deleted",
            itemId = "item-in-del",
            newProductId = "prod-x",
        )
        assertTrue(resultOnDeletedList.isFailure)
    }

    @Test
    fun `ReplaceCartItemUseCase should fail when list is already completed`() = runTest {
        val completedList = ShoppingList(
            id = "list-replace-completed",
            name = "Compras Finalizadas",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            items = listOf(
                ShoppingItem(
                    id = "item-comp-1",
                    listId = "list-replace-completed",
                    rawText = "Leite",
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(completedList)

        val result = replaceCartItemUseCase(
            listId = "list-replace-completed",
            itemId = "item-comp-1",
            newProductId = "prod-new",
        )
        assertTrue(result.isFailure)
        assertEquals("Não é possível substituir itens em uma lista já finalizada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `RemoveCartItemUseCase should mark item as deleted successfully`() = runTest {
        val list = ShoppingList(
            id = "list-remove-1",
            name = "Lista de Teste",
            type = ShoppingListType.ASSISTANT,
            items = listOf(
                ShoppingItem(
                    id = "item-rem-1",
                    listId = "list-remove-1",
                    rawText = "Sabonete",
                    updatedAt = 1000L,
                ),
                ShoppingItem(
                    id = "item-rem-2",
                    listId = "list-remove-1",
                    rawText = "Shampoo",
                    updatedAt = 1000L,
                ),
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        val result = removeCartItemUseCase(
            listId = "list-remove-1",
            itemId = "item-rem-1",
        )
        assertTrue(result.isSuccess)

        val updatedList = repository.getShoppingListById("list-remove-1").first()
        assertNotNull(updatedList)
        val removedItem = updatedList.items.find { it.id == "item-rem-1" }
        assertNotNull(removedItem)
        assertTrue(removedItem.isDeleted)

        val activeItem = updatedList.items.find { it.id == "item-rem-2" }
        assertNotNull(activeItem)
        assertEquals(false, activeItem.isDeleted)
    }

    @Test
    fun `RemoveCartItemUseCase should fail when list does not exist`() = runTest {
        val result = removeCartItemUseCase(
            listId = "non-existent-list",
            itemId = "item-1",
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `RemoveCartItemUseCase should fail when list is deleted or item not found`() = runTest {
        val deletedList = ShoppingList(
            id = "list-remove-del",
            name = "Lista Deletada",
            type = ShoppingListType.ASSISTANT,
            isDeleted = true,
            items = listOf(
                ShoppingItem(
                    id = "item-in-del-list",
                    listId = "list-remove-del",
                    rawText = "Item",
                    updatedAt = 1000L,
                ),
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        val resultDeletedList = removeCartItemUseCase("list-remove-del", "item-in-del-list")
        assertTrue(resultDeletedList.isFailure)

        val activeList = ShoppingList(
            id = "list-remove-item-not-found",
            name = "Lista Ativa",
            type = ShoppingListType.ASSISTANT,
            items = listOf(
                ShoppingItem(
                    id = "item-already-del",
                    listId = "list-remove-item-not-found",
                    rawText = "Item Já Deletado",
                    isDeleted = true,
                    updatedAt = 1000L,
                ),
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(activeList)

        val resultAlreadyDeleted = removeCartItemUseCase("list-remove-item-not-found", "item-already-del")
        assertTrue(resultAlreadyDeleted.isFailure)

        val resultNotFound = removeCartItemUseCase("list-remove-item-not-found", "ghost-item")
        assertTrue(resultNotFound.isFailure)
    }

    @Test
    fun `RemoveCartItemUseCase should fail when list is already completed`() = runTest {
        val completedList = ShoppingList(
            id = "list-remove-comp",
            name = "Lista Concluída",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            items = listOf(
                ShoppingItem(
                    id = "item-in-comp",
                    listId = "list-remove-comp",
                    rawText = "Item Finalizado",
                    updatedAt = 1000L,
                ),
            ),
            updatedAt = 1000L,
        )
        repository.insertShoppingList(completedList)

        val result = removeCartItemUseCase("list-remove-comp", "item-in-comp")
        assertTrue(result.isFailure)
        assertEquals("Não é possível remover itens de uma lista já finalizada", result.exceptionOrNull()?.message)
    }
}
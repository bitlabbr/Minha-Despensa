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
    fun `CreatePlannedShoppingListUseCase should enforce items and store budget in cents`() = runTest {
        val drafts = listOf(
            PlannedItemDraft(productId = "prod-1", quantity = 2.0),
            PlannedItemDraft(productId = "prod-2", quantity = 1.0),
        )

        val result = createPlannedListUseCase(
            name = "Compras Planejadas",
            budgetInCents = 15000L, // R$ 150,00
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
    fun `AddOrUpdateCartItemUseCase should accumulate quantity and accept price in cents`() = runTest {
        val list = ShoppingList(
            id = "list-1",
            name = "Compras de Teste",
            type = ShoppingListType.ASSISTANT,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(list)

        // 1. Inserir primeiro item bipado no carrinho
        val firstAddResult = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 2.0,
            priceAtTimeInCents = 750L, // R$ 7,50
        )
        assertTrue(firstAddResult.isSuccess)

        // 2. Bipar o mesmo produto novamente somando 1.0 unidade com novo preço
        val secondAddResult = addOrUpdateCartItemUseCase(
            listId = "list-1",
            productId = "prod-1",
            quantity = 1.0,
            priceAtTimeInCents = 800L, // R$ 8,00
        )
        assertTrue(secondAddResult.isSuccess)

        val updatedList = repository.getShoppingListById("list-1").first()
        assertNotNull(updatedList)
        assertEquals(1, updatedList.items.size)
        val item = updatedList.items.first()
        assertEquals(3.0, item.quantity)
        assertEquals(800L, item.priceAtTime)
        assertTrue(item.isChecked)
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
    fun `StartShoppingSessionUseCase should fail on deleted or non-existent list`() = runTest {
        val deletedList = ShoppingList(
            id = "list-del",
            name = "Excluída",
            type = ShoppingListType.PLANNED,
            isDeleted = true,
            updatedAt = 1000L,
        )
        repository.insertShoppingList(deletedList)

        // Lista deletada logicamente
        val delResult = startShoppingSessionUseCase(existingListId = "list-del")
        assertTrue(delResult.isFailure)

        // Lista não existente
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

        // 1. Inserir item novo
        val addResult = addCatalogItemUseCase(listId = "list-catalog", productId = "prod-1", quantity = 2.0)
        assertTrue(addResult.isSuccess)

        val listAfterFirstAdd = repository.getShoppingListById("list-catalog").first()
        assertEquals(1, listAfterFirstAdd?.items?.size)
        assertEquals(2.0, listAfterFirstAdd?.items?.first()?.quantity)

        // 2. Adicionar o mesmo produto novamente (deve somar quantidade: 2.0 + 3.0 = 5.0)
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

        // Quantidade inválida
        val zeroQty = addCatalogItemUseCase(listId = "list-val", productId = "prod-1", quantity = 0.0)
        assertTrue(zeroQty.isFailure)

        // Product ID vazio
        val blankProd = addCatalogItemUseCase(listId = "list-val", productId = "   ", quantity = 1.0)
        assertTrue(blankProd.isFailure)

        // Lista inexistente
        val noList = addCatalogItemUseCase(listId = "unknown-id", productId = "prod-1", quantity = 1.0)
        assertTrue(noList.isFailure)
    }
}
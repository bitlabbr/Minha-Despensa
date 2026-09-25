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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.core.domain.usecase.AddOrUpdateCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.FinalizeShoppingSessionUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.StartShoppingSessionUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeShoppingListRepository
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantSubFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingAssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shoppingListRepository: FakeShoppingListRepository
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var startShoppingSessionUseCase: StartShoppingSessionUseCase
    private lateinit var addOrUpdateCartItemUseCase: AddOrUpdateCartItemUseCase
    private lateinit var finalizeShoppingSessionUseCase: FinalizeShoppingSessionUseCase
    private lateinit var checkEanStatusUseCase: CheckEanStatusUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var viewModel: ShoppingAssistantViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingListRepository = FakeShoppingListRepository()
        catalogRepository = FakeCatalogRepository()
        startShoppingSessionUseCase = StartShoppingSessionUseCase(shoppingListRepository)
        addOrUpdateCartItemUseCase = AddOrUpdateCartItemUseCase(shoppingListRepository)
        finalizeShoppingSessionUseCase = FinalizeShoppingSessionUseCase(shoppingListRepository)
        checkEanStatusUseCase = CheckEanStatusUseCase(catalogRepository)
        logger = FakeAppLogger()

        viewModel = ShoppingAssistantViewModel(
            startShoppingSessionUseCase = startShoppingSessionUseCase,
            addOrUpdateCartItemUseCase = addOrUpdateCartItemUseCase,
            finalizeShoppingSessionUseCase = finalizeShoppingSessionUseCase,
            checkEanStatusUseCase = checkEanStatusUseCase,
            shoppingListRepository = shoppingListRepository,
            catalogRepository = catalogRepository,
            logger = logger,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession with existing list should load items and calculate total`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod1 = CatalogProduct(id = "p1", name = "Arroz 5kg", updatedAt = 1000L)
        catalogRepository.insertProduct(prod1, null)

        val list = ShoppingList(
            id = "list-assistant-1",
            name = "Compras do Mês",
            type = ShoppingListType.PLANNED,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "i1",
                    listId = "list-assistant-1",
                    productId = "p1",
                    quantity = 2.0,
                    priceAtTime = 2500L, // R$ 25,00
                    isChecked = true,
                    updatedAt = 1000L,
                ),
                ShoppingItem(
                    id = "i2",
                    listId = "list-assistant-1",
                    productId = null,
                    rawText = "Chocolate",
                    quantity = 1.0,
                    priceAtTime = 1000L, // R$ 10,00
                    isChecked = false,
                    updatedAt = 1000L,
                ),
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-assistant-1")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("list-assistant-1", state.listId)
        assertEquals("Compras do Mês", state.listTitle)
        assertEquals(2, state.items.size)
        assertEquals(1, state.checkedCount)
        assertEquals(2, state.totalCount)
        // Checked item is 2 * 2500 = 5000L
        assertEquals(5000L, state.totalCartValueInCents)
    }

    @Test
    fun `toggleItemChecked should update item checked state and recalculate total`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-assistant-2",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "i1",
                    listId = "list-assistant-2",
                    quantity = 1.0,
                    priceAtTime = 1500L,
                    isChecked = false,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-assistant-2")
        testScheduler.advanceUntilIdle()
        assertEquals(0L, viewModel.uiState.value.totalCartValueInCents)

        viewModel.onToggleItemChecked("i1", true)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.checkedCount)
        assertEquals(1500L, state.totalCartValueInCents)
    }

    @Test
    fun `barcode scan subflows should handle registered and unregistered products`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-session",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)
        viewModel.startSession("list-session")
        testScheduler.advanceUntilIdle()

        val prod = CatalogProduct(id = "p-ean", name = "Café", ean = "7891234567890", updatedAt = 1000L)
        catalogRepository.insertProduct(prod, null)

        viewModel.onScanBarcodeClicked()
        testScheduler.advanceUntilIdle()
        assertEquals(ShoppingAssistantSubFlow.BarcodeScanner, viewModel.uiState.value.activeSubFlow)

        // 1. Scan registered product -> AddItemDetails
        viewModel.onBarcodeScanned("7891234567890")
        testScheduler.advanceUntilIdle()

        val subflow1 = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.AddItemDetails>(subflow1)
        assertEquals("p-ean", subflow1.product?.id)

        // 2. Scan unregistered product -> CreateProduct
        viewModel.onBarcodeScanned("7890000000000")
        testScheduler.advanceUntilIdle()

        val subflow2 = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.CreateProduct>(subflow2)
        assertEquals("7890000000000", subflow2.initialEan)

        // 3. Close subflow
        viewModel.onCloseSubFlow()
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.activeSubFlow)
    }

    @Test
    fun `onFinalizePurchase should invoke finalize usecase and trigger completion callback`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-finalize",
            name = "Compras Concluir",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-finalize")
        testScheduler.advanceUntilIdle()

        var finished = false
        viewModel.onFinalizePurchase {
            finished = true
        }

        testScheduler.advanceUntilIdle()

        assertTrue(finished)
        assertEquals("list-finalize", shoppingListRepository.purchaseFinalizedListId)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isFinalizing)
    }

    @Test
    fun `onConfirmItemDetails with price should update item values and increment total cart value without duplicating`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod = CatalogProduct(id = "p-rice", name = "Arroz", updatedAt = 1000L)
        catalogRepository.insertProduct(prod, null)

        val list = ShoppingList(
            id = "list-cart-calc",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "item-rice-1",
                    listId = "list-cart-calc",
                    productId = "p-rice",
                    quantity = 1.0,
                    priceAtTime = null,
                    isChecked = true,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-cart-calc")
        testScheduler.advanceUntilIdle()

        // Before setting price: subtotal = 0, cart total = 0
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(0L, viewModel.uiState.value.items.first().subtotalInCents)
        assertEquals(0L, viewModel.uiState.value.totalCartValueInCents)

        // User edits item to quantity = 2.0, unit price = 550 cents (R$ 5,50)
        viewModel.onConfirmItemDetails(
            product = prod,
            rawText = null,
            quantity = 2.0,
            priceInCents = 550L,
            existingItemId = "item-rice-1",
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size, "Item must not be duplicated")
        val item = state.items.first()
        assertEquals(2.0, item.quantity)
        assertEquals(550L, item.priceAtTime)
        assertEquals(1100L, item.subtotalInCents, "Subtotal must be 2 * 550 = 1100")
        assertEquals(1100L, state.totalCartValueInCents, "Total cart value must increment to 1100")
    }

    @Test
    fun `soft deleted items should be excluded from assistant uiState items`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-deleted-test",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "i-active",
                    listId = "list-deleted-test",
                    rawText = "Ativo",
                    quantity = 1.0,
                    isChecked = true,
                    updatedAt = 1000L,
                    isDeleted = false,
                ),
                ShoppingItem(
                    id = "i-deleted",
                    listId = "list-deleted-test",
                    rawText = "Excluído",
                    quantity = 1.0,
                    isChecked = true,
                    updatedAt = 1000L,
                    isDeleted = true,
                ),
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-deleted-test")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("i-active", state.items.first().id)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `discardSession should delete shopping list and call onFinished`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-discard",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-discard")
        testScheduler.advanceUntilIdle()

        assertNotNull(shoppingListRepository.getShoppingListById("list-discard").first())

        var callbackCalled = false
        viewModel.discardSession {
            callbackCalled = true
        }
        testScheduler.advanceUntilIdle()

        assertTrue(callbackCalled)
        assertNull(shoppingListRepository.getShoppingListById("list-discard").first())
    }

    @Test
    fun `isCompleted should be true when starting session with completed list`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val completedList = ShoppingList(
            id = "list-comp",
            name = "Compras Finalizadas",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(completedList)

        viewModel.startSession("list-comp")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isCompleted)
    }

    @Test
    fun `direct shopping session should have isDirectShopping true and hasChanges false initially`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.startSession(null)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isDirectShopping)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `direct shopping session should set hasChanges true when item is added`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.startSession(null)
        testScheduler.advanceUntilIdle()

        val prod = CatalogProduct(id = "p-direct", name = "Sabão", updatedAt = 1000L)
        catalogRepository.insertProduct(prod, null)

        viewModel.onConfirmItemDetails(
            product = prod,
            rawText = null,
            quantity = 1.0,
            priceInCents = 1500L,
            existingItemId = null,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isDirectShopping)
        assertTrue(state.hasChanges)
    }

    @Test
    fun `existing list session should have isDirectShopping false and hasChanges false initially`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-exist",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "i1",
                    listId = "list-exist",
                    rawText = "Pão",
                    quantity = 1.0,
                    isChecked = false,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-exist")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDirectShopping)
        assertFalse(state.hasChanges)

        // Toggle item check -> hasChanges should become true
        viewModel.onToggleItemChecked("i1", true)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasChanges)

        // Revert check back -> hasChanges should become false
        viewModel.onToggleItemChecked("i1", false)
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hasChanges)
    }
}

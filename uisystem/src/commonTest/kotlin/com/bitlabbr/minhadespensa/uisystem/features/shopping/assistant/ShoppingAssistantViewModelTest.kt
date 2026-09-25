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
import com.bitlabbr.minhadespensa.core.domain.usecase.RemoveCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ReplaceCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.StartShoppingSessionUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeShoppingListRepository
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.CartItemUiModel
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
    private lateinit var replaceCartItemUseCase: ReplaceCartItemUseCase
    private lateinit var removeCartItemUseCase: RemoveCartItemUseCase
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
        replaceCartItemUseCase = ReplaceCartItemUseCase(shoppingListRepository)
        removeCartItemUseCase = RemoveCartItemUseCase(shoppingListRepository)
        logger = FakeAppLogger()

        viewModel = ShoppingAssistantViewModel(
            startShoppingSessionUseCase = startShoppingSessionUseCase,
            addOrUpdateCartItemUseCase = addOrUpdateCartItemUseCase,
            finalizeShoppingSessionUseCase = finalizeShoppingSessionUseCase,
            checkEanStatusUseCase = checkEanStatusUseCase,
            replaceCartItemUseCase = replaceCartItemUseCase,
            removeCartItemUseCase = removeCartItemUseCase,
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

    @Test
    fun `scanning and confirming product details should add a new item to the list rather than updating existing items`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod = CatalogProduct(id = "p-scanned", name = "Pão", updatedAt = 1000L)
        catalogRepository.insertProduct(prod, null)

        val list = ShoppingList(
            id = "list-scan-add",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "item-existing-pao",
                    listId = "list-scan-add",
                    rawText = "Pão",
                    quantity = 1.0,
                    isChecked = false,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-scan-add")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)

        // User scans barcode and confirms product (existingItemId is null)
        viewModel.onConfirmItemDetails(
            product = prod,
            rawText = null,
            quantity = 2.0,
            priceInCents = 600L,
            existingItemId = null,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size, "Scanned product must be added as a new item, not update existing item")
        assertEquals(2, state.totalCount)

        val existingItem = state.items.find { it.id == "item-existing-pao" }
        assertNotNull(existingItem)
        assertEquals(1.0, existingItem.quantity)
        assertFalse(existingItem.isChecked)

        val newItem = state.items.find { it.id != "item-existing-pao" }
        assertNotNull(newItem)
        assertEquals("p-scanned", newItem.productId)
        assertEquals(2.0, newItem.quantity)
        assertEquals(600L, newItem.priceAtTime)
        assertTrue(newItem.isChecked)
    }

    @Test
    fun `onReplaceItemClicked should open ReplaceItemOptions subflow`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val cartItem = CartItemUiModel(
            id = "item-1",
            productId = null,
            displayName = "Leite",
            quantity = 2.0,
            priceAtTime = null,
            isChecked = false,
            subtotalInCents = 0,
        )

        viewModel.onReplaceItemClicked(cartItem)
        testScheduler.advanceUntilIdle()

        val active = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.ReplaceItemOptions>(active)
        assertEquals("item-1", active.item.id)
    }

    @Test
    fun `onStartReplaceBarcodeScan and onStartReplaceCatalogSearch transitions`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val cartItem = CartItemUiModel(
            id = "item-1",
            productId = null,
            displayName = "Leite",
            quantity = 2.0,
            priceAtTime = null,
            isChecked = false,
            subtotalInCents = 0,
        )

        viewModel.onStartReplaceBarcodeScan(cartItem)
        testScheduler.advanceUntilIdle()
        val scanFlow = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.ReplaceItemBarcodeScanner>(scanFlow)
        assertEquals("item-1", scanFlow.item.id)

        viewModel.onStartReplaceCatalogSearch(cartItem)
        testScheduler.advanceUntilIdle()
        val searchFlow = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.SearchCatalogForReplacement>(searchFlow)
        assertEquals("item-1", searchFlow.item.id)
    }

    @Test
    fun `onBarcodeScannedForReplacement with found product transitions to AddItemDetails with isReplacement true`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod = CatalogProduct(id = "p-leite-b", name = "Leite Marca B", ean = "7891000333123", updatedAt = 1000L)
        catalogRepository.insertProduct(prod, null)

        val cartItem = CartItemUiModel(
            id = "item-1",
            productId = null,
            displayName = "Leite genérico",
            quantity = 3.0,
            priceAtTime = null,
            isChecked = false,
            subtotalInCents = 0,
        )

        viewModel.onBarcodeScannedForReplacement(cartItem, "7891000333123")
        testScheduler.advanceUntilIdle()

        val active = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.AddItemDetails>(active)
        assertEquals("p-leite-b", active.product?.id)
        assertEquals("item-1", active.existingItemId)
        assertEquals(3.0, active.initialQuantity)
        assertTrue(active.isReplacement)
    }

    @Test
    fun `onBarcodeScannedForReplacement with unknown product transitions to CreateProduct with replacingItemId`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val cartItem = CartItemUiModel(
            id = "item-free-text",
            productId = null,
            displayName = "Café",
            quantity = 1.0,
            priceAtTime = null,
            isChecked = false,
            subtotalInCents = 0,
        )

        viewModel.onBarcodeScannedForReplacement(cartItem, "7899999999123")
        testScheduler.advanceUntilIdle()

        val active = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.CreateProduct>(active)
        assertEquals("7899999999123", active.initialEan)
        assertEquals("item-free-text", active.replacingItemId)

        // Then, registering the product transitions to AddItemDetails with isReplacement true
        val createdProd = CatalogProduct(id = "p-cafe-novo", name = "Café Gourmet", ean = "7899999999123", updatedAt = 1000L)
        viewModel.onProductCreatedFromCatalog(createdProd)
        testScheduler.advanceUntilIdle()

        val nextActive = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.AddItemDetails>(nextActive)
        assertEquals("p-cafe-novo", nextActive.product?.id)
        assertEquals("item-free-text", nextActive.existingItemId)
        assertTrue(nextActive.isReplacement)
    }

    @Test
    fun `onReplacementProductSelected transitions to AddItemDetails with isReplacement true`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod = CatalogProduct(id = "p-manteiga", name = "Manteiga Extra", updatedAt = 1000L)
        val cartItem = CartItemUiModel(
            id = "item-butter",
            productId = null,
            displayName = "Manteiga",
            quantity = 2.0,
            priceAtTime = null,
            isChecked = false,
            subtotalInCents = 0,
        )

        viewModel.onReplacementProductSelected(cartItem, prod)
        testScheduler.advanceUntilIdle()

        val active = viewModel.uiState.value.activeSubFlow
        assertIs<ShoppingAssistantSubFlow.AddItemDetails>(active)
        assertEquals("p-manteiga", active.product?.id)
        assertEquals("item-butter", active.existingItemId)
        assertEquals(2.0, active.initialQuantity)
        assertTrue(active.isReplacement)
    }

    @Test
    fun `onConfirmItemDetails with isReplacement true replaces free-text item with catalog product and updates price`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prodSub = CatalogProduct(id = "p-leite-ninho", name = "Leite Ninho 1L", updatedAt = 1000L)
        catalogRepository.insertProduct(prodSub, null)

        val list = ShoppingList(
            id = "list-repl-test",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(
                ShoppingItem(
                    id = "item-raw-milk",
                    listId = "list-repl-test",
                    rawText = "milk",
                    quantity = 2.0,
                    isChecked = false,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-repl-test")
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmItemDetails(
            product = prodSub,
            rawText = null,
            quantity = 3.0,
            priceInCents = 650L,
            existingItemId = "item-raw-milk",
            isReplacement = true,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        val replaced = state.items.first()
        assertEquals("item-raw-milk", replaced.id, "ID of item must be preserved")
        assertEquals("p-leite-ninho", replaced.productId)
        assertEquals("Leite Ninho 1L", replaced.displayName)
        assertEquals(3.0, replaced.quantity)
        assertEquals(650L, replaced.priceAtTime)
        assertTrue(replaced.isChecked)
        assertEquals(1950L, state.totalCartValueInCents)
        assertNull(state.activeSubFlow)
    }

    @Test
    fun `onConfirmItemDetails with isReplacement true replaces catalog product A with catalog product B`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prodA = CatalogProduct(id = "p-brand-a", name = "Leite Marca A", updatedAt = 1000L)
        val prodB = CatalogProduct(id = "p-brand-b", name = "Leite Marca B", updatedAt = 1000L)
        catalogRepository.insertProduct(prodA, null)
        catalogRepository.insertProduct(prodB, null)

        val list = ShoppingList(
            id = "list-repl-catalog",
            name = "Compras",
            type = ShoppingListType.ASSISTANT,
            items = listOf(
                ShoppingItem(
                    id = "item-prod-a",
                    listId = "list-repl-catalog",
                    productId = "p-brand-a",
                    quantity = 1.0,
                    priceAtTime = 400L,
                    isChecked = true,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-repl-catalog")
        testScheduler.advanceUntilIdle()

        viewModel.onConfirmItemDetails(
            product = prodB,
            rawText = null,
            quantity = 2.0,
            priceInCents = 450L,
            existingItemId = "item-prod-a",
            isReplacement = true,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        val replaced = state.items.first()
        assertEquals("item-prod-a", replaced.id, "ID of item must be preserved")
        assertEquals("p-brand-b", replaced.productId)
        assertEquals("Leite Marca B", replaced.displayName)
        assertEquals(2.0, replaced.quantity)
        assertEquals(450L, replaced.priceAtTime)
        assertTrue(replaced.isChecked)
        assertEquals(900L, state.totalCartValueInCents)
    }

    @Test
    fun `completed list cannot switch product or be mutated`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-completed-vm",
            name = "Compras Finalizadas",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            items = listOf(
                ShoppingItem(
                    id = "item-comp-1",
                    listId = "list-completed-vm",
                    rawText = "Leite",
                    quantity = 1.0,
                    isChecked = true,
                    updatedAt = 1000L,
                )
            ),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-completed-vm")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isCompleted)

        val cartItem = state.items.first()

        // Attempts to replace or scan or edit should do nothing
        viewModel.onReplaceItemClicked(cartItem)
        assertNull(viewModel.uiState.value.activeSubFlow)

        viewModel.onScanBarcodeClicked()
        assertNull(viewModel.uiState.value.activeSubFlow)

        viewModel.onEditItemClicked(cartItem)
        assertNull(viewModel.uiState.value.activeSubFlow)

        // Toggle check should do nothing
        viewModel.onToggleItemChecked("item-comp-1", false)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.items.first().isChecked)
    }

    @Test
    fun `onUpdateListTitle updates list name in repository and reflects in uiState`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-rename",
            name = "Nome Antigo",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = emptyList(),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-rename")
        testScheduler.advanceUntilIdle()

        assertEquals("Nome Antigo", viewModel.uiState.value.listTitle)

        viewModel.onUpdateListTitle("Nome Novo")
        testScheduler.advanceUntilIdle()

        assertEquals("Nome Novo", viewModel.uiState.value.listTitle)
        val repoList = shoppingListRepository.getShoppingListById("list-rename").first()
        assertEquals("Nome Novo", repoList?.name)
    }

    @Test
    fun `onUpdateListTitle ignores blank or whitespace-only titles`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-rename-blank",
            name = "Nome Original",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = emptyList(),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-rename-blank")
        testScheduler.advanceUntilIdle()

        viewModel.onUpdateListTitle("   ")
        testScheduler.advanceUntilIdle()

        assertEquals("Nome Original", viewModel.uiState.value.listTitle)
    }

    @Test
    fun `onUpdateListTitle does nothing when list is completed`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-rename-completed",
            name = "Lista Finalizada",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.COMPLETED,
            items = emptyList(),
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-rename-completed")
        testScheduler.advanceUntilIdle()

        viewModel.onUpdateListTitle("Tentativa de Renomear")
        testScheduler.advanceUntilIdle()

        assertEquals("Lista Finalizada", viewModel.uiState.value.listTitle)
        val repoList = shoppingListRepository.getShoppingListById("list-rename-completed").first()
        assertEquals("Lista Finalizada", repoList?.name)
    }

    @Test
    fun `onUpdateListDetails updates title and budget in repository and reflects in uiState`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val list = ShoppingList(
            id = "list-details",
            name = "Lista Inicial",
            type = ShoppingListType.ASSISTANT,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(
                    id = "item-budget-1",
                    listId = "list-details",
                    rawText = "Vinho",
                    quantity = 2.0,
                    priceAtTime = 6000L,
                    isChecked = true,
                    updatedAt = 1000L,
                )
            ),
            budgetInCents = 10000L,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.startSession("list-details")
        testScheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals("Lista Inicial", initialState.listTitle)
        assertEquals(10000L, initialState.budgetInCents)
        assertTrue(initialState.isOverBudget)

        // Increase budget to 15000L and rename
        viewModel.onUpdateListDetails("Lista Atualizada", 15000L)
        testScheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals("Lista Atualizada", updatedState.listTitle)
        assertEquals(15000L, updatedState.budgetInCents)
        assertFalse(updatedState.isOverBudget)

        val repoList = shoppingListRepository.getShoppingListById("list-details").first()
        assertEquals("Lista Atualizada", repoList?.name)
        assertEquals(15000L, repoList?.budgetInCents)
    }

    @Test
    fun `ShoppingAssistantUiState computes remainingBudget and budgetProgress correctly`() {
        val stateWithoutBudget = com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantUiState(
            budgetInCents = null,
            totalCartValueInCents = 5000L,
        )
        assertNull(stateWithoutBudget.remainingBudgetInCents)
        assertNull(stateWithoutBudget.budgetProgress)
        assertFalse(stateWithoutBudget.isOverBudget)

        val stateWithinBudget = com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantUiState(
            budgetInCents = 10000L,
            totalCartValueInCents = 4000L,
        )
        assertEquals(6000L, stateWithinBudget.remainingBudgetInCents)
        assertEquals(0.4f, stateWithinBudget.budgetProgress)
        assertFalse(stateWithinBudget.isOverBudget)

        val stateOverBudget = com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantUiState(
            budgetInCents = 10000L,
            totalCartValueInCents = 12000L,
        )
        assertEquals(-2000L, stateOverBudget.remainingBudgetInCents)
        assertEquals(1.2f, stateOverBudget.budgetProgress)
        assertTrue(stateOverBudget.isOverBudget)
    }

    @Test
    fun `onRemoveItem should mark item as deleted and remove from ui items`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val listId = "list-remove-test"
        val item1 = ShoppingItem(
            id = "item-1",
            listId = listId,
            rawText = "Item 1",
            quantity = 2.0,
            priceAtTime = 500L,
            updatedAt = 1000L,
        )
        val item2 = ShoppingItem(
            id = "item-2",
            listId = listId,
            rawText = "Item 2",
            quantity = 1.0,
            priceAtTime = 1000L,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(
            ShoppingList(
                id = listId,
                name = "Lista de Remoção",
                type = ShoppingListType.ASSISTANT,
                items = listOf(item1, item2),
                updatedAt = 1000L,
            ),
        )

        viewModel.startSession(listId)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.items.size)

        viewModel.onRemoveItem("item-1")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("item-2", viewModel.uiState.value.items[0].id)

        val updatedRepoList = shoppingListRepository.getShoppingListById(listId).first()
        val repoItem1 = updatedRepoList?.items?.find { it.id == "item-1" }
        assertTrue(repoItem1?.isDeleted == true)
    }

    @Test
    fun `onRemoveItem should close AddItemDetails subflow if opened for the item`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val listId = "list-subflow-remove"
        val item1 = ShoppingItem(
            id = "item-1",
            listId = listId,
            rawText = "Item Subflow",
            quantity = 1.0,
            priceAtTime = 500L,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(
            ShoppingList(
                id = listId,
                name = "Lista Subflow",
                type = ShoppingListType.ASSISTANT,
                items = listOf(item1),
                updatedAt = 1000L,
            ),
        )

        viewModel.startSession(listId)
        testScheduler.advanceUntilIdle()

        val cartItem = viewModel.uiState.value.items.first()
        viewModel.onEditItemClicked(cartItem)
        testScheduler.advanceUntilIdle()

        assertIs<ShoppingAssistantSubFlow.AddItemDetails>(viewModel.uiState.value.activeSubFlow)

        viewModel.onRemoveItem("item-1")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeSubFlow)
        assertEquals(0, viewModel.uiState.value.items.size)
    }

    @Test
    fun `onRemoveItem should not remove item if list is already completed`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val listId = "list-comp-remove"
        val item1 = ShoppingItem(
            id = "item-1",
            listId = listId,
            rawText = "Item Concluído",
            quantity = 1.0,
            priceAtTime = 500L,
            updatedAt = 1000L,
        )
        shoppingListRepository.insertShoppingList(
            ShoppingList(
                id = listId,
                name = "Lista Concluída",
                type = ShoppingListType.ASSISTANT,
                status = ShoppingListStatus.COMPLETED,
                items = listOf(item1),
                updatedAt = 1000L,
            ),
        )

        viewModel.startSession(listId)
        testScheduler.advanceUntilIdle()

        viewModel.onRemoveItem("item-1")
        testScheduler.advanceUntilIdle()

        val repoList = shoppingListRepository.getShoppingListById(listId).first()
        val repoItem1 = repoList?.items?.find { it.id == "item-1" }
        assertFalse(repoItem1?.isDeleted == true)
    }
}



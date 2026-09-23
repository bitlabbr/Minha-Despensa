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
}

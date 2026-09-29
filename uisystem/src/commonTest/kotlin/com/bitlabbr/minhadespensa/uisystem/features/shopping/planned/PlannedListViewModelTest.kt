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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.planned

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.usecase.CreatePlannedShoppingListUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeShoppingListRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlannedListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shoppingListRepository: FakeShoppingListRepository
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var createPlannedShoppingListUseCase: CreatePlannedShoppingListUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var viewModel: PlannedListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingListRepository = FakeShoppingListRepository()
        catalogRepository = FakeCatalogRepository()
        createPlannedShoppingListUseCase = CreatePlannedShoppingListUseCase(shoppingListRepository)
        logger = FakeAppLogger()
        viewModel = PlannedListViewModel(
            createPlannedShoppingListUseCase = createPlannedShoppingListUseCase,
            catalogRepository = catalogRepository,
            logger = logger,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `budget change should sanitize non-numeric input`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.onBudgetChange("abc150,50xyz")
        testScheduler.advanceUntilIdle()

        assertEquals("150,50", viewModel.uiState.value.budgetInput)
    }

    @Test
    fun `increase and decrease quantity should update selectedQuantities map`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.onIncreaseQuantity("prod-1")
        testScheduler.advanceUntilIdle()
        assertEquals(1.0, viewModel.uiState.value.selectedQuantities["prod-1"])

        viewModel.onIncreaseQuantity("prod-1")
        testScheduler.advanceUntilIdle()
        assertEquals(2.0, viewModel.uiState.value.selectedQuantities["prod-1"])

        viewModel.onDecreaseQuantity("prod-1")
        testScheduler.advanceUntilIdle()
        assertEquals(1.0, viewModel.uiState.value.selectedQuantities["prod-1"])

        // Decreasing at 1.0 removes product from selectedQuantities
        viewModel.onDecreaseQuantity("prod-1")
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedQuantities["prod-1"])
    }

    @Test
    fun `search query should filter available products`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod1 = CatalogProduct(id = "p1", name = "Arroz Tio João", brand = "Tio João", updatedAt = 1000L)
        val prod2 = CatalogProduct(id = "p2", name = "Feijão Camil", brand = "Camil", updatedAt = 1000L)
        catalogRepository.insertProduct(prod1, null)
        catalogRepository.insertProduct(prod2, null)

        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.availableProducts.size)

        viewModel.onSearchQueryChange("Arroz")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.availableProducts.size)
        assertEquals("p1", viewModel.uiState.value.availableProducts.first().id)
    }

    @Test
    fun `savePlannedList should create shopping list with items and budget`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod1 = CatalogProduct(id = "p1", name = "Arroz", updatedAt = 1000L)
        val prod2 = CatalogProduct(id = "p2", name = "Feijão", updatedAt = 1000L)
        catalogRepository.insertProduct(prod1, null)
        catalogRepository.insertProduct(prod2, null)

        viewModel.onTitleChange("Planejamento Mensal")
        viewModel.onBudgetChange("250,00")
        viewModel.onIncreaseQuantity("p1")
        viewModel.onIncreaseQuantity("p2")
        viewModel.onIncreaseQuantity("p2")

        var createdId: String? = null
        viewModel.savePlannedList { id ->
            createdId = id
        }

        testScheduler.advanceUntilIdle()

        assertNotNull(createdId)
        val savedList = shoppingListRepository.getShoppingListById(createdId!!).first()
        assertNotNull(savedList)
        assertEquals("Planejamento Mensal", savedList.name)
        assertEquals(25000L, savedList.budgetInCents)
        assertEquals(2, savedList.items.size)

        val itemP1 = savedList.items.first { it.productId == "p1" }
        assertEquals(1.0, itemP1.quantity)

        val itemP2 = savedList.items.first { it.productId == "p2" }
        assertEquals(2.0, itemP2.quantity)

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.isSuccess)
    }

    @Test
    fun `savePlannedList without items or title should be ignored`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        var called = false
        // 1. Without title
        viewModel.onIncreaseQuantity("p1")
        viewModel.savePlannedList { called = true }
        testScheduler.advanceUntilIdle()
        assertFalse(called)

        // 2. Without items
        viewModel.onTitleChange("Título válido")
        viewModel.onDecreaseQuantity("p1")
        viewModel.savePlannedList { called = true }
        testScheduler.advanceUntilIdle()
        assertFalse(called)
    }
}

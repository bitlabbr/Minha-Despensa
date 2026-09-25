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
 *                        the license, and indicate if changes mandate.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.features.shopping.quicklist

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.usecase.CreateQuickShoppingListUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuickListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shoppingListRepository: FakeShoppingListRepository
    private lateinit var createQuickShoppingListUseCase: CreateQuickShoppingListUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var viewModel: QuickListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingListRepository = FakeShoppingListRepository()
        createQuickShoppingListUseCase = CreateQuickShoppingListUseCase(shoppingListRepository)
        logger = FakeAppLogger()
        viewModel = QuickListViewModel(
            createQuickShoppingListUseCase = createQuickShoppingListUseCase,
            logger = logger,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty fields and cannot save`() {
        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.rawContent)
        assertFalse(state.isSaving)
        assertFalse(state.isSuccess)
        assertFalse(state.canSave)
    }

    @Test
    fun `onTitleChange and onContentChange should update UI state`() {
        viewModel.onTitleChange("Compras de Domingo")
        viewModel.onContentChange("2kg Arroz\n1L Leite")

        val state = viewModel.uiState.value
        assertEquals("Compras de Domingo", state.title)
        assertEquals("2kg Arroz\n1L Leite", state.rawContent)
        assertTrue(state.canSave)
    }

    @Test
    fun `saveQuickList should persist shopping list and notify callback`() = runTest(testDispatcher) {
        viewModel.onTitleChange("Churrasco")
        viewModel.onContentChange("Picanha 1kg\nCarvão 2un")

        var returnedListId: String? = null
        viewModel.saveQuickList { id ->
            returnedListId = id
        }

        testScheduler.advanceUntilIdle()

        assertNotNull(returnedListId)
        val createdList = shoppingListRepository.getShoppingListById(returnedListId!!).first()
        assertNotNull(createdList)
        assertEquals("Churrasco", createdList.name)
        assertEquals(2, createdList.items.size)

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.isSuccess)
    }

    @Test
    fun `saveQuickList with blank content should not trigger save`() = runTest(testDispatcher) {
        var called = false
        viewModel.saveQuickList { called = true }

        testScheduler.advanceUntilIdle()

        assertFalse(called)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `onBudgetChange should filter invalid characters`() {
        viewModel.onBudgetChange("abc120,50xyz")
        assertEquals("120,50", viewModel.uiState.value.budgetInput)
    }

    @Test
    fun `saveQuickList with budget should persist budgetInCents`() = runTest(testDispatcher) {
        viewModel.onTitleChange("Churrasco com Teto")
        viewModel.onContentChange("Picanha 1kg\nCarvão 2un")
        viewModel.onBudgetChange("250,00")

        var returnedListId: String? = null
        viewModel.saveQuickList { id ->
            returnedListId = id
        }

        testScheduler.advanceUntilIdle()

        assertNotNull(returnedListId)
        val createdList = shoppingListRepository.getShoppingListById(returnedListId!!).first()
        assertNotNull(createdList)
        assertEquals(25000L, createdList.budgetInCents)
    }
}

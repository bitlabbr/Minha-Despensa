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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.overview

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shoppingListRepository: FakeShoppingListRepository
    private lateinit var logger: FakeAppLogger
    private lateinit var viewModel: ShoppingListsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shoppingListRepository = FakeShoppingListRepository()
        logger = FakeAppLogger()
        viewModel = ShoppingListsViewModel(
            shoppingListRepository = shoppingListRepository,
            logger = logger,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should separate active lists and completed lists`() = runTest(testDispatcher) {
        val list1 = ShoppingList(
            id = "list-1",
            name = "Compras Semanais",
            type = ShoppingListType.PLANNED,
            status = ShoppingListStatus.SHOPPING,
            items = listOf(
                ShoppingItem(id = "item-1", listId = "list-1", isChecked = true, updatedAt = 1000L),
                ShoppingItem(id = "item-2", listId = "list-1", isChecked = false, updatedAt = 1000L),
            ),
            updatedAt = 1711200000000L,
        )
        val list2 = ShoppingList(
            id = "list-2",
            name = "Compras Concluídas",
            type = ShoppingListType.SCRATCHPAD,
            status = ShoppingListStatus.COMPLETED,
            items = listOf(
                ShoppingItem(id = "item-3", listId = "list-2", isChecked = true, updatedAt = 1000L),
            ),
            updatedAt = 1711200000000L,
        )

        shoppingListRepository.insertShoppingList(list1)
        shoppingListRepository.insertShoppingList(list2)

        viewModel.uiState.test {
            val item = awaitItem()
            val state = if (item.isLoading) awaitItem() else item

            assertFalse(state.isLoading)
            assertEquals(1, state.activeLists.size)
            assertEquals("Compras Semanais", state.activeLists.first().name)
            assertEquals(2, state.activeLists.first().totalItems)
            assertEquals(1, state.activeLists.first().checkedItems)

            assertEquals(1, state.completedLists.size)
            assertEquals("Compras Concluídas", state.completedLists.first().name)
            assertEquals(1, state.completedLists.first().totalItems)
            assertEquals(1, state.completedLists.first().checkedItems)
        }
    }

    @Test
    fun `deleteList should remove shopping list from repository and uiState`() = runTest(testDispatcher) {
        val list1 = ShoppingList(
            id = "list-to-delete",
            name = "Lista Temporária",
            type = ShoppingListType.SCRATCHPAD,
            status = ShoppingListStatus.DRAFT,
            updatedAt = 1711200000000L,
        )
        shoppingListRepository.insertShoppingList(list1)

        viewModel.uiState.test {
            val state1 = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertEquals(1, state1.activeLists.size)

            viewModel.deleteList("list-to-delete")

            val state2 = awaitItem()
            assertTrue(state2.activeLists.isEmpty())
        }
    }

    @Test
    fun `updateListDetails should update list name and budget in repository and uiState`() = runTest(testDispatcher) {
        val list = ShoppingList(
            id = "list-overview-edit",
            name = "Nome Antigo",
            type = ShoppingListType.PLANNED,
            status = ShoppingListStatus.DRAFT,
            budgetInCents = null,
            updatedAt = 1711200000000L,
        )
        shoppingListRepository.insertShoppingList(list)

        viewModel.uiState.test {
            val state1 = awaitItem().let { if (it.isLoading) awaitItem() else it }
            val item1 = state1.activeLists.first { it.id == "list-overview-edit" }
            assertEquals("Nome Antigo", item1.name)
            assertNull(item1.budgetInCents)

            viewModel.updateListDetails("list-overview-edit", "Nome Novo", 50000L)

            val state2 = awaitItem()
            val item2 = state2.activeLists.first { it.id == "list-overview-edit" }
            assertEquals("Nome Novo", item2.name)
            assertEquals(50000L, item2.budgetInCents)
        }

        val updatedRepoList = shoppingListRepository.getShoppingListById("list-overview-edit").first()
        assertEquals("Nome Novo", updatedRepoList?.name)
        assertEquals(50000L, updatedRepoList?.budgetInCents)
    }
}


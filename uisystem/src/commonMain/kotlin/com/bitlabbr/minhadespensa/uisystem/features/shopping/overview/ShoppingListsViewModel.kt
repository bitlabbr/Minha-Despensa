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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.shopping.overview.model.ShoppingListSummaryUiModel
import com.bitlabbr.minhadespensa.uisystem.features.shopping.overview.model.ShoppingListsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ShoppingListsViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val TAG = "ShoppingListsViewModel"

    val uiState: StateFlow<ShoppingListsUiState> = shoppingListRepository.getAllActiveShoppingLists()
        .map { lists ->
            val summaries = lists.map { it.toSummaryUiModel() }
            ShoppingListsUiState(
                isLoading = false,
                activeLists = summaries.filter { it.status != ShoppingListStatus.COMPLETED },
                completedLists = summaries.filter { it.status == ShoppingListStatus.COMPLETED },
            )
        }
        .catch { error ->
            logger.e(TAG, "Erro ao carregar listas: ${error.message}", error)
            emit(ShoppingListsUiState(isLoading = false, error = error.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShoppingListsUiState(isLoading = true),
        )

    fun deleteList(listId: String) {
        viewModelScope.launch {
            shoppingListRepository.deleteShoppingListById(listId)
        }
    }

    private fun ShoppingList.toSummaryUiModel(): ShoppingListSummaryUiModel {
        val dateTime = Instant.fromEpochMilliseconds(updatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val formattedDate = "${dateTime.dayOfMonth.toString().padStart(2, '0')}/${dateTime.monthNumber.toString().padStart(2, '0')}"

        val total = items.size
        val checked = items.count { it.isChecked }

        return ShoppingListSummaryUiModel(
            id = id,
            name = name,
            type = type,
            status = status,
            totalItems = total,
            checkedItems = checked,
            budgetInCents = budgetInCents,
            formattedDate = formattedDate,
        )
    }
}
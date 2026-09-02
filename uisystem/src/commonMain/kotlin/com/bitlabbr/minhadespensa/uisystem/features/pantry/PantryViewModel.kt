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

package com.bitlabbr.minhadespensa.uisystem.features.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryUiState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock


class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val logger: AppLogger,
    notificationManager: AppNotificationManager,
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pantryUiState = MutableStateFlow(PantryUiState(isLoading = true))
    val pantryUiState: StateFlow<PantryUiState> = _pantryUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                pantryRepository.getAllActivePantryItemsWithCategory(),
                pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS),
                _searchQuery
            ) { allItems, expiringItems, query ->
                val allUiItems = allItems.map { it.toPantryItemUiModel() }
                val filtered = if (query.isBlank()) {
                    emptyList()
                } else {
                    allUiItems.filter { it.name.contains(query, ignoreCase = true) }
                }
                _pantryUiState.update { currentState ->
                    currentState.copy(
                        allActivePantryItems = allUiItems,
                        expiringPantryItems = expiringItems.map { it.toPantryItemUiModel() },
                        searchResults = filtered,
                        isLoading = false,
                        error = null
                    )
                }
            }.collect()
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSearchResultSelected(item: PantryItemUiModel) {
        _searchQuery.value = item.name
        getPantryItemDetails(item.id)
    }

    fun getPantryItemDetails(pantryItemId: String) {
        viewModelScope.launch {
            _pantryUiState.update { it.copy(isLoading = true, selectedPantryItem = null) }
            pantryRepository.getPantryItemWithCategoryByID(pantryItemId)
                .map { it?.toPantryItemUiModel() }
                .collect { item ->
                    _pantryUiState.update { it.copy(selectedPantryItem = item, isLoading = false) }
                }
        }
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }

    fun PantryItemWithCategory.toPantryItemUiModel(): PantryItemUiModel {
        val now = Clock.System.now().toEpochMilliseconds()
        val isExpired = this.pantryItem.expirationDate?.let { it < now } ?: false

        return PantryItemUiModel(
            id = this.pantryItem.id,
            name = this.name,
            category = this.category,
            brand = null,
            quantity = this.pantryItem.quantity,
            measureUnit = MeasureUnit.UNIT,
            netWeight = 0,
            expirationDate = this.pantryItem.expirationDate,
            isExpired = isExpired
        )
    }
}
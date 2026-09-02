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
import com.bitlabbr.minhadespensa.core.domain.model.CatalogCategories
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryFilterSubState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryListSubState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryUiState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager,
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val pantryIdToProductIdMap = mutableMapOf<String, String>()

    val uiState: StateFlow<PantryUiState> = combine(
        pantryRepository.getAllActivePantryItemsWithCategory(),
        pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS),
        _searchQuery,
        _selectedCategory,
    ) { allItems, expiringItems, query, selectedCategory ->

        allItems.forEach { item ->
            pantryIdToProductIdMap[item.pantryItem.id] = item.pantryItem.productId
        }

        val allUiItems = allItems.map { it.toPantryItemUiModel() }
        val isPantryEmpty = allUiItems.isEmpty()

        val dynamicCategories = (CatalogCategories.DEFAULT_CATEGORIES + allUiItems.map { it.category.trim() })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val filteredProducts = allUiItems.filter { item ->
            val matchesCategory = selectedCategory == null ||
                    item.category.equals(selectedCategory, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }

        val searchResults = if (query.isBlank()) {
            emptyList()
        } else {
            allUiItems.filter { it.name.contains(query, ignoreCase = true) }
        }

        PantryUiState(
            filterState = PantryFilterSubState(
                availableCategories = dynamicCategories,
                selectedCategory = selectedCategory,
            ),
            listState = PantryListSubState(
                products = filteredProducts,
                isLoading = false,
                isCatalogEmpty = isPantryEmpty,
                error = null,
            ),
            allActivePantryItems = allUiItems,
            expiringPantryItems = expiringItems.map { it.toPantryItemUiModel() },
            searchResults = searchResults,
            isLoading = false,
            error = null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PantryUiState(
            listState = PantryListSubState(isLoading = true),
            isLoading = true,
        ),
    )

    val pantryUiState: StateFlow<PantryUiState> = uiState

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchResultSelected(item: PantryItemUiModel) {
        _searchQuery.value = item.name
        getPantryItemDetails(item.id)
    }

    fun getProductImage(id: String): Flow<ByteArray?> {
        val targetProductId = pantryIdToProductIdMap[id] ?: id
        return catalogRepository.getProductImage(targetProductId)
            .catch { error ->
                logger.e(TAG, "Error while loading the product image $id: ${error.message}", error)
                emit(null)
            }
    }

    fun getPantryItemDetails(pantryItemId: String) {
        viewModelScope.launch {
            pantryRepository.getPantryItemWithCategoryByID(pantryItemId)
                .map { it?.toPantryItemUiModel() }
                .collect { item ->
                    // Tratamento do item selecionado
                }
        }
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }

    private fun PantryItemWithCategory.toPantryItemUiModel(): PantryItemUiModel {
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
            isExpired = isExpired,
        )
    }
}
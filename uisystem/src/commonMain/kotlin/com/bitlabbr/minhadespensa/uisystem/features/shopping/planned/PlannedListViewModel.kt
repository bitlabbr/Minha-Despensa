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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.CreatePlannedShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.PlannedItemDraft
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.shopping.planned.model.PlannedListUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class PlannedListViewModel(
    private val createPlannedShoppingListUseCase: CreatePlannedShoppingListUseCase,
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val TAG = "PlannedListViewModel"

    private val _title = MutableStateFlow("")
    private val _budgetInput = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _selectedQuantities = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val _isSaving = MutableStateFlow(false)
    private val _isSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlannedListUiState> = combine(
        combine(_title, _budgetInput, _searchQuery) { title, budget, query ->
            Triple(title, budget, query)
        },
        catalogRepository.getAllActives(),
        _selectedQuantities,
        combine(_isSaving, _isSuccess, _errorMessage) { saving, success, error ->
            Triple(saving, success, error)
        },
    ) { (title, budget, query), catalogProducts, quantities, (saving, success, error) ->
        val filteredProducts = if (query.isBlank()) {
            catalogProducts
        } else {
            catalogProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.brand?.contains(query, ignoreCase = true) == true)
            }
        }

        PlannedListUiState(
            title = title,
            budgetInput = budget,
            searchQuery = query,
            availableProducts = filteredProducts,
            selectedQuantities = quantities,
            isSaving = saving,
            isSuccess = success,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannedListUiState(),
    )

    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onBudgetChange(newBudget: String) {
        _budgetInput.value = newBudget.filter { it.isDigit() || it == ',' || it == '.' }.take(10)
    }
    fun onSearchQueryChange(newQuery: String) { _searchQuery.value = newQuery }

    fun onIncreaseQuantity(productId: String) {
        val current = _selectedQuantities.value[productId] ?: 0.0
        _selectedQuantities.update { it + (productId to (current + 1.0)) }
    }

    fun onDecreaseQuantity(productId: String) {
        val current = _selectedQuantities.value[productId] ?: return
        if (current <= 1.0) {
            _selectedQuantities.update { it - productId }
        } else {
            _selectedQuantities.update { it + (productId to (current - 1.0)) }
        }
    }

    fun savePlannedList(onSuccess: (listId: String) -> Unit) {
        viewModelScope.launch {
            if (!_title.value.isNotBlank() || _selectedQuantities.value.isEmpty()) return@launch

            _isSaving.value = true
            _errorMessage.value = null

            // Converte input monetário (ex: "150,50") para centavos (15050L)
            val budgetInCents: Long? = _budgetInput.value
                .replace(',', '.')
                .toDoubleOrNull()
                ?.let { (it * 100).roundToLong() }

            val itemsDraft = _selectedQuantities.value.map { (productId, qty) ->
                PlannedItemDraft(productId = productId, quantity = qty)
            }

            val result = createPlannedShoppingListUseCase(
                name = _title.value,
                budgetInCents = budgetInCents,
                itemsDraft = itemsDraft,
            )

            result.onSuccess { createdList ->
                logger.d(TAG, "Lista planejada salva: ${createdList.id}")
                _isSaving.value = false
                _isSuccess.value = true
                onSuccess(createdList.id)
            }.onFailure { error ->
                logger.e(TAG, "Erro ao salvar lista planejada: ${error.message}", error)
                _isSaving.value = false
                _errorMessage.value = error.message ?: "Falha ao salvar lista planejada"
            }
        }
    }
}
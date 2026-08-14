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
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock


class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _uiState = MutableStateFlow(PantryUiState(isLoading = true))
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    private val _productFormState = MutableStateFlow(ProductFormState())
    val productFormState: StateFlow<ProductFormState> = _productFormState.asStateFlow()

    init {
        logger.d(TAG, "init")
        viewModelScope.launch {
            combine(
                pantryRepository.getAllActivePantryItemsWithCategory(),
                pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS)
            ) { allItems, expiringItems ->
                _uiState.update { currentState ->
                    currentState.copy(
                        allActivePantryItems = allItems.map { it.toPantryItemUiModel() },
                        expiringPantryItems = expiringItems.map { it.toPantryItemUiModel() },
                        isLoading = false,
                        error = null
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        }
    }

    fun getPantryItemDetails(pantryItemId: String) {
        logger.d(TAG, "getPantryItemDetails: $pantryItemId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedPantryItem = null, error = null) }
            pantryRepository.getPantryItemWithCategoryByID(pantryItemId)
                .map { it?.toPantryItemUiModel() }
                .collect { item ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedPantryItem = item,
                            isLoading = false,
                            error = if (item == null) "Pantry item not found" else null
                        )
                    }
                }
        }
    }

    fun onProductFormChange(newState: ProductFormState) {
        _productFormState.update { newState }
    }

    fun saveProduct(): Job {
        return viewModelScope.launch {
            _productFormState.update { it.copy(isSaving = true) }
            val formState = _productFormState.value

            logger.d(TAG, "Attempting to save product with state: $formState")

            // Here you would typically convert the form state to a domain model
            // and persist it using the repository.
            // For example:
            // val newProduct = Product(name = formState.name, ...)
            // pantryRepository.insertProduct(newProduct)

            // Simulating a save operation
            delay(1500)

            logger.d(TAG, "Product saved successfully.")

            // Resetting form state after saving. This also sets isSaving back to false.
            _productFormState.update { ProductFormState() }
        }
    }

    /**
     * Resets the product form to its initial state.
     * Typically used when the user cancels the creation process.
     */
    fun resetProductForm() {
        _productFormState.value = ProductFormState()
        logger.d(TAG, "Product form state has been reset.")
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }
}

fun PantryItemWithCategory.toPantryItemUiModel(): PantryItemUiModel {
    val now = Clock.System.now().toEpochMilliseconds()
    val isExpired = this.pantryItem.expirationDate?.let { it < now } ?: false

    return PantryItemUiModel(
        id = this.pantryItem.id,
        name = this.name,
        brand = null,
        quantity = this.pantryItem.quantity,
        measureUnit = this.pantryItem.productId.let { /* TODO: Get MeasureUnit from product details */ com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit.UNITY },
        netWeight = 0,
        expirationDate = this.pantryItem.expirationDate,
        isExpired = isExpired
    )
}
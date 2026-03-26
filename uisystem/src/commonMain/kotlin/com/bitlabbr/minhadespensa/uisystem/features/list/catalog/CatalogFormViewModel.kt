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

package com.bitlabbr.minhadespensa.uisystem.features.list.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.usecases.SaveCatalogProductUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogFormViewModel(
    private val saveProductUseCase: SaveCatalogProductUseCase,
    private val logger: AppLogger
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogFormState())
    val state: StateFlow<CatalogFormState> = _state.asStateFlow()

    fun onNameChange(newValue: String) { _state.update { it.copy(name = newValue) } }
    fun onBrandChange(newValue: String) { _state.update { it.copy(brand = newValue) } }
    fun onEanChange(newValue: String) { _state.update { it.copy(ean = newValue) } }
    fun onWeightChange(newValue: String) { _state.update { it.copy(netWeight = newValue) } }
    fun onUnitChange(newValue: MeasureUnit) { _state.update { it.copy(measureUnit = newValue) } }

    fun saveProduct() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val weight = _state.value.netWeight.replace(',', '.').toDoubleOrNull()

            if (weight == null || weight <= 0) {
                _state.update { it.copy(isLoading = false, errorMessage = "Peso inválido") }
                return@launch
            }

            val result = saveProductUseCase(
                name = _state.value.name,
                brand = _state.value.brand.takeIf { it.isNotBlank() },
                measureUnit = _state.value.measureUnit,
                netWeight = weight,
                ean = _state.value.ean.takeIf { it.isNotBlank() },
                isEditing = false
            )

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
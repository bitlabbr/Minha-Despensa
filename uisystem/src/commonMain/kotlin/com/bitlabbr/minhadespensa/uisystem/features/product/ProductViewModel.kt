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

package com.bitlabbr.minhadespensa.uisystem.features.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.uisystem.features.product.widgets.register.ProductFormState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.catetory_others
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ProductViewModel(
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger
) : ViewModel() {
    private val TAG = "ProductViewModel"
    private var eanValidationJob: Job? = null
    private val _productFormState = MutableStateFlow(ProductFormState())
    val productFormState: StateFlow<ProductFormState> = _productFormState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            catalogRepository.getCategories()
                .catch { e -> logger.e(TAG, "Failure while loading categories: ${e.message}", e) }
                .collect { categories ->
                    _productFormState.update { it.copy(availableCategories = categories) }
                }
        }
    }

    fun onProductFormChange(newState: ProductFormState) {
        _productFormState.update { current ->
            val isEanChanged = newState.ean != current.ean
            val preservedEanError = if (isEanChanged) null else current.eanError
            val preservedIsChecking = !isEanChanged && current.isCheckingEan

            val validatedState = newState.copy(
                availableCategories = current.availableCategories,
                nameError = when {
                    newState.name.isBlank() && current.name.isNotEmpty() -> "Nome é obrigatório"
                    newState.name.length > CoreConstants.Product.NAME_MAX_LENGTH ->
                        "Máximo de ${CoreConstants.Product.NAME_MAX_LENGTH} caracteres"

                    else -> null
                },
                netWeightError = when {
                    newState.netWeight.isNotBlank() &&
                            newState.netWeight.replace(',', '.').toDoubleOrNull() == null -> "Valor numérico inválido"

                    else -> null
                },
                eanError = preservedEanError,
                isCheckingEan = preservedIsChecking
            )

            if (isEanChanged) {
                validateEanDebounced(newState.ean.trim())
            }

            validatedState
        }
    }

    private fun validateEanDebounced(ean: String) {
        eanValidationJob?.cancel()

        if (ean.isBlank()) {
            _productFormState.update { it.copy(eanError = null, isCheckingEan = false) }
            return
        }

        if (ean.length !in CoreConstants.Product.EAN_VALID_LENGTHS) {
            _productFormState.update {
                it.copy(
                    eanError = "EAN deve ter 8, 13 ou 14 dígitos",
                    isCheckingEan = false
                )
            }
            return
        }

        eanValidationJob = viewModelScope.launch {
            _productFormState.update { it.copy(isCheckingEan = true, eanError = null) }
            delay(350.milliseconds)
            val existingProduct = catalogRepository.getProductByEan(ean).firstOrNull()
            _productFormState.update { state ->
                if (state.ean.trim() == ean) {
                    if (existingProduct != null && !existingProduct.isDeleted) {
                        state.copy(
                            eanError = "Código já cadastrado: ${existingProduct.name}",
                            isCheckingEan = false
                        )
                    } else {
                        state.copy(eanError = null, isCheckingEan = false)
                    }
                } else {
                    state
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveProduct(): Job {
        return viewModelScope.launch {
            val form = _productFormState.value
            if (!form.isFormValid) {
                logger.d(TAG, "Formulário inválido para salvar.")
                return@launch
            }

            _productFormState.update { it.copy(isSaving = true) }
            val now = getCurrentTime()

            try {
                val productId = Uuid.random().toString()
                val netWeightValue = form.netWeight.replace(',', '.').toDoubleOrNull() ?: 1.0
                val measureUnitValue = runCatching { MeasureUnit.valueOf(form.measureUnit) }
                    .getOrDefault(MeasureUnit.UNIT)

                val catalogProduct = CatalogProduct(
                    id = productId,
                    name = form.name.trim(),
                    brand = form.brand.trim().takeIf { it.isNotBlank() },
                    category = form.category.ifBlank { getString(Res.string.catetory_others) },
                    measureUnit = measureUnitValue,
                    netWeight = netWeightValue,
                    updatedAt = now,
                    isDeleted = false,
                    manuallyAdded = true,
                    notes = form.notes.trim(),
                    ean = form.ean.trim().takeIf { it.isNotBlank() }
                )
                catalogRepository.insertProduct(catalogProduct, form.imageBytes)
                _productFormState.update { current ->
                    ProductFormState(availableCategories = current.availableCategories)
                }
            } catch (e: Exception) {
                logger.e(TAG, "Erro ao salvar produto: ${e.message}", e)
                _productFormState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetProductForm() {
        eanValidationJob?.cancel()
        _productFormState.update { current ->
            ProductFormState(availableCategories = current.availableCategories)
        }
    }
}
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

package com.bitlabbr.minhadespensa.uisystem.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductsListViewModel(
    private val catalogRepository: CatalogRepository,
    private val pantryRepository: PantryRepository,
    private val priceRepository: PriceRepository,
    private val logger: AppLogger
) : ViewModel() {
    private val TAG = "ProductsListViewModel"
    private val _formState = MutableStateFlow(ProductFormState())
    val formState = _formState.asStateFlow()

    val productUiState: StateFlow<ProductsUiState> = pantryRepository.getAllActiveInventory()
        .map<List<PantryItem>, ProductsUiState> { pantryItems ->
            ProductsUiState.Success(
                items = emptyList(),
                totalQuantity = pantryItems.sumOf { it.quantity }
            )
        }
        .catch { e ->
            logger.e(TAG, "Failure while loading pantry items: ${e.message}")
            emit(ProductsUiState.Error("Falha ao carregar dados."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductsUiState.Loading
        )

    fun saveProduct() {
        val state = _formState.value
        if (state.name.isBlank() || state.quantity.isBlank()) return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            try {
                val now = getCurrentTime()
                val productId = Uuid.random().toString()

                val catalogProduct = CatalogProduct(
                    id = productId,
                    name = state.name,
                    brand = state.brand.ifBlank { null },
                    measureUnit = state.unit,
                    netWeight = state.netWeight.toDouble(),
                    updatedAt = now,
                    manuallyAdded = true
                )
                catalogRepository.insertProduct(catalogProduct, null)

                val pantryItem = PantryItem(
                    id = Uuid.random().toString(),
                    productId = productId,
                    quantity = state.quantity.toDoubleOrNull() ?: 0.0,
                    expirationDate = state.expirationDate,
                    updatedAt = now
                )
                pantryRepository.saveItem(pantryItem)

                state.price?.let { priceStr ->
                    val priceCents = ((priceStr.toDoubleOrNull() ?: (0.0 * 100))).toLong()
                    if (priceCents > 0) {
                        priceRepository.addPriceEntry(
                            PriceEntry(
                                id = Uuid.random().toString(),
                                productId = productId,
                                priceInCents = priceCents,
                                updatedAt = now
                            )
                        )
                    }
                }

                resetForm()
            } catch (e: Exception) {
                logger.e(TAG, "Failure while trying to save the product: ${e.message}")
            } finally {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun removeProduct(pantryId: String) {
        viewModelScope.launch {
            pantryRepository.deleteItem(pantryId, getCurrentTime())
        }
    }

    fun resetForm() = _formState.update { ProductFormState() }

    fun onBrandChange(newBrand: String) {
        _formState.update { it.copy(brand = newBrand) }
    }

    fun onNetWeightChange(newWeight: String) {
        _formState.update { it.copy(netWeight = newWeight) }
    }

    fun onPriceChange(newPrice: String) {
        _formState.update { it.copy(price = newPrice) }
    }

    fun onExpirationDateChange(date: Long?) {
        _formState.update { it.copy(expirationDate = date) }
    }

    fun onNameChange(newName: String) {
        _formState.update { it.copy(name = newName) }
    }

    fun onQuantityChange(newQuantity: String) {
        _formState.update { it.copy(quantity = newQuantity) }
    }

    fun onUnitChange(newUnit: MeasureUnit) {
        _formState.update { it.copy(unit = newUnit) }
    }
}

/*
 * Copyright (c) 2026 Willian Santos
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0
 * International License (CC BY-NC 4.0).
 *
 * You may use, copy, modify, and distribute this file for non-commercial
 * purposes only, provided that proper attribution is given.
 *
 * The copyright holder retains all commercial rights and may
 * license this work under different terms.
 *
 * License: https://creativecommons.org/licenses/by-nc/4.0/
 *
 */

package com.bitlabbr.minhadespensa.uisystem.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.Product
import com.bitlabbr.minhadespensa.core.domain.repository.ProductRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random

class ProductsListViewModel(
    private val repository: ProductRepository,
    private val logger: AppLogger
) : ViewModel() {
    private val TAG = "ProductsListViewModel"
    private val _uiState = MutableStateFlow<List<Product>>(emptyList())
    private val _formState = MutableStateFlow(ProductFormState())
    val formState = _formState.asStateFlow()

    val uiState: StateFlow<ProductsUiState> = repository.getAllProducts()
        .map<List<Product>, ProductsUiState> { product ->
           // logger.d(TAG, "Lista atualizada do banco. Itens: ${product.size}")
            val total = product.sumOf { it.amount }

            ProductsUiState.Success(product, total)
        }
        .catch { e ->
            logger.e(TAG, "Erro ao carregar produtos: ${e.message}")
            emit(ProductsUiState.Error("Falha ao carregar dados."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductsUiState.Loading
        )

    fun onNameChange(newName: String) {
        _formState.update { it.copy(name = newName) }
    }

    fun onQuantityChange(newQuantity: String) {
        // Only allows numeric input (optional regex check can be added here)
        _formState.update { it.copy(quantity = newQuantity) }
    }

    fun onUnitChange(newUnit: MeasureUnit) {
        _formState.update { it.copy(unit = newUnit) }
    }

    fun saveProduct() {
        val currentState = _formState.value
        if (currentState.name.isBlank() || currentState.quantity.isBlank()) return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            try {
                val qtyDouble = currentState.quantity.toDoubleOrNull() ?: 0.0

                val newProduct = Product(
                    id = "${Clock.System.now().toEpochMilliseconds()}",
                    name = currentState.name,
                    amount = qtyDouble,
                    measureUnit = currentState.unit,
                    expirationDate = null,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )

                logger.d(TAG, "Saving product: ${newProduct.name}")
                repository.insertProduct(newProduct)
                resetForm()

            } catch (e: Exception) {
                logger.e(TAG, "Error saving: ${e.message}")
            } finally {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetForm() {
        _formState.update { ProductFormState() }
    }

    fun removeProduct(id: String) {
        logger.d(TAG, "removeProduct: $id")
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }
}

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random

class ProductsListViewModel(
    private val repository: ProductRepository,
    private val logger: AppLogger
) : ViewModel() {
    private val TAG = "ProductsListViewModel"
    private val _uiState = MutableStateFlow<List<Product>>(emptyList())
    val uiState: StateFlow<ProductsUiState> = repository.getAllProducts()
        .map<List<Product>, ProductsUiState> { product ->
            logger.d(TAG, "Lista atualizada do banco. Itens: ${product.size}")
            val total = product.sumOf { it.amount * 1.0 }

            ProductsUiState.Success(
                produtos = product,
                totalEstimado = total
            )
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

    fun addProductTest() {
        viewModelScope.launch {
            val novoProduto = Product(
                id = "${Clock.System.now().toEpochMilliseconds()}",
                name = "Produto ${Random.nextInt(100, 999)}",
                amount = Random.nextDouble(1.0, 5.0),
                measureUnit = MeasureUnit.UNITY,
                expirationDate = null
            )
            logger.d(TAG, "Tentando salvar: ${novoProduto.name}")
            repository.insertProduct(novoProduto)
        }
    }

    fun removerProduct(id: String) {
        viewModelScope.launch {
            logger.d(TAG, "Removendo: $id")
            repository.deleteProductById(id)
        }
    }
}


sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(val produtos: List<Product>, val totalEstimado: Double) : ProductsUiState
    data class Error(val message: String) : ProductsUiState
}
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
 */

package com.bill.minhadispensa.uisystem.theme.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bill.minhadispensa.core.domain.model.Product
import com.bill.minhadispensa.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductsListViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Product>>(emptyList())
    val uiState: StateFlow<List<Product>> = _uiState.asStateFlow()

    init {
        loadAllProducts()
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.value = products
            }
        }
    }
}
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

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.Product

data class ProductFormState(
    val name: String = "",
    val quantity: String = "",
    val unit: MeasureUnit = MeasureUnit.UNITY,
    val isSaving: Boolean = false
)

sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(val products: List<Product>, val estimatedTotal: Double) : ProductsUiState
    data class Error(val message: String) : ProductsUiState
}
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

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit

data class ProductFormState(
    val name: String = "",
    val brand: String = "",
    val quantity: String = "",
    val unit: MeasureUnit = MeasureUnit.UNITY,
    val netWeight: String = "",
    val expirationDate: Long? = null,
    val price: String = "",
    val isSaving: Boolean = false
)

sealed interface ProductsUiState {
    data object Loading : ProductsUiState

    data class Success(
        val items: List<PantryItemUiModel>,
        val totalQuantity: Double
    ) : ProductsUiState

    data class Error(val message: String) : ProductsUiState
}
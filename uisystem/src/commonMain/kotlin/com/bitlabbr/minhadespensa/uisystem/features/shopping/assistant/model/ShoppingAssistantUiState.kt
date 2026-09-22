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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct

sealed interface ShoppingAssistantSubFlow {
    data object BarcodeScanner : ShoppingAssistantSubFlow
    data class CreateProduct(val initialEan: String? = null) : ShoppingAssistantSubFlow
    data class AddItemDetails(
        val product: CatalogProduct? = null,
        val rawText: String? = null,
        val existingItemId: String? = null,
        val initialQuantity: Double = 1.0,
        val initialPriceInCents: Long? = null,
    ) : ShoppingAssistantSubFlow
}

data class CartItemUiModel(
    val id: String,
    val productId: String?,
    val displayName: String,
    val quantity: Double,
    val priceAtTime: Long?,
    val isChecked: Boolean,
    val subtotalInCents: Long,
)

data class ShoppingAssistantUiState(
    val listId: String = "",
    val listTitle: String = "Compras",
    val items: List<CartItemUiModel> = emptyList(),
    val totalCartValueInCents: Long = 0,
    val checkedCount: Int = 0,
    val totalCount: Int = 0,
    val activeSubFlow: ShoppingAssistantSubFlow? = null,
    val isLoading: Boolean = false,
    val isFinalizing: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
)
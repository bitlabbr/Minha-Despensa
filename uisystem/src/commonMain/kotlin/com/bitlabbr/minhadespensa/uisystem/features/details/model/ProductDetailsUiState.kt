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

package com.bitlabbr.minhadespensa.uisystem.features.details.model

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.uisystem.model.UiText

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val product: ProductDetailsInfoUiModel? = null,
    val pantryStock: ProductPantryStockUiModel? = null,
    val priceHistory: List<ProductPriceEntryUiModel> = emptyList(),
    val latestPrice: Long? = null,
    val averagePrice: Long? = null,
    val lowestPrice: Long? = null,
    val highestPrice: Long? = null,
    val activeShoppingLists: List<ShoppingListOptionUiModel> = emptyList(),
    val activeSubFlow: ProductDetailsSubFlow? = null,
    val error: UiText? = null,
)

data class ProductDetailsInfoUiModel(
    val id: String,
    val name: String,
    val brand: String? = null,
    val category: String,
    val ean: String? = null,
    val measureUnit: MeasureUnit = MeasureUnit.UNIT,
    val netWeight: Double = 1.0,
    val imageBytes: ByteArray? = null,
)

data class ProductPantryStockUiModel(
    val totalQuantity: Double = 0.0,
    val measureUnit: MeasureUnit = MeasureUnit.UNIT,
    val closestExpirationDate: Long? = null,
    val isExpired: Boolean = false,
    val batches: List<PantryBatchUiModel> = emptyList(),
    val hasStock: Boolean = totalQuantity > 0.0,
)

data class PantryBatchUiModel(
    val id: String,
    val quantity: Double,
    val expirationDate: Long? = null,
    val batchNumber: String? = null,
    val isExpired: Boolean = false,
    val daysUntilExpiration: Long? = null,
)

data class ProductPriceEntryUiModel(
    val id: String,
    val priceInCents: Long,
    val storeName: String,
    val date: Long,
)

data class ShoppingListOptionUiModel(
    val id: String,
    val name: String,
    val itemsCount: Int = 0,
)

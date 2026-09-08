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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.evaluate

import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogProductUiModel
import com.bitlabbr.minhadespensa.uisystem.model.UiText

data class PantryItemFormState(
    val selectedProduct: CatalogProductUiModel? = null,
    val ean: String = "",
    val quantity: String = "1.0",
    val expirationDate: Long? = null,
    val batchNumber: String = "",
    val price: String = "",
    val isSearchingCatalog: Boolean = false,
    val productNotFound: Boolean = false,
    val isSaving: Boolean = false,
    val quantityError: UiText? = null,
) {
    val isFormValid: Boolean
        get() = selectedProduct != null &&
                quantity.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true &&
                quantityError == null &&
                !isSearchingCatalog &&
                !isSaving
}
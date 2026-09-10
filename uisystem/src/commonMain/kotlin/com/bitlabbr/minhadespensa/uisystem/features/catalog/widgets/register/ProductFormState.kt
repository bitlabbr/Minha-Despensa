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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.model.UiText

data class ProductFormState(
    val name: String = "",
    val brand: String = "",
    val category: String = CoreConstants.Product.DEFAULT_CATEGORY,
    val measureUnit: MeasureUnit = CoreConstants.Product.DEFAULT_MEASURE_UNITY,
    val netWeight: String = CoreConstants.Product.DEFAULT_NET_WEIGHT.toString(),
    val ean: String = "",
    val notes: String = "",
    val imageBytes: ByteArray? = null,
    val availableCategories: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isCheckingEan: Boolean = false,
    val errorMessage: String? = null,
    val nameError: UiText? = null,
    val netWeightError: UiText? = null,
    val eanError: UiText? = null,
    val categoryError: UiText? = null,
    val measureUnitError: UiText? = null,
) {

    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                nameError == null &&
                netWeightError == null &&
                eanError == null &&
                !isCheckingEan &&
                !isSaving

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ProductFormState
        if (name != other.name) return false
        if (brand != other.brand) return false
        if (category != other.category) return false
        if (measureUnit != other.measureUnit) return false
        if (netWeight != other.netWeight) return false
        if (ean != other.ean) return false
        if (notes != other.notes) return false
        if (isSaving != other.isSaving) return false
        if (isCheckingEan != other.isCheckingEan) return false
        if (errorMessage != other.errorMessage) return false
        if (nameError != other.nameError) return false
        if (netWeightError != other.netWeightError) return false
        if (eanError != other.eanError) return false
        if (availableCategories != other.availableCategories) return false
        if (imageBytes != null) {
            if (other.imageBytes == null || !imageBytes.contentEquals(other.imageBytes)) return false
        } else if (other.imageBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + brand.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + measureUnit.hashCode()
        result = 31 * result + netWeight.hashCode()
        result = 31 * result + ean.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + isCheckingEan.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (nameError?.hashCode() ?: 0)
        result = 31 * result + (netWeightError?.hashCode() ?: 0)
        result = 31 * result + (eanError?.hashCode() ?: 0)
        result = 31 * result + availableCategories.hashCode()
        return result
    }
}
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
package com.bitlabbr.minhadespensa.uisystem.features.product.widgets.register

data class ProductFormState(
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val measureUnit: String = "",
    val netWeight: String = "",
    val ean: String = "",
    val notes: String = "",
    val imageBytes: ByteArray? = null,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val netWeightError: String? = null,
    val measureUnitError: String? = null,
    val eanError: String? = null,
    val isCheckingEan: Boolean = false
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                nameError == null &&
                netWeight.isNotBlank() &&
                netWeightError == null &&
                measureUnit.isNotBlank() &&
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
        if (ean != other.ean) return false
        if (notes != other.notes) return false
        if (netWeight != other.netWeight) return false
        if (measureUnit != other.measureUnit) return false
        if (isSaving != other.isSaving) return false
        if (isCheckingEan != other.isCheckingEan) return false
        if (nameError != other.nameError) return false
        if (netWeightError != other.netWeightError) return false
        if (measureUnitError != other.measureUnitError) return false
        if (eanError != other.eanError) return false

        if (imageBytes != null) {
            if (other.imageBytes == null) return false
            if (!imageBytes.contentEquals(other.imageBytes)) return false
        } else if (other.imageBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + brand.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + ean.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + netWeight.hashCode()
        result = 31 * result + measureUnit.hashCode()
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + isCheckingEan.hashCode()
        result = 31 * result + nameError.hashCode()
        result = 31 * result + netWeightError.hashCode()
        result = 31 * result + measureUnitError.hashCode()
        result = 31 * result + eanError.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        return result
    }
}
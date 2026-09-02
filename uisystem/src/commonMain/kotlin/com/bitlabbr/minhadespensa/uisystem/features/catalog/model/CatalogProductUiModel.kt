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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.model

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit

data class CatalogProductUiModel(
    val id: String,
    val name: String,
    val category: String,
    val brand: String?,
    val measureUnit: MeasureUnit,
    val netWeight: Double,
    val ean: String?,
    val formattedWeight: String,
)

fun CatalogProduct.toUiModel(): CatalogProductUiModel {
    return CatalogProductUiModel(
        id = this.id,
        name = this.name,
        category = this.category,
        brand = this.brand,
        measureUnit = this.measureUnit,
        netWeight = this.netWeight,
        ean = this.ean,
        formattedWeight = "${this.netWeight} ${this.measureUnit.name.lowercase()}"
    )
}
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

package com.bitlabbr.minhadespensa.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CatalogProduct(
    val id: String,
    val ean: String? = null,
    val name: String,
    val brand: String? = null,
    val measureUnit: MeasureUnit,
    val netWeight: Double,
    val thumbnailUrl: String? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val manuallyAdded: Boolean = true
) {

    override fun toString(): String {
        return "CatalogProduct(" +
                "id='$id', " +
                "ean=$ean, " +
                "name='$name', " +
                "brand=$brand, " +
                "measureUnit=$measureUnit, " +
                "thumbnailUrl=$thumbnailUrl," +
                " updatedAt=$updatedAt, " +
                "isDeleted=$isDeleted, " +
                "manuallyAdded=$manuallyAdded" +
                ")"
    }

}
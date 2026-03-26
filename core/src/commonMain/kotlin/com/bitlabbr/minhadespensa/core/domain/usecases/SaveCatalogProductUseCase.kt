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

package com.bitlabbr.minhadespensa.core.domain.usecases

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SaveCatalogProductUseCase(
    private val repository: CatalogRepository
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        id: String = Uuid.random().toString(),
        name: String,
        brand: String?,
        measureUnit: MeasureUnit,
        netWeight: Double,
        ean: String?,
        imageBytes: ByteArray? = null,
        isEditing: Boolean = false,
        thumbnailUrl: String? = null,
        manuallyAdded: Boolean = false
    ): Result<Unit> = runCatching {

        val product = CatalogProduct(
            id = id,
            name = name.trim(),
            brand = brand?.trim(),
            measureUnit = measureUnit,
            netWeight = netWeight,
            ean = ean?.trim(),
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = manuallyAdded,
            thumbnailUrl = thumbnailUrl
        )

        if (isEditing) {
            repository.updateForProductIfNewer(product, imageBytes)
        } else {
            repository.insertProduct(product, imageBytes)
        }
    }
}
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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SaveCatalogProductUseCase(
    private val catalogRepository: CatalogRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        id: String? = null,
        name: String,
        brand: String? = null,
        category: String = CoreConstants.Product.DEFAULT_CATEGORY,
        measureUnit: MeasureUnit,
        netWeight: Double = CoreConstants.Product.DEFAULT_NET_WEIGHT,
        ean: String? = null,
        imageBytes: ByteArray? = null,
        isEditing: Boolean = false,
    ): Result<CatalogProduct> = runCatching {
        val trimmedName = name.trim()
        val trimmedBrand = brand?.trim()?.takeIf { it.isNotBlank() }
        val trimmedCategory = category.trim().ifBlank { CoreConstants.Product.DEFAULT_CATEGORY }
        val trimmedEan = ean?.trim()?.takeIf { it.isNotBlank() }

        require(trimmedName.isNotBlank()) { CoreConstants.Validation.ERROR_PRODUCT_NAME_BLANK }
        require(trimmedName.length <= CoreConstants.Product.NAME_MAX_LENGTH) { "${CoreConstants.Validation.ERROR_PRODUCT_NAME_TOO_LONG}:${CoreConstants.Product.NAME_MAX_LENGTH}" }
        require(trimmedCategory.length <= CoreConstants.Product.CATEGORY_MAX_LENGTH) { "${CoreConstants.Validation.ERROR_PRODUCT_CATEGORY_TOO_LONG}: ${CoreConstants.Product.CATEGORY_MAX_LENGTH}" }
        require(netWeight > 0.0) { CoreConstants.Validation.ERROR_PRODUCT_NET_WEIGHT_POSITIVE }

        trimmedEan?.let { code ->
            require(code.length in CoreConstants.Product.EAN_VALID_LENGTHS && code.all { it.isDigit() }) {
                CoreConstants.Validation.ERROR_EAN_INVALID_FORMAT
            }
        }

        val product = CatalogProduct(
            id = id ?: Uuid.random().toString(),
            name = trimmedName,
            brand = trimmedBrand,
            category = trimmedCategory,
            measureUnit = measureUnit,
            netWeight = netWeight,
            ean = trimmedEan,
            thumbnailUrl = null,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true,
        )

        if (isEditing) {
            catalogRepository.updateForProductIfNewer(product, imageBytes)
        } else {
            catalogRepository.insertProduct(product, imageBytes)
        }

        product
    }
}
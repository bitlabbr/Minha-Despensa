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

import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch

class DeleteProductCategoryUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(
        id: String,
        categoryName: String,
    ): Result<Unit> = runCatching {
        val trimmedId = id.trim()
        val trimmedName = categoryName.trim()

        require(trimmedId.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_ID_REQUIRED }
        require(trimmedName.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
        require(trimmedName.normalizeForSearch() != CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()) {
            CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE
        }

        catalogRepository.deleteCategory(
            id = trimmedId,
            categoryName = trimmedName,
            fallbackCategory = CoreConstants.Product.DEFAULT_CATEGORY,
            updatedAt = getCurrentTime(),
        )
    }
}

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

import com.bitlabbr.minhadespensa.core.domain.model.ProductCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SaveProductCategoryUseCase(
    private val catalogRepository: CatalogRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        id: String? = null,
        name: String,
        oldName: String? = null,
    ): Result<ProductCategory> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
        require(trimmedName.length <= CoreConstants.Product.CATEGORY_MAX_LENGTH) {
            "${CoreConstants.Validation.ERROR_PRODUCT_CATEGORY_TOO_LONG}: ${CoreConstants.Product.CATEGORY_MAX_LENGTH}"
        }

        val normalizedNewName = trimmedName.normalizeForSearch()
        val normalizedDefault = CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()
        val activeCategories = catalogRepository.getAllActiveCategories().first()
        val existingNames = catalogRepository.getCategories().first()

        val trimmedId = id?.trim()?.takeIf { it.isNotBlank() }
        val now = getCurrentTime()

        if (trimmedId != null) {
            val currentCategory = activeCategories.firstOrNull { it.id == trimmedId }
            val effectiveOldName = oldName?.trim()?.takeIf { it.isNotBlank() }
                ?: currentCategory?.name
                ?: ""

            require(effectiveOldName.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
            val normalizedOldName = effectiveOldName.normalizeForSearch()

            require(normalizedOldName != normalizedDefault) {
                CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE
            }

            val hasDuplicateInActive = activeCategories.any { cat ->
                cat.id != trimmedId &&
                    cat.name.normalizeForSearch() != normalizedOldName &&
                    cat.name.normalizeForSearch() == normalizedNewName
            }
            val hasDuplicateInNames = existingNames.any { existingName ->
                existingName.normalizeForSearch() != normalizedOldName &&
                    existingName.normalizeForSearch() == normalizedNewName
            }

            require(!hasDuplicateInActive && !hasDuplicateInNames) {
                CoreConstants.Validation.ERROR_CATEGORY_ALREADY_EXISTS
            }

            catalogRepository.renameCategory(
                id = trimmedId,
                oldName = effectiveOldName,
                newName = trimmedName,
                updatedAt = now,
            )

            ProductCategory(
                id = trimmedId,
                name = trimmedName,
                isDefault = currentCategory?.isDefault ?: false,
                updatedAt = now,
                isDeleted = false,
            )
        } else {
            val hasDuplicateInActive = activeCategories.any { cat ->
                cat.name.normalizeForSearch() == normalizedNewName
            }
            val hasDuplicateInNames = existingNames.any { existingName ->
                existingName.normalizeForSearch() == normalizedNewName
            }

            require(!hasDuplicateInActive && !hasDuplicateInNames) {
                CoreConstants.Validation.ERROR_CATEGORY_ALREADY_EXISTS
            }

            val category = ProductCategory(
                id = Uuid.random().toString(),
                name = trimmedName,
                isDefault = false,
                updatedAt = now,
                isDeleted = false,
            )

            catalogRepository.saveCategory(category)
            category
        }
    }
}

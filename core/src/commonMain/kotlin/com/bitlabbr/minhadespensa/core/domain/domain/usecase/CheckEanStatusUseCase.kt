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

package com.bitlabbr.minhadespensa.core.domain.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.flow.firstOrNull

sealed interface EanStatus {
    data object Empty : EanStatus
    data object InvalidFormat : EanStatus
    data class Found(val product: CatalogProduct) : EanStatus
    data object NotFound : EanStatus
}

class CheckEanStatusUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(ean: String): EanStatus {
        val trimmed = ean.trim()
        if (trimmed.isBlank()) return EanStatus.Empty

        if (trimmed.length !in CoreConstants.Product.EAN_VALID_LENGTHS || !trimmed.all { it.isDigit() }) {
            return EanStatus.InvalidFormat
        }
        val product = catalogRepository.getProductByEan(trimmed).firstOrNull()
        return if (product != null && !product.isDeleted) {
            EanStatus.Found(product)
        } else {
            EanStatus.NotFound
        }
    }
}
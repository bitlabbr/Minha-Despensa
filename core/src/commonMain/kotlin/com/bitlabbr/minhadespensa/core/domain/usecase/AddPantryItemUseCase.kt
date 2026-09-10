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

import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddPantryItemUseCase(
    private val pantryRepository: PantryRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        productId: String,
        quantity: Double,
        expirationDate: Long? = null,
        batchNumber: String? = null,
    ): Result<PantryItem> = runCatching {
        require(productId.isNotBlank()) { CoreConstants.Validation.ERROR_PRODUCT_ID_REQUIRED }
        require(quantity > 0.0) { CoreConstants.Validation.ERROR_PANTRY_QUANTITY_POSITIVE }

        val trimmedBatch = batchNumber?.trim()?.takeIf { it.isNotBlank() }

        val item = PantryItem(
            id = Uuid.random().toString(),
            productId = productId,
            quantity = quantity,
            expirationDate = expirationDate,
            batchNumber = trimmedBatch,
            updatedAt = getCurrentTime(),
            isDeleted = false,
        )

        pantryRepository.insertPantryItem(item)
        item
    }
}
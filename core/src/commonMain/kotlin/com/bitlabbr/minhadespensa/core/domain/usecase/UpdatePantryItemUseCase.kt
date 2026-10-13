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

class UpdatePantryItemUseCase(
    private val pantryRepository: PantryRepository,
) {
    suspend operator fun invoke(
        id: String,
        productId: String,
        quantity: Double,
        expirationDate: Long? = null,
        batchNumber: String? = null,
        unitPriceInCents: Long? = null,
    ): Result<PantryItem> = runCatching {
        require(id.isNotBlank()) { CoreConstants.Validation.ERROR_PANTRY_ITEM_ID_REQUIRED }
        require(productId.isNotBlank()) { CoreConstants.Validation.ERROR_PRODUCT_ID_REQUIRED }
        require(quantity >= 0.0) { CoreConstants.Validation.ERROR_PANTRY_QUANTITY_NON_NEGATIVE }
        if (unitPriceInCents != null) {
            require(unitPriceInCents >= 0L) { CoreConstants.Validation.ERROR_PANTRY_UNIT_PRICE_NON_NEGATIVE }
        }

        val trimmedBatch = batchNumber?.trim()?.takeIf { it.isNotBlank() }

        val item = PantryItem(
            id = id,
            productId = productId,
            quantity = quantity,
            expirationDate = expirationDate,
            batchNumber = trimmedBatch,
            unitPriceInCents = unitPriceInCents,
            updatedAt = getCurrentTime(),
            isDeleted = quantity == 0.0,
        )

        pantryRepository.updatePantryItemIfNewer(item)
        item
    }
}

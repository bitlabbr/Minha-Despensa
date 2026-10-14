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

import com.bitlabbr.minhadespensa.core.domain.model.PantryStockValue
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToLong

class GetPantryTotalValueUseCase(
    private val pantryRepository: PantryRepository,
) {
    operator fun invoke(): Flow<PantryStockValue> {
        return pantryRepository.getAllActivePantryItems().map { items ->
            val activeItems = items.filter { !it.isDeleted }
            var totalValueInCents = 0L
            var itemsWithPriceCount = 0

            for (item in activeItems) {
                val unitPrice = item.unitPriceInCents
                if (unitPrice != null && unitPrice > 0L && item.quantity > 0.0) {
                    val itemTotal = (item.quantity * unitPrice.toDouble()).roundToLong()
                    totalValueInCents += itemTotal
                    itemsWithPriceCount++
                }
            }

            PantryStockValue(
                totalValueInCents = totalValueInCents,
                itemsWithPriceCount = itemsWithPriceCount,
                totalActiveItemsCount = activeItems.size,
            )
        }
    }
}

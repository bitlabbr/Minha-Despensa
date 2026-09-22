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

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.coroutines.flow.firstOrNull

class FinalizeShoppingSessionUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: String): Result<Unit> = runCatching {
        val now = getCurrentTime()
        val list = shoppingListRepository.getShoppingListById(listId).firstOrNull()
            ?: throw IllegalArgumentException("Lista não encontrada: $listId")

        shoppingListRepository.finalizePurchase(listId)

        val completedList = list.copy(
            status = ShoppingListStatus.COMPLETED,
            updatedAt = now,
        )
        shoppingListRepository.updateShoppingListIfNewer(completedList)
    }
}
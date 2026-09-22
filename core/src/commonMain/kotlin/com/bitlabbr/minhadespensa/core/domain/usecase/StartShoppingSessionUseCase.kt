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

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StartShoppingSessionUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(existingListId: String? = null): Result<ShoppingList> = runCatching {
        val now = getCurrentTime()

        if (existingListId != null) {
            val existingList = shoppingListRepository.getShoppingListById(existingListId).firstOrNull()
                ?: throw IllegalArgumentException("Lista não encontrada: $existingListId")

            require(!existingList.isDeleted) { "Não é possível iniciar compras em uma lista excluída" }

            val updatedList = existingList.copy(
                status = ShoppingListStatus.SHOPPING,
                updatedAt = now,
            )
            shoppingListRepository.updateShoppingListIfNewer(updatedList)
            updatedList
        } else {
            val newList = ShoppingList(
                id = Uuid.random().toString(),
                name = generateSessionTitle(now),
                type = ShoppingListType.ASSISTANT,
                status = ShoppingListStatus.SHOPPING,
                items = emptyList(),
                budgetInCents = null,
                updatedAt = now,
                isDeleted = false,
            )
            shoppingListRepository.insertShoppingList(newList)
            newList
        }
    }

    private fun generateSessionTitle(timestamp: Long): String {
        val dateTime = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        return "Compras de $day/$month"
    }
}
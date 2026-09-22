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

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateQuickShoppingListUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        rawContent: String,
        customTitle: String? = null,
    ): Result<ShoppingList> = runCatching {
        val lines = rawContent
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        require(lines.isNotEmpty()) { "A lista rápida deve conter pelo menos um item" }

        val now = getCurrentTime()
        val listId = Uuid.random().toString()
        val finalTitle = customTitle?.trim()?.takeIf { it.isNotBlank() } ?: generateDefaultTitle(now)

        val items = lines.map { line ->
            ShoppingItem(
                id = Uuid.random().toString(),
                listId = listId,
                productId = null, // Texto puro, sem vínculo inicial de catálogo
                rawText = line,
                quantity = 1.0,
                priceAtTime = null,
                isChecked = false,
                updatedAt = now,
                isDeleted = false,
            )
        }

        val shoppingList = ShoppingList(
            id = listId,
            name = finalTitle,
            type = ShoppingListType.SCRATCHPAD,
            status = ShoppingListStatus.DRAFT,
            items = items,
            budgetInCents = null,
            updatedAt = now,
            isDeleted = false,
        )

        shoppingListRepository.insertShoppingList(shoppingList)
        shoppingList
    }

    private fun generateDefaultTitle(timestamp: Long): String {
        val dateTime = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        return "Lista rápida de $day/$month"
    }
}
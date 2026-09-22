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
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddCatalogItemToShoppingListUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        listId: String,
        productId: String,
        quantity: Double = 1.0,
    ): Result<ShoppingItem> = runCatching {
        require(productId.isNotBlank()) { "ID do produto inválido" }
        require(quantity > 0.0) { "A quantidade deve ser maior que zero" }

        val currentList = shoppingListRepository.getShoppingListById(listId).firstOrNull()
            ?: throw IllegalArgumentException("Lista não encontrada: $listId")
        require(!currentList.isDeleted) { "Não é possível alterar uma lista que foi excluída" }

        val now = getCurrentTime()
        val existingItem = currentList.items.find { it.productId == productId && !it.isDeleted }

        if (existingItem != null) {
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + quantity,
                updatedAt = now,
            )
            shoppingListRepository.updateShoppingItemIfNewer(updatedItem)
            updatedItem
        } else {
            val newItem = ShoppingItem(
                id = Uuid.random().toString(),
                listId = listId,
                productId = productId,
                rawText = null,
                quantity = quantity,
                priceAtTime = null,
                isChecked = false,
                updatedAt = now,
                isDeleted = false,
            )
            shoppingListRepository.insertShoppingItem(newItem)
            newItem
        }
    }
}
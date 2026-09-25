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
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import kotlinx.coroutines.flow.firstOrNull

class ReplaceCartItemUseCase(
    private val shoppingListRepository: ShoppingListRepository,
) {
    suspend operator fun invoke(
        listId: String,
        itemId: String,
        newProductId: String?,
        newRawText: String? = null,
        quantity: Double? = null,
        priceAtTimeInCents: Long? = null,
    ): Result<ShoppingItem> = runCatching {
        quantity?.let {
            require(it > 0.0) { "A quantidade deve ser maior que zero" }
        }
        priceAtTimeInCents?.let {
            require(it >= 0L) { "O preço não pode ser negativo" }
        }
        require(!newProductId.isNullOrBlank() || !newRawText.isNullOrBlank()) {
            "O item substituto deve possuir um produto vinculado ou descrição em texto"
        }

        val currentList = shoppingListRepository.getShoppingListById(listId).firstOrNull()
            ?: throw IllegalArgumentException("Lista não encontrada: $listId")
        require(!currentList.isDeleted) { "Não é possível alterar uma lista que foi excluída" }
        require(currentList.status != ShoppingListStatus.COMPLETED) {
            "Não é possível substituir itens em uma lista já finalizada"
        }

        val itemToReplace = currentList.items.find { it.id == itemId && !it.isDeleted }
            ?: throw IllegalArgumentException("Item não encontrado no carrinho: $itemId")

        val now = getCurrentTime()
        val updated = itemToReplace.copy(
            productId = newProductId,
            rawText = newRawText,
            quantity = quantity ?: itemToReplace.quantity,
            priceAtTime = priceAtTimeInCents ?: itemToReplace.priceAtTime,
            isChecked = true,
            updatedAt = now,
        )

        shoppingListRepository.updateShoppingItemIfNewer(updated)
        updated
    }
}

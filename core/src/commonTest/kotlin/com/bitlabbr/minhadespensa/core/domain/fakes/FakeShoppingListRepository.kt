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

package com.bitlabbr.minhadespensa.core.domain.fakes

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeShoppingListRepository : ShoppingListRepository {
    val listsFlow = MutableStateFlow<Map<String, ShoppingList>>(emptyMap())
    var purchaseFinalizedListId: String? = null

    override fun getAllActiveShoppingLists(): Flow<List<ShoppingList>> =
        listsFlow.map { it.values.filter { list -> !list.isDeleted } }

    override fun getShoppingListById(listId: String): Flow<ShoppingList?> =
        listsFlow.map { it[listId] }

    override suspend fun insertShoppingList(shoppingList: ShoppingList) {
        listsFlow.value += (shoppingList.id to shoppingList)
    }

    override suspend fun forceUpdateShoppingList(shoppingList: ShoppingList) {
        listsFlow.value += (shoppingList.id to shoppingList)
    }

    override suspend fun updateShoppingListIfNewer(list: ShoppingList) {
        listsFlow.value += (list.id to list)
    }

    override suspend fun markShoppingListAsDeleted(listId: String, updatedAt: Long) {
        listsFlow.value[listId]?.let {
            listsFlow.value += (listId to it.copy(isDeleted = true, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteShoppingListById(listId: String) {
        listsFlow.value -= listId
    }

    override suspend fun insertShoppingItem(item: ShoppingItem) {
        val parent = listsFlow.value[item.listId] ?: return
        val updatedItems = parent.items + item
        listsFlow.value += (parent.id to parent.copy(items = updatedItems))
    }

    override suspend fun forceUpdateShoppingItem(item: ShoppingItem) {
        updateItemInternal(item)
    }

    override suspend fun updateShoppingItemIfNewer(item: ShoppingItem) {
        updateItemInternal(item)
    }

    override suspend fun toggleItemCheck(itemId: String, isChecked: Boolean) {
        listsFlow.value.values.forEach { list ->
            val found = list.items.find { it.id == itemId }
            if (found != null) {
                updateItemInternal(found.copy(isChecked = isChecked))
            }
        }
    }

    override suspend fun markShoppingItemAsDeleted(itemId: String, updatedAt: Long) {
        listsFlow.value.values.forEach { list ->
            val found = list.items.find { it.id == itemId }
            if (found != null) {
                updateItemInternal(found.copy(isDeleted = true, updatedAt = updatedAt))
            }
        }
    }

    override suspend fun deleteShoppingItemById(itemId: String) {
        listsFlow.value.values.forEach { list ->
            if (list.items.any { it.id == itemId }) {
                val updatedItems = list.items.filterNot { it.id == itemId }
                listsFlow.value += (list.id to list.copy(items = updatedItems))
            }
        }
    }

    override suspend fun finalizePurchase(listId: String) {
        purchaseFinalizedListId = listId
    }

    private fun updateItemInternal(item: ShoppingItem) {
        val parent = listsFlow.value[item.listId] ?: return
        val updated = parent.items.map { if (it.id == item.id) item else it }
        listsFlow.value += (parent.id to parent.copy(items = updated))
    }
}
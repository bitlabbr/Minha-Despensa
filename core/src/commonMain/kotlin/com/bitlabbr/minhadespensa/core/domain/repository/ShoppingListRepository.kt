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

package com.bitlabbr.minhadespensa.core.domain.repository

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getAllActiveShoppingLists(): Flow<List<ShoppingList>>
    fun getShoppingListById(listId: String): Flow<ShoppingList?>
    suspend fun insertShoppingList(shoppingList: ShoppingList)
    suspend fun forceUpdateForShoppingList(shoppingList: ShoppingList)
    suspend fun markShoppingListAsDeleted(listID: String, updatedAt: Long)
    suspend fun deleteShoppingListById(listID: String)
    suspend fun updateShoppingListIfNewer(list: ShoppingList)

    suspend fun insertShoppingItem(item: ShoppingItem)
    suspend fun forceUpdateForShoppingItem(item: ShoppingItem)
    suspend fun toggleItemCheck(id: String, isChecked: Boolean)
    suspend fun markAsDeleted(id: String)
    suspend fun finalizePurchase(listId: String)
    suspend fun updateShoppingItemIfNewer(item: ShoppingItem)
}
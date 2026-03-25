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

package com.bitlabbr.minhadespensa.data.local.dao

import androidx.room.*
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveShoppingLists(): Flow<List<ShoppingListWithItems>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    fun getShoppingListById(listId: String): Flow<ShoppingListWithItems?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertShoppingList(shoppingList: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun forceUpdateForShoppingList(shoppingList: ShoppingListEntity): Int

    @Query(
        """
    UPDATE shopping_lists 
    SET name = :name, budgetInCents = :budgetInCents, updatedAt = :updatedAt, isDeleted = :isDeleted
    WHERE id = :id AND updatedAt < :updatedAt
"""
    )
    suspend fun updateShoppingListIfNewer(
        id: String,
        name: String,
        budgetInCents: Long?,
        updatedAt: Long,
        isDeleted: Boolean
    ): Int

    @Query(
        """
        UPDATE shopping_lists 
        SET isDeleted = 1, updatedAt = :updatedAt 
        WHERE id = :listID
    """
    )
    suspend fun markShoppingListAsDeleted(listID: String, updatedAt: Long)

    @Query("DELETE FROM shopping_lists WHERE id = :listID")
    suspend fun deleteShoppingListById(listID: String)

    @Query("UPDATE shopping_lists SET updatedAt = :now WHERE id = :listId")
    suspend fun updateTimestamp(listId: String, now: Long)
}
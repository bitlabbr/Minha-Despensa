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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE is_deleted = 0")
    fun getActiveItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertShoppingItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items WHERE id = :itemId")
    fun findById(itemId: String): Flow<ShoppingItemEntity?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun forceUpdateItem(item: ShoppingItemEntity)

    @Query(
        """
    UPDATE shopping_items 
    SET product_id = :productId, quantity = :quantity, price_at_time = :priceAtTime, 
        is_checked = :isChecked, updated_at = :updatedAt, is_deleted = :isDeleted
    WHERE id = :id AND (
        updated_at < :updatedAt
        OR (updated_at = :updatedAt AND is_deleted = 0 AND :isDeleted = 1)
    )
"""
    )
    suspend fun updateItemIfNewer(
        id: String,
        productId: String?,
        quantity: Double,
        priceAtTime: Long?,
        isChecked: Boolean,
        updatedAt: Long,
        isDeleted: Boolean
    ): Int

    @Query("UPDATE shopping_items SET is_checked = :checked, updated_at = :now WHERE id = :id")
    suspend fun updateCheckStatus(id: String, checked: Boolean, now: Long)

    @Query("UPDATE shopping_items SET is_deleted = 1, updated_at = :now WHERE updated_at <= :now")
    suspend fun deleteAllLogical(now: Long)

    @Query("UPDATE shopping_items SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id AND updated_at <= :updatedAt")
    suspend fun markAsDeleted(id: String, updatedAt: Long)

    @Query("SELECT * FROM shopping_items WHERE is_checked = 1 AND is_deleted = 0")
    fun getCheckedItemsSync(): Flow<List<ShoppingItemEntity>>
}
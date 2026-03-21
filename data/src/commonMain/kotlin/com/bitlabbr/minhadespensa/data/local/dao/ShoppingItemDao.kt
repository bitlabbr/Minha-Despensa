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
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE isDeleted = 0")
    fun getActiveItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET isChecked = :checked, updatedAt = :now WHERE id = :id")
    suspend fun updateCheckStatus(id: String, checked: Boolean, now: Long)

    @Query("UPDATE shopping_items SET isDeleted = 1, updatedAt = :now")
    suspend fun deleteAllLogical(now: Long)

    @Query("UPDATE shopping_items SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long)

    @Query("SELECT * FROM shopping_items WHERE isChecked = 1 AND isDeleted = 0")
    suspend fun getCheckedItemsSync(): List<ShoppingItemEntity>
}
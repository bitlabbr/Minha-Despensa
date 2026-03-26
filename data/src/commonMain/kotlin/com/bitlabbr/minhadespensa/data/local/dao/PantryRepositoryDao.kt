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
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryRepositoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPantryItem(item: PantryItemEntity): Long

    @Query("SELECT * FROM pantry_items WHERE productId = :productId AND isDeleted = 0")
    fun getPantryItemsByProductID(productId: String): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE isDeleted = 0")
    fun getAllActivePantryItems(): Flow<List<PantryItemEntity>>

    @Query(
        """
        UPDATE pantry_items 
        SET isDeleted = 1, updatedAt = :updatedAt 
        WHERE id = :id
    """
    )
    suspend fun markPantryItemAsDeleted(id: String, updatedAt: Long)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun forceUpdatePantryItem(pantryItem: PantryItemEntity): Int

    @Query(
        """
    UPDATE pantry_items 
    SET productId = :productId, quantity = :quantity, updatedAt = :updatedAt, isDeleted = :isDeleted, expirationDate = :expirationDate, batchNumber = :batchNumber
    WHERE id = :id AND updatedAt < :updatedAt
"""
    )
    suspend fun updatePantryItemIfNewer(
        id: String,
        productId: String,
        quantity: Double,
        updatedAt: Long,
        isDeleted: Boolean,
        expirationDate: Long?,
        batchNumber: String?
    ): Int

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deletePantryItemById(id: String): Int

    @Query("SELECT * FROM pantry_items WHERE id = :pantryItemId")
    fun getPantryItemByID(pantryItemId: String): Flow<PantryItemEntity?>
}
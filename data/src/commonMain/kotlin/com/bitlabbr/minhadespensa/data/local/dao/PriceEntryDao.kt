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
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceEntry(entry: PriceEntryEntity): Long

    @Query("SELECT * FROM price_entries WHERE productId = :productId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getPriceHistoryByProductId(productId: String): Flow<List<PriceEntryEntity>>

    @Query("SELECT * FROM price_entries WHERE productId = :productId AND isDeleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestPriceForProductID(productId: String): Flow<PriceEntryEntity?>

    @Query("UPDATE price_entries SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPriceEntryAsDeletedById(id: String, updatedAt: Long)

    @Query("DELETE FROM price_entries WHERE id = :id")
    suspend fun deletePriceEntryById(id: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun forceUpdatePriceEntry(priceEntry: PriceEntryEntity): Int

    @Query(
        """
    UPDATE price_entries 
    SET productId = :productId, priceInCents = :priceInCents, storeName = :storeName, updatedAt = :updatedAt, isDeleted = :isDeleted
    WHERE id = :id AND updatedAt < :updatedAt
"""
    )
    suspend fun updatePriceEntryIfNewer(
        id: String,
        productId: String,
        priceInCents: Long,
        storeName: String?,
        updatedAt: Long,
        isDeleted: Boolean
    ): Int
}
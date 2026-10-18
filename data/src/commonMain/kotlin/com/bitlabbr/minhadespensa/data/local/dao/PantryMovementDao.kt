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
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import kotlinx.coroutines.flow.Flow

data class ProductConsumptionAggregate(
    val productId: String,
    val totalConsumed: Double
)

@Dao
interface PantryMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: PantryMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<PantryMovementEntity>)

    @Query(
        """
        SELECT * FROM pantry_movements
        WHERE updatedAt >= :startTimestamp AND updatedAt <= :endTimestamp AND is_deleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovementEntity>>

    @Query(
        """
        SELECT * FROM pantry_movements
        WHERE is_deleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun getAllMovements(): Flow<List<PantryMovementEntity>>

    @Query(
        """
        SELECT productId, SUM(quantity) AS totalConsumed
        FROM pantry_movements
        WHERE type = 'CONSUMPTION'
          AND updatedAt >= :startTimestamp AND updatedAt <= :endTimestamp
          AND is_deleted = 0
        GROUP BY productId
        ORDER BY totalConsumed DESC
        LIMIT :limit
        """
    )
    fun getMostConsumedProducts(startTimestamp: Long, endTimestamp: Long, limit: Int): Flow<List<ProductConsumptionAggregate>>

    @Query(
        """
        SELECT CAST(COALESCE(SUM(total_price_in_cents), 0) AS INTEGER)
        FROM pantry_movements
        WHERE type IN ('ENTRY_MANUAL', 'ENTRY_PURCHASE')
          AND total_price_in_cents IS NOT NULL
          AND total_price_in_cents > 0
          AND updatedAt >= :startOfMonth AND updatedAt <= :endOfMonth
          AND is_deleted = 0
        """
    )
    fun getMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Flow<Long>

    @Query(
        """
        SELECT CAST(COALESCE(SUM(total_price_in_cents), 0) AS INTEGER)
        FROM pantry_movements
        WHERE type IN ('ENTRY_MANUAL', 'ENTRY_PURCHASE')
          AND total_price_in_cents IS NOT NULL
          AND total_price_in_cents > 0
          AND updatedAt >= :startOfMonth AND updatedAt <= :endOfMonth
          AND is_deleted = 0
        """
    )
    suspend fun findMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Long
}

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
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: MonthlyBudgetEntity)

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month")
    fun getBudget(year: Int, month: Int): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month")
    suspend fun findBudget(year: Int, month: Int): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets WHERE year = :year ORDER BY month ASC")
    fun getBudgetsForYear(year: Int): Flow<List<MonthlyBudgetEntity>>

    @Query(
        """
        SELECT CAST(COALESCE(SUM(total_price_in_cents), 0) AS INTEGER)
        FROM pantry_movements
        WHERE type IN ('ENTRY_MANUAL', 'ENTRY_PURCHASE')
          AND total_price_in_cents IS NOT NULL
          AND total_price_in_cents > 0
          AND timestamp >= :startOfMonth AND timestamp <= :endOfMonth
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
          AND timestamp >= :startOfMonth AND timestamp <= :endOfMonth
          AND is_deleted = 0
        """
    )
    suspend fun findMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Long
}

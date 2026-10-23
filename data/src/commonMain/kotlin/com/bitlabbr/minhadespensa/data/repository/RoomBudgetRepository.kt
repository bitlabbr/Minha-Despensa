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

package com.bitlabbr.minhadespensa.data.repository

import com.bitlabbr.minhadespensa.core.domain.model.MonthlyBudget
import com.bitlabbr.minhadespensa.core.domain.model.MonthlyFinancialSummary
import com.bitlabbr.minhadespensa.core.domain.repository.BudgetRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.getMonthTimeRange
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomBudgetRepository(
    private val db: AppDatabase,
    private val logger: AppLogger,
    private val outboxEnqueuer: OutboxEnqueuer = OutboxEnqueuer(db.pendingSyncDao()),
) : BudgetRepository {

    private val TAG = "RoomBudgetRepository"
    private val dao = db.budgetDao()

    override fun getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?> {
        validateYearAndMonth(year, month)
        logger.d(TAG, "getBudgetForMonth: year=$year, month=$month")
        return dao.getBudget(year, month).map { it?.toDomain() }
    }

    override suspend fun setBudgetForMonth(year: Int, month: Int, budgetInCents: Long) {
        validateYearAndMonth(year, month)
        require(budgetInCents >= 0L) { "O orçamento não pode ser negativo" }
        logger.d(TAG, "setBudgetForMonth: year=$year, month=$month, budgetInCents=$budgetInCents")

        val now = getCurrentTime()
        val entity = MonthlyBudgetEntity(
            year = year,
            month = month,
            budgetInCents = budgetInCents,
            updatedAt = now,
        )
        dao.insertOrUpdate(entity)
        outboxEnqueuer.enqueueBudget(entity)
        logger.d(TAG, "setBudgetForMonth: Successfully saved budget for $year-$month ($budgetInCents cents)")
    }

    override fun getMonthlyExpenses(year: Int, month: Int): Flow<Long> {
        validateYearAndMonth(year, month)
        logger.d(TAG, "getMonthlyExpenses: year=$year, month=$month")
        val (start, end) = getMonthTimeRange(year, month)
        return dao.getMonthlyExpenses(start, end).distinctUntilChanged()
    }

    override fun getYearlySummary(year: Int): Flow<List<MonthlyFinancialSummary>> {
        require(year in 2000..2100) { "Ano inválido: $year" }
        logger.d(TAG, "getYearlySummary: year=$year")

        val months = (1..12).toList()
        val monthlyExpensesFlows = months.map { month ->
            getMonthlyExpenses(year, month)
        }

        val combinedExpensesFlow: Flow<List<Long>> = combine(monthlyExpensesFlows) { expensesArray ->
            expensesArray.toList()
        }

        return combine(
            dao.getBudgetsForYear(year),
            combinedExpensesFlow
        ) { budgetEntities, monthlyExpenses ->
            val budgetMap = budgetEntities.associateBy { it.month }
            months.map { month ->
                val budget = budgetMap[month]
                val expenses = monthlyExpenses[month - 1]
                MonthlyFinancialSummary(
                    year = year,
                    month = month,
                    budgetInCents = budget?.budgetInCents,
                    totalSpentInCents = expenses
                )
            }
        }
    }

    private fun validateYearAndMonth(year: Int, month: Int) {
        require(year in 2000..2100) { "Ano inválido: $year" }
        require(month in 1..12) { "Mês inválido: $month. Deve estar entre 1 e 12." }
    }
}

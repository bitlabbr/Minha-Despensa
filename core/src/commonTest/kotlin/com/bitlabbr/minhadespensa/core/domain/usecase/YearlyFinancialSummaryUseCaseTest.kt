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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.model.MonthlyBudget
import com.bitlabbr.minhadespensa.core.domain.model.MonthlyFinancialSummary
import com.bitlabbr.minhadespensa.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class YearlyFinancialSummaryUseCaseTest {

    private class LocalFakeBudgetRepository : BudgetRepository {
        val budgets = MutableStateFlow<Map<Pair<Int, Int>, MonthlyBudget>>(emptyMap())
        val expenses = MutableStateFlow<Map<Pair<Int, Int>, Long>>(emptyMap())

        override fun getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?> =
            budgets.map { it[Pair(year, month)] }

        override suspend fun setBudgetForMonth(year: Int, month: Int, budgetInCents: Long) {
            budgets.value += (Pair(year, month) to MonthlyBudget(year, month, budgetInCents, 0L))
        }

        override fun getMonthlyExpenses(year: Int, month: Int): Flow<Long> =
            expenses.map { it[Pair(year, month)] ?: 0L }

        override fun getYearlySummary(year: Int): Flow<List<MonthlyFinancialSummary>> =
            combine(budgets, expenses) { bMap, eMap ->
                (1..12).map { month ->
                    MonthlyFinancialSummary(
                        year = year,
                        month = month,
                        budgetInCents = bMap[Pair(year, month)]?.budgetInCents,
                        totalSpentInCents = eMap[Pair(year, month)] ?: 0L,
                    )
                }
            }
    }

    private val repository = LocalFakeBudgetRepository()
    private val useCase = GetYearlyFinancialSummaryUseCase(repository)

    @Test
    fun `when year is invalid should throw IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            useCase(1999)
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(2101)
        }
    }

    @Test
    fun `when year is valid should return 12 monthly summaries`() = runTest {
        repository.setBudgetForMonth(2026, 1, 100000L)
        repository.setBudgetForMonth(2026, 2, 120000L)
        val currentExpenses = repository.expenses.value.toMutableMap()
        currentExpenses[Pair(2026, 1)] = 85000L
        currentExpenses[Pair(2026, 2)] = 130000L
        repository.expenses.value = currentExpenses

        val list = useCase(2026).first()

        assertEquals(12, list.size)

        val jan = list[0]
        assertEquals(1, jan.month)
        assertEquals(100000L, jan.budgetInCents)
        assertEquals(85000L, jan.totalSpentInCents)
        assertEquals(15000L, jan.remainingInCents)
        assertEquals(0.85f, jan.consumptionProgress)

        val feb = list[1]
        assertEquals(2, feb.month)
        assertEquals(120000L, feb.budgetInCents)
        assertEquals(130000L, feb.totalSpentInCents)
        assertEquals(true, feb.isOverBudget)

        val dec = list[11]
        assertEquals(12, dec.month)
        assertEquals(null, dec.budgetInCents)
        assertEquals(0L, dec.totalSpentInCents)
    }
}

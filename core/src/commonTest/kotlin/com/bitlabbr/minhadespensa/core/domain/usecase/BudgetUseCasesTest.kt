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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeBudgetRepository : BudgetRepository {
    private val budgets = MutableStateFlow<Map<Pair<Int, Int>, MonthlyBudget>>(emptyMap())
    private val expenses = MutableStateFlow<Map<Pair<Int, Int>, Long>>(emptyMap())

    override fun getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?> {
        return budgets.map { it[Pair(year, month)] }
    }

    override suspend fun setBudgetForMonth(year: Int, month: Int, budgetInCents: Long) {
        val current = budgets.value.toMutableMap()
        current[Pair(year, month)] = MonthlyBudget(
            year = year,
            month = month,
            budgetInCents = budgetInCents,
            updatedAt = 1000L
        )
        budgets.value = current
    }

    override fun getMonthlyExpenses(year: Int, month: Int): Flow<Long> {
        return expenses.map { it[Pair(year, month)] ?: 0L }
    }

    fun emitExpenses(year: Int, month: Int, amountInCents: Long) {
        val current = expenses.value.toMutableMap()
        current[Pair(year, month)] = amountInCents
        expenses.value = current
    }

    override fun getYearlySummary(year: Int): Flow<List<MonthlyFinancialSummary>> {
        return budgets.map { budgetMap ->
            (1..12).map { month ->
                val budget = budgetMap[Pair(year, month)]
                val totalSpent = expenses.value[Pair(year, month)] ?: 0L
                MonthlyFinancialSummary(
                    year = year,
                    month = month,
                    budgetInCents = budget?.budgetInCents,
                    totalSpentInCents = totalSpent
                )
            }
        }
    }
}

class BudgetUseCasesTest {

    private val fakeBudgetRepository = FakeBudgetRepository()
    private val setMonthlyBudgetUseCase = SetMonthlyBudgetUseCase(fakeBudgetRepository)
    private val getMonthlyFinancialSummaryUseCase = GetMonthlyFinancialSummaryUseCase(fakeBudgetRepository)

    // -------------------------------------------------------------------------
    // SetMonthlyBudgetUseCase Tests
    // -------------------------------------------------------------------------

    @Test
    fun `SetMonthlyBudgetUseCase should successfully persist budget for valid inputs`() = runTest {
        val result = setMonthlyBudgetUseCase(year = 2026, month = 10, budgetInCents = 150000L)
        assertTrue(result.isSuccess)

        val savedBudget = fakeBudgetRepository.getBudgetForMonth(2026, 10).first()
        assertEquals(2026, savedBudget?.year)
        assertEquals(10, savedBudget?.month)
        assertEquals(150000L, savedBudget?.budgetInCents)
    }

    @Test
    fun `SetMonthlyBudgetUseCase should fail when budget is negative`() = runTest {
        val result = setMonthlyBudgetUseCase(year = 2026, month = 10, budgetInCents = -1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("O orçamento não pode ser negativo", result.exceptionOrNull()?.message)
    }

    @Test
    fun `SetMonthlyBudgetUseCase should fail when month is invalid`() = runTest {
        val resultZero = setMonthlyBudgetUseCase(year = 2026, month = 0, budgetInCents = 1000L)
        assertTrue(resultZero.isFailure)

        val resultThirteen = setMonthlyBudgetUseCase(year = 2026, month = 13, budgetInCents = 1000L)
        assertTrue(resultThirteen.isFailure)
    }

    @Test
    fun `SetMonthlyBudgetUseCase should fail when year is out of bounds`() = runTest {
        val resultPast = setMonthlyBudgetUseCase(year = 1999, month = 5, budgetInCents = 1000L)
        assertTrue(resultPast.isFailure)

        val resultFuture = setMonthlyBudgetUseCase(year = 2101, month = 5, budgetInCents = 1000L)
        assertTrue(resultFuture.isFailure)
    }

    // -------------------------------------------------------------------------
    // GetMonthlyFinancialSummaryUseCase Tests
    // -------------------------------------------------------------------------

    @Test
    fun `GetMonthlyFinancialSummaryUseCase should combine budget and expenses correctly`() = runTest {
        setMonthlyBudgetUseCase(year = 2026, month = 10, budgetInCents = 200000L) // R$ 2.000,00
        fakeBudgetRepository.emitExpenses(year = 2026, month = 10, amountInCents = 75000L) // R$ 750,00

        val summary = getMonthlyFinancialSummaryUseCase(year = 2026, month = 10).first()
        assertEquals(2026, summary.year)
        assertEquals(10, summary.month)
        assertEquals(200000L, summary.budgetInCents)
        assertEquals(75000L, summary.totalSpentInCents)
        assertEquals(125000L, summary.remainingInCents)
        assertEquals(0.375f, summary.consumptionProgress, 0.001f)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `GetMonthlyFinancialSummaryUseCase with no budget set should have null remaining and zero progress`() = runTest {
        fakeBudgetRepository.emitExpenses(year = 2026, month = 10, amountInCents = 50000L)

        val summary = getMonthlyFinancialSummaryUseCase(year = 2026, month = 10).first()
        assertNull(summary.budgetInCents)
        assertEquals(50000L, summary.totalSpentInCents)
        assertNull(summary.remainingInCents)
        assertEquals(0f, summary.consumptionProgress)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `GetMonthlyFinancialSummaryUseCase with zero expenses should report full budget remaining`() = runTest {
        setMonthlyBudgetUseCase(year = 2026, month = 10, budgetInCents = 100000L)

        val summary = getMonthlyFinancialSummaryUseCase(year = 2026, month = 10).first()
        assertEquals(100000L, summary.budgetInCents)
        assertEquals(0L, summary.totalSpentInCents)
        assertEquals(100000L, summary.remainingInCents)
        assertEquals(0f, summary.consumptionProgress)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `GetMonthlyFinancialSummaryUseCase with over budget expenses should report negative remaining and isOverBudget true`() = runTest {
        setMonthlyBudgetUseCase(year = 2026, month = 10, budgetInCents = 100000L)
        fakeBudgetRepository.emitExpenses(year = 2026, month = 10, amountInCents = 150000L)

        val summary = getMonthlyFinancialSummaryUseCase(year = 2026, month = 10).first()
        assertEquals(100000L, summary.budgetInCents)
        assertEquals(150000L, summary.totalSpentInCents)
        assertEquals(-50000L, summary.remainingInCents)
        assertEquals(1.5f, summary.consumptionProgress, 0.001f)
        assertTrue(summary.isOverBudget)
    }

    @Test
    fun `GetMonthlyFinancialSummaryUseCase should throw for invalid month or year`() {
        assertFailsWith<IllegalArgumentException> {
            getMonthlyFinancialSummaryUseCase(year = 2026, month = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthlyFinancialSummaryUseCase(year = 2026, month = 13)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthlyFinancialSummaryUseCase(year = 1999, month = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthlyFinancialSummaryUseCase(year = 2101, month = 1)
        }
    }
}

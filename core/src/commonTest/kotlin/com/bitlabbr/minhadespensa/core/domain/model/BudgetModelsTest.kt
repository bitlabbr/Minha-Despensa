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

package com.bitlabbr.minhadespensa.core.domain.model

import com.bitlabbr.minhadespensa.core.domain.util.getMonthTimeRange
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BudgetModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `MonthlyBudget serialization and deserialization should preserve all fields`() {
        val budget = MonthlyBudget(
            year = 2026,
            month = 10,
            budgetInCents = 150000L,
            updatedAt = 1728900000000L
        )

        val serialized = json.encodeToString(budget)
        val deserialized = json.decodeFromString<MonthlyBudget>(serialized)

        assertEquals(budget, deserialized)
        assertEquals(2026, deserialized.year)
        assertEquals(10, deserialized.month)
        assertEquals(150000L, deserialized.budgetInCents)
        assertEquals(1728900000000L, deserialized.updatedAt)
    }

    @Test
    fun `MonthlyFinancialSummary with valid budget under limit`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 100000L, // R$ 1.000,00
            totalSpentInCents = 60000L // R$ 600,00
        )

        assertEquals(40000L, summary.remainingInCents)
        assertEquals(0.6f, summary.consumptionProgress, 0.001f)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `MonthlyFinancialSummary with totalSpent reaching exact budget limit`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 100000L,
            totalSpentInCents = 100000L
        )

        assertEquals(0L, summary.remainingInCents)
        assertEquals(1.0f, summary.consumptionProgress, 0.001f)
        assertFalse(summary.isOverBudget, "Exato no teto não é considerado estouro")
    }

    @Test
    fun `MonthlyFinancialSummary with over budget`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 100000L,
            totalSpentInCents = 150000L
        )

        assertEquals(-50000L, summary.remainingInCents)
        assertEquals(1.5f, summary.consumptionProgress, 0.001f)
        assertTrue(summary.isOverBudget)
    }

    @Test
    fun `MonthlyFinancialSummary with null budget (sem orcamento definido)`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = null,
            totalSpentInCents = 50000L
        )

        assertNull(summary.remainingInCents)
        assertEquals(0f, summary.consumptionProgress)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `MonthlyFinancialSummary with zero expenses (gastos zerados)`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 200000L,
            totalSpentInCents = 0L
        )

        assertEquals(200000L, summary.remainingInCents)
        assertEquals(0f, summary.consumptionProgress)
        assertFalse(summary.isOverBudget)
    }

    @Test
    fun `MonthlyFinancialSummary with zero budget (orcamento zero)`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 0L,
            totalSpentInCents = 100L
        )

        assertEquals(-100L, summary.remainingInCents)
        assertEquals(0f, summary.consumptionProgress, "Orçamento zero não deve causar divisão por zero")
        assertTrue(summary.isOverBudget)
    }

    @Test
    fun `MonthlyFinancialSummary serialization and deserialization should preserve computed fields`() {
        val summary = MonthlyFinancialSummary(
            year = 2026,
            month = 10,
            budgetInCents = 100000L,
            totalSpentInCents = 75000L
        )

        val serialized = json.encodeToString(summary)
        val deserialized = json.decodeFromString<MonthlyFinancialSummary>(serialized)

        assertEquals(summary, deserialized)
        assertEquals(25000L, deserialized.remainingInCents)
        assertEquals(0.75f, deserialized.consumptionProgress, 0.001f)
        assertFalse(deserialized.isOverBudget)
    }

    @Test
    fun `getMonthTimeRange should correctly calculate start and end of month for standard months`() {
        val tz = TimeZone.UTC
        val (start, end) = getMonthTimeRange(year = 2026, month = 10, timeZone = tz)

        val startDate = Instant.fromEpochMilliseconds(start).toLocalDateTime(tz)
        assertEquals(2026, startDate.year)
        assertEquals(10, startDate.monthNumber)
        assertEquals(1, startDate.dayOfMonth)
        assertEquals(0, startDate.hour)
        assertEquals(0, startDate.minute)
        assertEquals(0, startDate.second)

        val endDate = Instant.fromEpochMilliseconds(end).toLocalDateTime(tz)
        assertEquals(2026, endDate.year)
        assertEquals(10, endDate.monthNumber)
        assertEquals(31, endDate.dayOfMonth)
        assertEquals(23, endDate.hour)
        assertEquals(59, endDate.minute)
        assertEquals(59, endDate.second)
    }

    @Test
    fun `getMonthTimeRange should correctly handle February leap and non-leap years`() {
        val tz = TimeZone.UTC

        // Non-leap year 2026 (28 days)
        val (_, endNonLeap) = getMonthTimeRange(year = 2026, month = 2, timeZone = tz)
        val endNonLeapDate = Instant.fromEpochMilliseconds(endNonLeap).toLocalDateTime(tz)
        assertEquals(28, endNonLeapDate.dayOfMonth)

        // Leap year 2024 (29 days)
        val (_, endLeap) = getMonthTimeRange(year = 2024, month = 2, timeZone = tz)
        val endLeapDate = Instant.fromEpochMilliseconds(endLeap).toLocalDateTime(tz)
        assertEquals(29, endLeapDate.dayOfMonth)
    }

    @Test
    fun `getMonthTimeRange should correctly handle December boundary transition to next year`() {
        val tz = TimeZone.UTC
        val (start, end) = getMonthTimeRange(year = 2026, month = 12, timeZone = tz)

        val startDate = Instant.fromEpochMilliseconds(start).toLocalDateTime(tz)
        assertEquals(2026, startDate.year)
        assertEquals(12, startDate.monthNumber)
        assertEquals(1, startDate.dayOfMonth)

        val endDate = Instant.fromEpochMilliseconds(end).toLocalDateTime(tz)
        assertEquals(2026, endDate.year)
        assertEquals(12, endDate.monthNumber)
        assertEquals(31, endDate.dayOfMonth)
        assertEquals(23, endDate.hour)
        assertEquals(59, endDate.minute)
        assertEquals(59, endDate.second)
    }

    @Test
    fun `getMonthTimeRange should throw IllegalArgumentException for invalid months or years`() {
        assertFailsWith<IllegalArgumentException> {
            getMonthTimeRange(year = 2026, month = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthTimeRange(year = 2026, month = 13)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthTimeRange(year = 1999, month = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            getMonthTimeRange(year = 2101, month = 5)
        }
    }
}

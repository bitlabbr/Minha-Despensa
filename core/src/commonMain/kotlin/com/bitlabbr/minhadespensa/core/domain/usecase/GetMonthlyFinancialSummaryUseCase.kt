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

import com.bitlabbr.minhadespensa.core.domain.model.MonthlyFinancialSummary
import com.bitlabbr.minhadespensa.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMonthlyFinancialSummaryUseCase(
    private val budgetRepository: BudgetRepository,
) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlyFinancialSummary> {
        require(year in 2000..2100) { "Ano inválido: $year" }
        require(month in 1..12) { "Mês inválido: $month. Deve estar entre 1 e 12." }

        return combine(
            budgetRepository.getBudgetForMonth(year, month),
            budgetRepository.getMonthlyExpenses(year, month)
        ) { budget, expenses ->
            MonthlyFinancialSummary(
                year = year,
                month = month,
                budgetInCents = budget?.budgetInCents,
                totalSpentInCents = expenses,
            )
        }
    }
}

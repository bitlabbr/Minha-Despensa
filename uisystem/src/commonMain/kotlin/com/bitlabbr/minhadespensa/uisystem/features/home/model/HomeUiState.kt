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

package com.bitlabbr.minhadespensa.uisystem.features.home.model

import androidx.compose.runtime.Immutable
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.ExpiringItemCard

@Immutable
data class HomeFinancialUiModel(
    val totalSpentInCents: Long = 0L,
    val budgetInCents: Long? = null,
    val consumptionProgress: Float = 0f,
    val currentConsumption: String = "R$ 0,00",
    val budget: String = "R$ 0,00",
    val targetChartLabel: String = "",
    val hasBudget: Boolean = false,
)

@Immutable
data class HomeUiState(
    val financialSummary: HomeFinancialUiModel = HomeFinancialUiModel(),
    val expiringItems: List<ExpiringItemCard> = emptyList(),
    val consumptionTrends: List<ConsumptionTrendItemUiModel> = emptyList(),
    val isSetBudgetDialogOpen: Boolean = false,
    val isSavingBudget: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

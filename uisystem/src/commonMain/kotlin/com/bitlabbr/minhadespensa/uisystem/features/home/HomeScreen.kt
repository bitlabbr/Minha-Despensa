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

package com.bitlabbr.minhadespensa.uisystem.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.ConsumptionTrendWidget
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.ExpiringSoonWidget
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.FinancialAnalyticsDialog
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.FinancialWidget
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.SetMonthlyBudgetDialog
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.general
import minhadespensa.uisystem.generated.resources.vision
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    bottomPadding: Dp = 0.dp,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {
            MinhaDespensaTopBar()
        },
    ) {
        PrimaryContainerHeader(
            textTop = stringResource(Res.string.vision),
            textBottom = stringResource(Res.string.general),
        )

        FinancialWidget(
            data = uiState.financialSummary,
            stockValue = uiState.pantryStockValue,
            onCardClick = { viewModel.openSetBudgetDialog() },
            onDefineBudgetClick = { viewModel.openSetBudgetDialog() },
            onOpenAnalytics = { viewModel.openFinancialAnalyticsDialog() },
        )

        ExpiringSoonWidget(
            items = uiState.expiringItems,
        )

        ConsumptionTrendWidget(
            items = uiState.consumptionTrends,
        )
    }

    if (uiState.isSetBudgetDialogOpen) {
        SetMonthlyBudgetDialog(
            currentBudgetInCents = uiState.financialSummary.budgetInCents,
            isSaving = uiState.isSavingBudget,
            onConfirm = { budgetInCents ->
                viewModel.saveMonthlyBudget(budgetInCents)
            },
            onDismiss = {
                viewModel.dismissSetBudgetDialog()
            },
        )
    }

    if (uiState.isFinancialAnalyticsDialogOpen) {
        FinancialAnalyticsDialog(
            stockValue = uiState.pantryStockValue,
            yearlyAnalytics = uiState.yearlyAnalytics,
            onDismiss = {
                viewModel.dismissFinancialAnalyticsDialog()
            },
        )
    }
}

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

package com.bitlabbr.minhadespensa.uisystem.features.home.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeMonthlyBarUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomePantryStockUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeYearlyAnalyticsUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.home_analytics_average_item_price
import minhadespensa.uisystem.generated.resources.home_analytics_close_button
import minhadespensa.uisystem.generated.resources.home_analytics_dialog_desc
import minhadespensa.uisystem.generated.resources.home_analytics_dialog_title
import minhadespensa.uisystem.generated.resources.home_analytics_evolution_section_title
import minhadespensa.uisystem.generated.resources.home_analytics_monthly_average
import minhadespensa.uisystem.generated.resources.home_analytics_no_budget_badge
import minhadespensa.uisystem.generated.resources.home_analytics_no_expenses_year
import minhadespensa.uisystem.generated.resources.home_analytics_over_budget_badge
import minhadespensa.uisystem.generated.resources.home_analytics_total_year_spent
import minhadespensa.uisystem.generated.resources.home_analytics_within_budget_badge
import minhadespensa.uisystem.generated.resources.placeholder_empty_metric
import org.jetbrains.compose.resources.stringResource

@Composable
fun FinancialAnalyticsDialog(
    stockValue: HomePantryStockUiModel,
    yearlyAnalytics: HomeYearlyAnalyticsUiModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = stringResource(Res.string.home_analytics_dialog_title, yearlyAnalytics.year),
        description = stringResource(Res.string.home_analytics_dialog_desc),
        buttons = {
            MinhaDespensaPrimaryButton(
                text = stringResource(Res.string.home_analytics_close_button),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Resumo Patrimonial do Estoque da Despensa
            StockValueCard(stockValue = stockValue)

            // 2. Indicadores do Ano (Totais & Médias)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YearlyMetricBox(
                    label = stringResource(Res.string.home_analytics_total_year_spent),
                    value = yearlyAnalytics.formattedTotalYearSpent,
                    modifier = Modifier.weight(1f),
                )
                YearlyMetricBox(
                    label = stringResource(Res.string.home_analytics_monthly_average),
                    value = yearlyAnalytics.formattedAverageMonthlySpent,
                    modifier = Modifier.weight(1f),
                )
                YearlyMetricBox(
                    label = stringResource(Res.string.home_analytics_average_item_price),
                    value = yearlyAnalytics.formattedAverageItemPrice ?: stringResource(Res.string.placeholder_empty_metric),
                    modifier = Modifier.weight(1f),
                )
            }

            // 3. Título da Seção de Evolução Mensal
            MinhaDespensaText(
                text = stringResource(Res.string.home_analytics_evolution_section_title),
                fontStyle = typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )

            // 4. Lista de 12 Meses com Progresso e Badges
            if (yearlyAnalytics.totalYearSpentInCents == 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.home_analytics_no_expenses_year),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    yearlyAnalytics.monthlySummaries.forEach { bar ->
                        MonthlyEvolutionBarRow(bar = bar)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceContainer.copy(alpha = 0.4f))
            .border(1.dp, colors.outline.copy(alpha = 0.15f), shape)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MinhaDespensaText(
                text = label,
                fontStyle = typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Normal,
            )
            MinhaDespensaText(
                text = value,
                fontStyle = typography.bodyLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MonthlyEvolutionBarRow(
    bar: HomeMonthlyBarUiModel,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MinhaDespensaText(
                    text = bar.monthName,
                    fontStyle = typography.bodyLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )

                MinhaDespensaText(
                    text = bar.formattedTotalSpent,
                    fontStyle = typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                )

                if (bar.hasBudget) {
                    MinhaDespensaText(
                        text = "/ ${bar.formattedBudget}",
                        fontStyle = typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.5f),
                    )
                }
            }

            // Status Badge
            val (badgeText, badgeBg, badgeTextColor) = when {
                bar.isOverBudget -> Triple(
                    stringResource(Res.string.home_analytics_over_budget_badge),
                    colors.error.copy(alpha = 0.15f),
                    colors.error,
                )
                bar.hasBudget -> Triple(
                    stringResource(Res.string.home_analytics_within_budget_badge),
                    colors.primary.copy(alpha = 0.15f),
                    colors.primary,
                )
                else -> Triple(
                    stringResource(Res.string.home_analytics_no_budget_badge),
                    colors.outline.copy(alpha = 0.15f),
                    colors.onSurface.copy(alpha = 0.6f),
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                MinhaDespensaText(
                    text = badgeText,
                    fontStyle = typography.bodySmall,
                    color = badgeTextColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Horizontal Progress Bar
        val barColor = if (bar.isOverBudget) colors.error else colors.primary
        val clampedProgress = minOf(1f, maxOf(0f, bar.progress))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colors.outline.copy(alpha = 0.15f)),
        ) {
            if (clampedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(barColor),
                )
            }
        }
    }
}

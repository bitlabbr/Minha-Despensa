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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.gauge.InverseAnchoredGauge
import com.bitlabbr.minhadespensa.uisystem.components.core.header.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeFinancialUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomePantryStockUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.financial_card_budget
import minhadespensa.uisystem.generated.resources.financial_card_current_consumption_label
import minhadespensa.uisystem.generated.resources.financial_card_title
import minhadespensa.uisystem.generated.resources.home_analytics_action_button
import minhadespensa.uisystem.generated.resources.home_financial_no_budget_hint
import minhadespensa.uisystem.generated.resources.home_financial_set_budget_cta
import minhadespensa.uisystem.generated.resources.home_stock_value_chip_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun FinancialWidget(
    data: HomeFinancialUiModel,
    modifier: Modifier = Modifier,
    stockValue: HomePantryStockUiModel? = null,
    onCardClick: () -> Unit = {},
    onDefineBudgetClick: () -> Unit = onCardClick,
    onOpenAnalytics: (() -> Unit)? = null,
) {
    val colors = getAppColors()
    val appDimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    SecondaryContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = data.hasBudget) { onCardClick() }
            .padding(horizontal = appDimens.paddingSmall),
        content = {
            SecondaryContainerHeader(
                text = stringResource(Res.string.financial_card_title),
                actionIcon = if (onOpenAnalytics != null) Icons.Rounded.Analytics else null,
                actionContentDescription = if (onOpenAnalytics != null) stringResource(Res.string.home_analytics_action_button) else null,
                onActionClick = onOpenAnalytics,
            )

            if (!data.hasBudget) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = appDimens.paddingSmall, vertical = appDimens.paddingSmall),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (data.totalSpentInCents > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MinhaDespensaText(
                                text = stringResource(Res.string.financial_card_current_consumption_label),
                                fontStyle = typography.bodyLarge,
                                color = colors.onSecondaryContainer,
                                fontWeight = FontWeight.Light,
                            )
                            MinhaDespensaText(
                                text = data.currentConsumption,
                                fontStyle = typography.priceLabel,
                                color = colors.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    MinhaDespensaText(
                        text = stringResource(Res.string.home_financial_no_budget_hint),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.home_financial_set_budget_cta),
                        onClick = onDefineBudgetClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                InverseAnchoredGauge(
                    progress = data.consumptionProgress,
                    gaugeHeight = 35.dp,
                    targetLabel = data.targetChartLabel,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = appDimens.paddingSmall,
                            vertical = appDimens.paddingMedium,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        MinhaDespensaText(
                            text = stringResource(Res.string.financial_card_current_consumption_label),
                            fontStyle = typography.displayMedium,
                            color = colors.onSecondaryContainer,
                            fontWeight = FontWeight.Light,
                        )
                        MinhaDespensaText(
                            text = data.currentConsumption,
                            fontStyle = typography.priceLabel,
                            color = colors.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.financial_card_budget),
                            fontStyle = typography.displayMedium,
                            color = colors.onSecondaryContainer,
                            fontWeight = FontWeight.Light,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinhaDespensaText(
                                text = data.budget,
                                fontStyle = typography.priceLabel,
                                color = colors.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            if (stockValue != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = appDimens.paddingSmall, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.onSecondaryContainer.copy(alpha = 0.08f))
                        .then(if (onOpenAnalytics != null) Modifier.clickable { onOpenAnalytics() } else Modifier)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Inventory2,
                                contentDescription = null,
                                tint = colors.onSecondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp),
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.home_stock_value_chip_label, stockValue.formattedTotalValue),
                                fontStyle = typography.bodySmall,
                                color = colors.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        if (onOpenAnalytics != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                MinhaDespensaText(
                                    text = stringResource(Res.string.home_analytics_action_button),
                                    fontStyle = typography.bodySmall,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * Sobrecarga de compatibilidade retroativa para HomeWidget.FinancialSummary
 */
@Composable
fun FinancialWidget(
    data: HomeWidget.FinancialSummary,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
) {
    FinancialWidget(
        data = HomeFinancialUiModel(
            consumptionProgress = data.consumptionProgress,
            targetChartLabel = data.consumptionTargetChartLabel,
            currentConsumption = data.currentConsumption,
            budget = data.budget,
            hasBudget = true,
        ),
        modifier = modifier,
        onCardClick = onCardClick,
    )
}

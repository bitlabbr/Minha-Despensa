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

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.gauge.InverseAnchoredGauge
import com.bitlabbr.minhadespensa.uisystem.components.core.header.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.financial_card_budget
import minhadespensa.uisystem.generated.resources.financial_card_current_consumption_label
import minhadespensa.uisystem.generated.resources.financial_card_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun FinancialWidget(data: HomeWidget.FinancialSummary) {
    val colors = getAppColors()
    val appDimens = MinhaDespensaTheme.dimens

    val consumptionProgress = data.consumptionProgress
    val currentConsumption = data.currentConsumption
    val targetChartLabel = data.consumptionTargetChartLabel
    val budget = data.budget

    SecondaryContainerGlassCard(
        content = {
            SecondaryContainerHeader(
                text = stringResource(Res.string.financial_card_title)
            )
            InverseAnchoredGauge(
                progress = consumptionProgress,
                gaugeHeight = 35.dp,
                targetLabel = targetChartLabel
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = appDimens.paddingSmall, vertical = appDimens.paddingMedium
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    MinhaDespensaText(
                        text = stringResource(Res.string.financial_card_current_consumption_label),
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Light
                    )
                    MinhaDespensaText(
                        text = currentConsumption,
                        fontStyle = MinhaDespensaTheme.typography.priceLabel,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    MinhaDespensaText(
                        text = stringResource(Res.string.financial_card_budget),
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Light
                    )
                    MinhaDespensaText(
                        text = budget,
                        fontStyle = MinhaDespensaTheme.typography.priceLabel,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.InverseAnchoredGauge
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.financialGaugePrimaryColor
import com.bitlabbr.minhadespensa.uisystem.theme.financialGaugeSecondaryColor

@Composable
fun FinancialCard(data: HomeWidget.FinancialSummary) {
    val colors = MinhaDespensaTheme.color
    val appDimens = MinhaDespensaTheme.dimens
    SecondaryContainerGlassCard(
        content = listOf {
            SecondaryContainerHeader(
                text = "VALOR GASTO NO MêS"
            )
            InverseAnchoredGauge(
                colorPrimary = financialGaugePrimaryColor,
                colorSecondary = financialGaugeSecondaryColor,
                progress = 0.85f,
                gaugeHeight = 35.dp,
                targetLabel = "R$1000,00"
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
                    CustomText(
                        text = "Gasto Atual:",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Light
                    )
                    CustomText(
                        text = "R$850,00",
                        fontStyle = MinhaDespensaTheme.typography.priceLabel,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    CustomText(
                        text = "Orçamento:",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Light
                    )
                    CustomText(
                        text = "R$1000,00",
                        fontStyle = MinhaDespensaTheme.typography.priceLabel,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

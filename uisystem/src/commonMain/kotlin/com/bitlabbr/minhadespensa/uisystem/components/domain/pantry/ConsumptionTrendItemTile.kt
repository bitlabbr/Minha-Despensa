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

package com.bitlabbr.minhadespensa.uisystem.components.domain.pantry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun ConsumptionTrendItemTile(
    productName: String,
    productCategory: String,
    productMeasureUnity: String,
    consumptionAmount: String,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    SecondaryContainerGlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingMedium, vertical = dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            // Icon
            if (iconPainter != null) {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(dimens.cardCorner * 0.35f)),
                )
            }

            // Title and category
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                MinhaDespensaText(
                    text = productName,
                    fontStyle = typography.bodyLarge,
                    color = colors.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                MinhaDespensaText(
                    text = productCategory,
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Light,
                )
            }

            // Amount and unity
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                MinhaDespensaText(
                    text = consumptionAmount,
                    fontStyle = typography.displayMedium,
                    color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Bold,
                )
                MinhaDespensaText(
                    text = productMeasureUnity,
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}
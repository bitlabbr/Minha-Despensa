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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun PantryItemTile(
    productName: String,
    itemCount: String,
    iconPainter: Painter? = null,
    containerColor: Color = getAppColors().primaryContainer,
    contentColor: Color = getAppColors().onPrimaryContainer,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall)

) {
    val shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner)
    val isDark = isSystemInDarkTheme()
    val primaryAlpha = if (isDark) 0.6f else 0.8f
    val secondaryAlpha = if (isDark) 0.3f else 0.6f

    val glassBrush = Brush.linearGradient(
        colors = listOf(
            containerColor.copy(alpha = primaryAlpha),
            containerColor.copy(alpha = secondaryAlpha)
        ),
        start = Offset(0f, .45f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            containerColor,
            Color.Transparent,
            containerColor,
            Color.Transparent,
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassBrush)
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MinhaDespensaTheme.dimens.paddingMedium,
                    end = MinhaDespensaTheme.dimens.paddingMedium
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ICON
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .height(80.dp)
                            .padding(
                                top = MinhaDespensaTheme.dimens.paddingSmall,
                                bottom = MinhaDespensaTheme.dimens.paddingSmall,
                            )
                            .clip(RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner * 0.4f))
                    )
                }
            }

            // CONTENT
            Column(
                modifier = Modifier.padding(end = MinhaDespensaTheme.dimens.paddingSmall).weight(2f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MinhaDespensaTheme.dimens.paddingSmall),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MinhaDespensaText(
                        text = productName,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall * 0.75f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MinhaDespensaText(
                        text = "",
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MinhaDespensaText(
                    text = itemCount,
                    fontStyle = MinhaDespensaTheme.typography.bodyLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                MinhaDespensaText(
                    text = "Un",
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = contentColor,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }

}
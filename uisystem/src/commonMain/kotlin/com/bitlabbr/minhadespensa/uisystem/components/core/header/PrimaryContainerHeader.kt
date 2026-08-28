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

package com.bitlabbr.minhadespensa.uisystem.components.core.header

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors


@Composable
fun PrimaryContainerHeader(
    textTop: String,
    textBottom: String? = null,
    description: String? = null,
    modifier: Modifier = Modifier,
    textColor: Color = getAppColors().onPrimaryContainer,
    actionIcon: ImageVector? = null,
    actionContentDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimens.paddingMedium,
                end = dimens.paddingSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            MinhaDespensaText(
                text = textTop,
                color = textColor,
                fontStyle = typography.displayLarge,
                fontWeight = FontWeight.Light,
            )

            if (!textBottom.isNullOrBlank()) {
                MinhaDespensaText(
                    text = textBottom,
                    color = textColor,
                    fontStyle = typography.displayLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (!description.isNullOrBlank()) {
                MinhaDespensaText(
                    text = description,
                    color = textColor.copy(alpha = 0.65f),
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Light,
                )
            }
        }

        when {
            trailingContent != null -> {
                trailingContent()
            }

            actionIcon != null && onActionClick != null -> {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionContentDescription,
                        tint = textColor.copy(alpha = 0.72f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}
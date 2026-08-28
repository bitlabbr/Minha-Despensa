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

package com.bitlabbr.minhadespensa.uisystem.components.core.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun SecondaryContainerSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = getAppColors().onSecondaryContainer.copy(alpha = 0.75f),
    verticalSpacing: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    SecondaryContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.paddingSmall,
                vertical = dimens.paddingSmall,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.paddingSmall / 2,
                    vertical = dimens.paddingSmall,
                ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            if (!title.isNullOrBlank()) {
                MinhaDespensaText(
                    text = title,
                    color = titleColor,
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}
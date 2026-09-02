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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun ItemContainerGlassCard(
    modifier: Modifier = Modifier
        .wrapContentSize(),
    shape: Shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner * 0.75f),
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseGlassCard(
        modifier = modifier,
        backgroundBrush = GlassCardDefaults.itemGlassBrush(),
        borderBrush = GlassCardDefaults.itemBorderBrush(),
        shape = shape,
        borderWidth = borderWidth,
    ) {
        Column(
            modifier = Modifier
                .padding(MinhaDespensaTheme.dimens.paddingSmall)
                .align(Alignment.TopCenter),
            content = content,
        )
    }
}
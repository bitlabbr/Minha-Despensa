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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.primaryContainerDarkAppColor
import com.bitlabbr.minhadespensa.uisystem.theme.primaryContainerLightAppColor

@Composable
fun PrimaryContainerGlassCard(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = MinhaDespensaTheme.dimens.paddingSmall,
            vertical = MinhaDespensaTheme.dimens.paddingSmall
        ),
    shape: Shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner),
    borderWidth: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {

    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) primaryContainerDarkAppColor else primaryContainerLightAppColor
    val primaryAlpha = if (isDark) 0.9f else 0.9f
    val secondaryAlpha = if (isDark) 0.5f else 0.8f
    val borderColor = if (isDark) primaryContainerDarkAppColor else Color.White

    val glassBrush = Brush.linearGradient(
        colors = listOf(
            tintColor.copy(alpha = primaryAlpha),
            tintColor.copy(alpha = secondaryAlpha)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            borderColor,
            Color.Transparent,
            borderColor,
            Color.Transparent,
            borderColor,
            Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassBrush)
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = shape
            ),
        content = content
    )
}

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

package com.bitlabbr.minhadespensa.uisystem.components.core.button

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.actionGradient
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.theme.onPrimaryLightAppColor

object MinhaDespensaButtonDefaults {

    val MinHeight: Dp = 48.dp
    val ExpandButtonHeight: Dp = 34.dp

    @Composable
    fun primaryShape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.6f)
    }

    @Composable
    fun secondaryShape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.6f)
    }

    val ExpandShape: Shape = CircleShape

    @Composable
    fun primaryActionBrush(): Brush = actionGradient()

    @Composable
    fun primaryTextColor(): Color = onPrimaryLightAppColor

    @Composable
    fun secondaryContainerColor(
        isDark: Boolean = isSystemInDarkTheme()
    ): Color {
        val colors = getAppColors()
        return if (isDark) {
            colors.onSurface.copy(alpha = 0.08f)
        } else {
            colors.onSurface.copy(alpha = 0.06f)
        }
    }

    @Composable
    fun secondaryBorderBrush(
        isDark: Boolean = isSystemInDarkTheme()
    ): Brush {
        val strokeColor = if (isDark) {
            Color.White.copy(alpha = 0.18f)
        } else {
            getAppColors().onSurface.copy(alpha = 0.12f)
        }

        return Brush.linearGradient(
            colors = listOf(
                strokeColor,
                strokeColor.copy(alpha = strokeColor.alpha * 0.3f),
            ),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    @Composable
    fun expandButtonContainerColor(
        isDark: Boolean = isSystemInDarkTheme()
    ): Color {
        val colors = getAppColors()
        return if (isDark) {
            colors.secondaryContainer.copy(alpha = 0.40f)
        } else {
            colors.secondaryContainer.copy(alpha = 0.65f)
        }
    }
}
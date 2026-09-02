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

package com.bitlabbr.minhadespensa.uisystem.components.core.snackbar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object MinhaDespensaSnackbarDefaults {

    @Composable
    fun shape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.65f)
    }

    @Composable
    fun accentColor(type: MinhaDespensaSnackbarType): Color {
        val colors = getAppColors()
        return when (type) {
            MinhaDespensaSnackbarType.SUCCESS -> colors.primary
            MinhaDespensaSnackbarType.ERROR -> colors.error
            MinhaDespensaSnackbarType.WARNING -> Color(0xFFF59E0B)
            MinhaDespensaSnackbarType.INFO -> colors.secondary
        }
    }

    @Composable
    fun backgroundBrush(
        isDark: Boolean = isSystemInDarkTheme(),
        containerColor: Color = getAppColors().surface,
    ): Brush {
        val alphaMiddle = if (isDark) 0.80f else 0.90f
        val alphaBottom = if (isDark) 0.40f else 0.50f
        return Brush.linearGradient(
            colors = listOf(
                containerColor,
                containerColor.copy(alpha = alphaMiddle),
                containerColor.copy(alpha = alphaBottom),
            ),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    @Composable
    fun borderBrush(accentColor: Color): Brush {
        return Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.75f),
                accentColor.copy(alpha = 0.55f),
                accentColor.copy(alpha = 0.25f),
                accentColor.copy(alpha = 0.15f),
            ),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }
}
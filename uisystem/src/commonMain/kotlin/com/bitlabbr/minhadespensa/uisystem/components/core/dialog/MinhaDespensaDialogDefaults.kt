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

package com.bitlabbr.minhadespensa.uisystem.components.core.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object MinhaDespensaDialogDefaults {

    val Elevation: Dp = 12.dp

    val properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
    )

    @Composable
    fun shape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.75f)
    }

    @Composable
    fun backgroundBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        val colors = getAppColors()
        val baseColor = colors.surface

        val primaryAlpha = if (isDark) 0.95f else 0.98f
        val secondaryAlpha = if (isDark) 0.92f else 0.96f

        return Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = primaryAlpha),
                baseColor.copy(alpha = secondaryAlpha),
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    @Composable
    fun borderStroke(isDark: Boolean = isSystemInDarkTheme()): BorderStroke {
        val strokeColor = if (isDark) {
            Color.White.copy(alpha = 0.16f)
        } else {
            getAppColors().onSurface.copy(alpha = 0.10f)
        }
        return BorderStroke(1.dp, strokeColor)
    }
}
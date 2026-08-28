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

package com.bitlabbr.minhadespensa.uisystem.components.core.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object ImagePickerDefaults {

    val PreviewImageSize: Dp = 160.dp
    val EmptyStateHeight: Dp = 120.dp

    @Composable
    fun cardShape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.55f)
    }

    @Composable
    fun imageShape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.45f)
    }

    @Composable
    fun borderStroke(isDark: Boolean = isSystemInDarkTheme()): BorderStroke {
        val colors = getAppColors()
        val strokeColor = if (isDark) {
            colors.onSecondaryContainer.copy(alpha = 0.20f)
        } else {
            colors.onSecondaryContainer.copy(alpha = 0.15f)
        }
        return BorderStroke(1.dp, strokeColor)
    }

    @Composable
    fun containerColor(isDark: Boolean = isSystemInDarkTheme()): Color {
        val colors = getAppColors()
        return if (isDark) {
            colors.secondaryContainer.copy(alpha = 0.40f)
        } else {
            colors.secondaryContainer.copy(alpha = 0.60f)
        }
    }
}
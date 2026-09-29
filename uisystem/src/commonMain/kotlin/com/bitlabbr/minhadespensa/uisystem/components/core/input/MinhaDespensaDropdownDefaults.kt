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

package com.bitlabbr.minhadespensa.uisystem.components.core.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object MinhaDespensaDropdownDefaults {

    val Elevation: Dp = 8.dp

    @Composable
    fun menuShape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.6f)
    }

    @Composable
    fun containerColor(): Color {
        return getAppColors().surface.copy(alpha = 0.96f)
    }

    @Composable
    fun borderStroke(): BorderStroke {
        val colors = getAppColors()
        return BorderStroke(
            width = 1.dp,
            color = colors.onSecondaryContainer.copy(alpha = 0.15f),
        )
    }

    @Composable
    fun itemColors(): MenuItemColors {
        val colors = getAppColors()
        return MenuDefaults.itemColors(
            textColor = colors.onSecondaryContainer,
            leadingIconColor = colors.onSecondaryContainer,
            trailingIconColor = colors.onSecondaryContainer,
        )
    }
}
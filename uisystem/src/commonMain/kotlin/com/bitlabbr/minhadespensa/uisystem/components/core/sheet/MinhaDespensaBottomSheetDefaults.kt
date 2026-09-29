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

package com.bitlabbr.minhadespensa.uisystem.components.core.sheet

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object MinhaDespensaBottomSheetDefaults {

    @Composable
    fun containerColor(): Color {
        return getAppColors().surface.copy(alpha = 0.96f)
    }

    @Composable
    fun shape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(
            topStart = dimens.cardCorner,
            topEnd = dimens.cardCorner,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )
    }

    @Composable
    fun dragHandleColor(): Color {
        return getAppColors().onSurface.copy(alpha = 0.24f)
    }
}
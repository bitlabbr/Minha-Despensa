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

package com.bitlabbr.minhadespensa.uisystem.components.core.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object MinhaDespensaChipDefaults {

    @Composable
    fun filterChipColors(): SelectableChipColors {
        val colors = getAppColors()

        return FilterChipDefaults.filterChipColors(
            containerColor = colors.surface.copy(alpha = 0.40f),
            labelColor = colors.onSecondaryContainer,
            iconColor = colors.onSecondaryContainer.copy(alpha = 0.70f),

            selectedContainerColor = colors.primary.copy(alpha = 0.5f),
            selectedLabelColor = colors.onPrimary,
            selectedLeadingIconColor = colors.onPrimary,
            selectedTrailingIconColor = colors.onPrimary,

            disabledContainerColor = colors.surface.copy(alpha = 0.15f),
            disabledLabelColor = colors.onSecondaryContainer.copy(alpha = 0.38f),
        )
    }

    @Composable
    fun shape(): Shape {
        val dimens = MinhaDespensaTheme.dimens
        return RoundedCornerShape(dimens.cardCorner * 0.45f)
    }

    @Composable
    fun border(selected: Boolean): BorderStroke? {
        val colors = getAppColors()
        return if (selected) {
            null
        } else {
            BorderStroke(1.dp, colors.onSecondaryContainer.copy(alpha = 0.12f))
        }
    }
}
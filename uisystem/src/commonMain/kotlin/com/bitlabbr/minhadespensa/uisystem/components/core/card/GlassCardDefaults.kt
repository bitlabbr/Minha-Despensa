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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object GlassCardDefaults {

    @Composable
    fun primaryGlassBrush(
        isDark: Boolean = isSystemInDarkTheme(),
        containerColor: Color = getAppColors().primaryContainer,
    ): Brush {
        val primaryAlpha = if (isDark) 0.85f else 0.95f
        val secondaryAlpha = if (isDark) 0.50f else 0.70f
        return Brush.linearGradient(
            colors = listOf(
                containerColor.copy(alpha = primaryAlpha),
                containerColor.copy(alpha = secondaryAlpha),
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    @Composable
    fun secondaryGlassBrush(
        isDark: Boolean = isSystemInDarkTheme(),
        containerColor: Color = getAppColors().secondaryContainer,
    ): Brush {
        val primaryAlpha = if (isDark) 0.40f else 0.50f
        val secondaryAlpha = if (isDark) 0.10f else 0.20f

        return Brush.linearGradient(
            colors = listOf(
                containerColor.copy(alpha = primaryAlpha),
                containerColor.copy(alpha = secondaryAlpha),
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    @Composable
    fun primaryBorderBrush(
        isDark: Boolean = isSystemInDarkTheme(),
    ): Brush {
        val colors = getAppColors()
        val highlightColor = if (isDark) {
            Color.White.copy(alpha = 0.35f)
        } else {
            colors.onPrimaryContainer.copy(alpha = 0.18f)
        }
        val shadowColor = if (isDark) {
            Color.Transparent
        } else {
            colors.onPrimaryContainer.copy(alpha = 0.05f)
        }
        return Brush.linearGradient(
            colors = listOf(
                highlightColor,
                shadowColor,
                highlightColor.copy(alpha = highlightColor.alpha * 0.5f),
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    @Composable
    fun secondaryBorderBrush(
        isDark: Boolean = isSystemInDarkTheme(),
    ): Brush {
        val colors = getAppColors()

        val strokeColor = if (isDark) {
            Color.White.copy(alpha = 0.15f)
        } else {
            colors.onSecondaryContainer.copy(alpha = 0.10f)
        }
        return Brush.linearGradient(
            colors = listOf(
                strokeColor,
                Color.Transparent,
                strokeColor.copy(alpha = 0.05f),
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }
}
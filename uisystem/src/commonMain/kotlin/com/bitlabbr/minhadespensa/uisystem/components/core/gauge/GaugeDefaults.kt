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

package com.bitlabbr.minhadespensa.uisystem.components.core.gauge

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.actionGradient
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

object GaugeDefaults {

    val BudgetGaugeSize: Dp = 100.dp
    val BudgetStrokeWidth: Dp = 12.dp
    val LinearGaugeHeight: Dp = 10.dp
    val CompactLinearGaugeHeight: Dp = 8.dp
    val TraceStrokeWidth: Dp = 2.dp
    val TraceOverflow: Dp = 4.dp
    val OverbudgetSpacing: Dp = 18.dp

    const val StartAngle: Float = 140f
    const val SweepAngle: Float = 260f
    const val WarningThreshold: Float = 0.60f
    const val DangerThreshold: Float = 0.85f

    val StrokeCapStyle: StrokeCap = StrokeCap.Round

    @Composable
    fun budgetTrackColor(isDark: Boolean = isSystemInDarkTheme()): Color {
        return if (isDark) {
            Color.White.copy(alpha = 0.12f)
        } else {
            getAppColors().onSurface.copy(alpha = 0.10f)
        }
    }

    @Composable
    fun budgetStatusColor(progress: Float): Color {
        val colors = getAppColors()
        return when {
            progress < WarningThreshold -> colors.primary
            progress < DangerThreshold -> colors.secondary
            else -> colors.error
        }
    }

    @Composable
    fun budgetProgressBrush(progress: Float): Brush {
        val statusColor = budgetStatusColor(progress)
        return Brush.sweepGradient(
            listOf(statusColor.copy(alpha = 0.40f), statusColor)
        )
    }

    @Composable
    fun linearTrailColor(isDark: Boolean = isSystemInDarkTheme()): Color {
        val colors = getAppColors()
        return if (isDark) {
            colors.onPrimaryContainer.copy(alpha = 0.20f)
        } else {
            Color.Black.copy(alpha = 0.12f)
        }
    }

    @Composable
    fun linearProgressBrush(): Brush {
        val colors = getAppColors()
        return actionGradient(
            primary = colors.primary,
            secondary = colors.secondary,
        )
    }

    fun linearProgressBrush(primary: Color, secondary: Color): Brush {
        return Brush.horizontalGradient(listOf(primary, secondary))
    }

    @Composable
    fun traceColor(): Color = getAppColors().primary

    @Composable
    fun traceLabelColor(): Color = getAppColors().onPrimaryContainer

    @Composable
    fun traceLabelStyle(): TextStyle {
        return MinhaDespensaTheme.typography.bodySmall.copy(
            color = traceLabelColor(),
            fontWeight = FontWeight.Light,
            fontSize = 9.sp,
        )
    }
}
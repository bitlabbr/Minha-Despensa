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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun BudgetGauge(
    progress: Float,
    modifier: Modifier = Modifier.size(GaugeDefaults.BudgetGaugeSize),
    strokeWidth: Dp = GaugeDefaults.BudgetStrokeWidth,
    trackColor: Color = GaugeDefaults.budgetTrackColor(),
    progressBrush: Brush = GaugeDefaults.budgetProgressBrush(progress),
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()

        drawArc(
            color = trackColor,
            startAngle = GaugeDefaults.StartAngle,
            sweepAngle = GaugeDefaults.SweepAngle,
            useCenter = false,
            style = Stroke(width = strokePx, cap = GaugeDefaults.StrokeCapStyle),
        )

        drawArc(
            brush = progressBrush,
            startAngle = GaugeDefaults.StartAngle,
            sweepAngle = GaugeDefaults.SweepAngle * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = Stroke(width = strokePx, cap = GaugeDefaults.StrokeCapStyle),
        )
    }
}
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InverseAnchoredGauge(
    progress: Float,
    targetLabel: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    gaugeHeight: Dp = GaugeDefaults.LinearGaugeHeight,
    trailColor: Color = GaugeDefaults.linearTrailColor(),
    progressBrush: Brush = GaugeDefaults.linearProgressBrush(),
    traceColor: Color = GaugeDefaults.traceColor(),
    labelStyle: TextStyle = GaugeDefaults.traceLabelStyle(),
) {
    val textMeasurer = rememberTextMeasurer()
    val isOverbudget = progress > 1.0f

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .height(gaugeHeight),
        ) {
            val width = size.width
            val height = size.height
            val cornerRadius = CornerRadius(height / 2, height / 2)
            val overflow = GaugeDefaults.TraceOverflow.toPx()
            val traceStroke = GaugeDefaults.TraceStrokeWidth.toPx()

            drawRoundRect(
                color = trailColor,
                size = size,
                cornerRadius = cornerRadius,
            )

            val barWidth = if (isOverbudget) width else (width * progress.coerceIn(0f, 1f))
            drawRoundRect(
                brush = progressBrush,
                size = Size(width = barWidth, height = height),
                cornerRadius = cornerRadius,
            )

            if (isOverbudget) {
                val traceX = width * (1.0f / progress)
                drawLine(
                    color = traceColor,
                    start = Offset(x = traceX, y = -overflow),
                    end = Offset(x = traceX, y = height + overflow),
                    strokeWidth = traceStroke,
                    cap = GaugeDefaults.StrokeCapStyle,
                )

                val textLayoutResult = textMeasurer.measure(
                    text = targetLabel,
                    style = labelStyle,
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = traceX - (textLayoutResult.size.width / 2),
                        y = height + overflow + 2.dp.toPx(),
                    ),
                )
            }
        }

        if (isOverbudget) {
            Spacer(modifier = Modifier.height(GaugeDefaults.OverbudgetSpacing))
        }
    }
}

@Composable
fun InverseAnchoredGauge(
    progress: Float,
    targetLabel: String,
    colorPrimary: Color,
    colorSecondary: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    gaugeHeight: Dp = GaugeDefaults.LinearGaugeHeight,
    trailColor: Color = GaugeDefaults.linearTrailColor(),
    traceColor: Color = GaugeDefaults.traceColor(),
    labelStyle: TextStyle = GaugeDefaults.traceLabelStyle(),
) {
    InverseAnchoredGauge(
        progress = progress,
        targetLabel = targetLabel,
        modifier = modifier,
        gaugeHeight = gaugeHeight,
        trailColor = trailColor,
        progressBrush = GaugeDefaults.linearProgressBrush(colorPrimary, colorSecondary),
        traceColor = traceColor,
        labelStyle = labelStyle,
    )
}
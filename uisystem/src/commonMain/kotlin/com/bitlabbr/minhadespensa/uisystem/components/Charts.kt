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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.financialGaugePrimaryColor
import com.bitlabbr.minhadespensa.uisystem.theme.financialGaugeSecondaryColor


@Composable
fun BudgetGauge(
    progress: Float,
    modifier: Modifier = Modifier.size(100.dp)
) {
    val colorPrimary = MinhaDespensaTheme.color.primary
    val colorWarning = MinhaDespensaTheme.color.onPrimary
    val colorError = MinhaDespensaTheme.color.error

    Canvas(modifier = modifier) {
        drawArc(
            color = Color.White.copy(alpha = 0.1f),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        val statusColor = when {
            progress < 0.6f -> colorPrimary
            progress < 0.85f -> colorWarning
            else -> colorError
        }

        drawArc(
            brush = Brush.sweepGradient(
                listOf(statusColor.copy(alpha = 0.5f), statusColor)
            ),
            startAngle = 140f,
            sweepAngle = 260f * progress,
            useCenter = false,
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


@Composable
fun InverseAnchoredGauge(
    progress: Float,
    targetLabel: String,
    labelStyle: TextStyle = MinhaDespensaTheme.typography.bodySmall.copy(
        color = MinhaDespensaTheme.color.onPrimaryContainer,
        fontWeight = FontWeight.Light
    ),
    colorPrimary: Color = MinhaDespensaTheme.color.primary,
    colorSecondary: Color = MinhaDespensaTheme.color.secondary,
    gaugeHeight: Dp = 30.dp,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    val traceColor = MinhaDespensaTheme.color.primary
    val labelColor = MinhaDespensaTheme.color.onPrimaryContainer
    val trailColor = if (isDark) MinhaDespensaTheme.color.onPrimaryContainer.copy(0.2f) else Color.Black.copy(0.2f)
    val isOverbudget = progress > 1.0f
    Column(modifier = modifier) {
        Canvas(
            modifier = modifier
                .graphicsLayer(clip = false)
                .height(gaugeHeight)
        ) {
            val width = size.width
            val height = size.height
            val cornerRadius = CornerRadius(height / 2, height / 2)
            val overflow = 6.dp.toPx()

            // TRAIL
            drawRoundRect(
                color = trailColor,
                size = size,
                cornerRadius = cornerRadius
            )
            val barWidth = if (isOverbudget) width else width * progress

            // BAR
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to colorPrimary,
                        1.0f to colorSecondary
                    )
                ),
                size = Size(width = barWidth, height = height),
                cornerRadius = cornerRadius
            )

            if (isOverbudget) {
                val traceX = width * (1.0f / progress)
                drawLine(
                    color = traceColor,
                    start = Offset(x = traceX, y = -overflow),
                    end = Offset(x = traceX, y = height + overflow),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val textLayoutResult = textMeasurer.measure(
                    text = targetLabel,
                    style = labelStyle
                )

                val textTopLeft = Offset(
                    x = traceX - (textLayoutResult.size.width / 2),
                    y = height + overflow + 4.dp.toPx()
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textTopLeft,
                    color = labelColor,
                )
            }
        }
        if (isOverbudget) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpiringItemTile(
    expirationLabel: String,
    productName: String,
    productCategory: String = "Category",
    anchoredTargetLabel: String = "01/01",
    anchoredGaugeProgress: Float = 0.8f,
    iconPainter: Painter? = null,
    containerColor: Color = MinhaDespensaTheme.color.primaryContainer,
    contentColor: Color = MinhaDespensaTheme.color.onPrimaryContainer,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall)
) {

    val shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner)
    val isDark = isSystemInDarkTheme()
    val primaryAlpha = if (isDark) 0.9f else 1f
    val secondaryAlpha = if (isDark) 0.5f else 0.3f

    val glassBrush = Brush.linearGradient(
        colors = listOf(
            containerColor.copy(alpha = primaryAlpha),
            containerColor.copy(alpha = secondaryAlpha)
        ),
        start = Offset(0f, .45f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            containerColor,
            Color.Transparent,
            containerColor,
            Color.Transparent,
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassBrush)
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MinhaDespensaTheme.dimens.paddingMedium,
                    end = MinhaDespensaTheme.dimens.paddingMedium
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ICON
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .height(80.dp)
                            .padding(
                                top = MinhaDespensaTheme.dimens.paddingSmall,
                                bottom = MinhaDespensaTheme.dimens.paddingSmall,
                            )
                            .clip(RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner * 0.4f))
                    )
                }
            }

            // CONTENT
            Column(
                modifier = Modifier.padding(end = MinhaDespensaTheme.dimens.paddingSmall).weight(2f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MinhaDespensaTheme.dimens.paddingSmall),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomText(
                        text = productName,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall * 0.75f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomText(
                        text = productCategory,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Light
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InverseAnchoredGauge(
                        colorPrimary = financialGaugePrimaryColor,
                        colorSecondary = financialGaugeSecondaryColor,
                        progress = anchoredGaugeProgress,
                        gaugeHeight = 10.dp,
                        targetLabel = anchoredTargetLabel,
                        labelStyle = MinhaDespensaTheme.typography.bodySmall.copy(
                            color = MinhaDespensaTheme.color.onPrimaryContainer,
                            fontWeight = FontWeight.Light,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                CustomText(
                    text = "Vence em",
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = contentColor,
                    fontWeight = FontWeight.Light
                )
                CustomText(
                    text = expirationLabel,
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConsumptionTrendItemTile(
    productName: String,
    productCategory: String,
    productMeasureUnity: String,
    consumptionAmount: String,
    iconPainter: Painter? = null,
    containerColor: Color = MinhaDespensaTheme.color.primaryContainer,
    contentColor: Color = MinhaDespensaTheme.color.onPrimaryContainer,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall)
) {

    val shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner)
    val isDark = isSystemInDarkTheme()
    val primaryAlpha = if (isDark) 0.9f else 1f
    val secondaryAlpha = if (isDark) 0.5f else 0.3f

    val glassBrush = Brush.linearGradient(
        colors = listOf(
            containerColor.copy(alpha = primaryAlpha),
            containerColor.copy(alpha = secondaryAlpha)
        ),
        start = Offset(0f, .45f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            containerColor,
            Color.Transparent,
            containerColor,
            Color.Transparent,
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassBrush)
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MinhaDespensaTheme.dimens.paddingMedium,
                    end = MinhaDespensaTheme.dimens.paddingMedium
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ICON
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .height(80.dp)
                            .padding(
                                top = MinhaDespensaTheme.dimens.paddingSmall,
                                bottom = MinhaDespensaTheme.dimens.paddingSmall,
                            )
                            .clip(RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner * 0.4f))
                    )
                }
            }

            // CONTENT
            Column(
                modifier = Modifier.padding(end = MinhaDespensaTheme.dimens.paddingSmall).weight(2f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MinhaDespensaTheme.dimens.paddingSmall),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomText(
                        text = productName,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MinhaDespensaTheme.dimens.paddingSmall * 0.75f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomText(
                        text = productCategory,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = contentColor,
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CustomText(
                    text = consumptionAmount,
                    fontStyle = MinhaDespensaTheme.typography.bodyLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                CustomText(
                    text = productMeasureUnity,
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = contentColor,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
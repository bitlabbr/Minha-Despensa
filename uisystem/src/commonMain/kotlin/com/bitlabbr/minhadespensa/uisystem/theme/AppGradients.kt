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

package com.bitlabbr.minhadespensa.uisystem.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Official Minha Despensa action gradient.
 *
 * Direction: green -> orange.
 */
fun actionGradient(
    primary: Color = primaryLightAppColor,
    secondary: Color = secondaryLightAppColor,
): Brush = Brush.linearGradient(
    colors = listOf(primary, secondary),
    start = Offset.Zero,
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

/**
 * Horizontal version used by gauges and horizontal progress indicators.
 */
fun actionHorizontalGradient(
    primary: Color = primaryLightAppColor,
    secondary: Color = secondaryLightAppColor,
): Brush = Brush.horizontalGradient(
    colors = listOf(primary, secondary),
)

/**
 * Soft version of the action gradient.
 *
 * Useful for selected rows, subtle highlights and non-primary emphasis.
 */
fun actionSoftGradient(
    primary: Color = primaryLightAppColor,
    secondary: Color = secondaryLightAppColor,
    alpha: Float = 0.16f,
): Brush = Brush.linearGradient(
    colors = listOf(
        primary.copy(alpha = alpha),
        secondary.copy(alpha = alpha),
    ),
    start = Offset.Zero,
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

/**
 * Stronger version for hero CTAs and prominent actions.
 */
fun actionStrongGradient(
    primary: Color = primaryLightAppColor,
    secondary: Color = secondaryLightAppColor,
): Brush = Brush.linearGradient(
    colors = listOf(
        primary,
        secondary,
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, 0f),
)

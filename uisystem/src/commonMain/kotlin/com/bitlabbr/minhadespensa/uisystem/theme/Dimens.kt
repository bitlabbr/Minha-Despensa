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

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp
)

val compactDimens = AppDimens(paddingSmall = 8.dp, paddingMedium = 16.dp, paddingLarge = 24.dp)
val expandedDimens = AppDimens(paddingSmall = 12.dp, paddingMedium = 24.dp, paddingLarge = 36.dp)

val LocalAppDimens = staticCompositionLocalOf { compactDimens }

fun AppDimens.withScale(scaleFactor: Float): AppDimens {
    return AppDimens(
        paddingSmall = this.paddingSmall * scaleFactor,
        paddingMedium = this.paddingMedium * scaleFactor,
        paddingLarge = this.paddingLarge * scaleFactor
    )
}
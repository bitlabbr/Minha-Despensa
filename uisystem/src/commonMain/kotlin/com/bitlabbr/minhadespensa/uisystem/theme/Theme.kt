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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = primaryDarkAppColor,
    onPrimary = onPrimaryDarkAppColor,

    secondary = secondaryDarkAppColor,
    onSecondary = onSecondaryDarkAppColor,

    primaryContainer = primaryContainerDarkAppColor,
    onPrimaryContainer = onPrimaryContainerDarkAppColor,

    secondaryContainer = secondaryContainerDarkAppColor,
    onSecondaryContainer = onSecondaryContainerDarkAppColor,

    tertiary = tertiaryDarkAppColor,
    onTertiary = onTertiaryDarkAppColor,

    background = backgroundDarkAppColor,
    onBackground = onBackgroundDarkAppColor,

    surface = surfaceDarkAppColor,
    onSurface = onSurfaceDarkAppColor
)

private val LightColorScheme = lightColorScheme(
    primary = primaryLightAppColor,
    onPrimary = onPrimaryLightAppColor,

    secondary = secondaryLightAppColor,
    onSecondary = onSecondaryLightAppColor,

    tertiary = tertiaryLightAppColor,
    onTertiary = onTertiaryLightAppColor,

    primaryContainer = primaryContainerLightAppColor,
    onPrimaryContainer = onPrimaryContainerLightAppColor,

    secondaryContainer = secondaryContainerLightAppColor,
    onSecondaryContainer = onSecondaryContainerLightAppColor,

    background = backgroundLightAppColor,
    onBackground = onBackgroundLightAppColor,

    surface = surfaceLightAppColor,
    onSurface = onSurfaceLightAppColor
)

@Composable
fun MinhaDespensaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthDp = maxWidth
        val isCompactScreen = screenWidthDp < 600.dp

        val dimensions = if (isCompactScreen) compactDimens else expandedDimens
        val typography = if (isCompactScreen) compactTypography else expandedTypography
        val densityInfo = LocalDensity.current

        val densityScaleFactor = when {
            densityInfo.density > 2.0f -> 1.10f // High Density
            densityInfo.density > 1.0f -> 1.05f // Medium Density
            else -> 0.9f                        // Low Density
        }

        val dimensionScaleFactor = if (densityInfo.density > 2.0f) 1.1f else 1.0f
        val finalTypography = typography.withScale(densityScaleFactor)
        val finalDimensions = dimensions.withScale(dimensionScaleFactor)

        CompositionLocalProvider(
            LocalAppDimens provides finalDimensions,
            LocalAppTypography provides finalTypography
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = androidx.compose.material3.Typography(
                    bodyLarge = finalTypography.bodyLarge,
                    titleLarge = finalTypography.displayLarge,
                    labelSmall = finalTypography.bodySmall,
                    labelMedium = finalTypography.bodySmall,
                    labelLarge = finalTypography.bodySmall
                ),
                content = content
            )
        }
    }
}

object MinhaDespensaTheme {
    val color: androidx.compose.material3.ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val dimens: AppDimens
        @Composable
        get() = LocalAppDimens.current

    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current
}
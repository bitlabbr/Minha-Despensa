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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MinhaDespensaTheme.color.primaryContainer,
    contentColor: Color = MinhaDespensaTheme.color.onPrimaryContainer,
    leftContent: @Composable (() -> Unit)? = null,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        title = {
            centerContent()
        },
        navigationIcon = {
            if (leftContent != null) {
                leftContent()
            }
        },
        actions = {
            if (rightContent != null) {
                rightContent()
            }
        }
    )
}

data class BottomNavItem<T : Any>(
    val title: String,
    val icon: ImageVector,
    val route: T
)

@Composable
fun CustomIconButton(
    iconPainter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(50.dp),
    backgroundColor: Color = MinhaDespensaTheme.color.onBackground,
    iconTint: Color = Color.White,
    shape: Shape = RectangleShape
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = iconPainter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(iconTint),
            modifier = Modifier.fillMaxSize()
        )
    }
}

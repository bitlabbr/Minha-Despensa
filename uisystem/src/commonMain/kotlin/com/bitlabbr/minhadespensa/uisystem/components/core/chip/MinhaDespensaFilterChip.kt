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

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun MinhaDespensaFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    shape: Shape = MinhaDespensaChipDefaults.shape(),
    colors: SelectableChipColors = MinhaDespensaChipDefaults.filterChipColors(),
) {
    val typography = MinhaDespensaTheme.typography

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            MinhaDespensaText(
                text = label,
                fontStyle = typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
        shape = shape,
        colors = colors,
        border = MinhaDespensaChipDefaults.border(selected),
    )
}
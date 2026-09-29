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

package com.bitlabbr.minhadespensa.uisystem.components.core.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun <T> MinhaDespensaDropdownField(
    options: List<T>,
    selectedOption: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    optionLabel: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    menuWidth: Dp? = null,
    itemLeadingIcon: (@Composable (T) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val colors = getAppColors()

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            anchorWidthPx = coordinates.size.width
        },
    ) {
        MinhaDespensaTextField(
            value = selectedOption?.let(optionLabel).orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            isError = isError,
            label = label?.let {
                {
                    MinhaDespensaText(
                        text = it,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = if (isError) colors.error else colors.onSecondaryContainer.copy(alpha = 0.70f),
                    )
                }
            },
            placeholder = placeholder?.let {
                {
                    MinhaDespensaText(
                        text = it,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.42f),
                    )
                }
            },
            supportingText = supportingText?.let {
                {
                    MinhaDespensaText(
                        text = it,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = if (isError) colors.error else colors.onSecondaryContainer.copy(alpha = 0.50f),
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = { if (enabled) expanded = !expanded },
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = label?.let { "Opções de $it" } ?: "Abrir opções",
                        tint = colors.onSecondaryContainer.copy(alpha = if (enabled) 0.72f else 0.38f),
                    )
                }
            },
        )

        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true },
            )
        }

        val resolvedMenuModifier = when {
            menuWidth != null -> Modifier.width(menuWidth)
            anchorWidthPx > 0 -> Modifier.width(with(density) { anchorWidthPx.toDp() })
            else -> Modifier.widthIn(min = 180.dp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MinhaDespensaDropdownDefaults.menuShape(),
            containerColor = MinhaDespensaDropdownDefaults.containerColor(),
            border = MinhaDespensaDropdownDefaults.borderStroke(),
            shadowElevation = MinhaDespensaDropdownDefaults.Elevation,
            modifier = resolvedMenuModifier.padding(vertical = 4.dp),
        ) {
            options.forEach { option ->
                val labelText = optionLabel(option)
                val isSelected = option == selectedOption

                DropdownMenuItem(
                    text = {
                        MinhaDespensaText(
                            text = labelText,
                            fontStyle = MinhaDespensaTheme.typography.bodySmall,
                            color = if (isSelected) colors.primary else colors.onSecondaryContainer,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = itemLeadingIcon?.let { { it(option) } },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MinhaDespensaDropdownDefaults.itemColors(),
                )
            }
        }
    }
}
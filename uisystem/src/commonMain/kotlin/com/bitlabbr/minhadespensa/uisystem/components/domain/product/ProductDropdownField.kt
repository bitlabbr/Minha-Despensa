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

package com.bitlabbr.minhadespensa.uisystem.components.domain.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun <T> ProductDropdownField(
    label: String,
    selectedOption: T?,
    placeholder: String,
    options: List<T>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: @Composable (T) -> String = { it.toString() },
    isRequired: Boolean = false,
    errorMessage: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens

    Box(modifier = modifier) {
        ProductTextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedOption?.let { optionLabel(it) }.orEmpty(),
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            isRequired = isRequired,
            errorMessage = errorMessage,
            trailingContent = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Abrir opções de $label",
                        tint = colors.onSecondaryContainer.copy(alpha = 0.72f),
                    )
                }
            },
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(dimens.cardCorner * 0.6f),
            containerColor = colors.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, colors.onSecondaryContainer.copy(alpha = 0.15f)),
            shadowElevation = 8.dp,
            modifier = Modifier.padding(vertical = 4.dp),
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
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MenuDefaults.itemColors(
                        textColor = colors.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.bitlabbr.minhadespensa.uisystem.components.core.input.MinhaDespensaTextField
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun ProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isRequired: Boolean = false,
    maxCharacters: Int? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    Column(modifier = modifier) {
        MinhaDespensaTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { input ->
                val sanitized = if (maxCharacters != null) input.take(maxCharacters) else input
                onValueChange(sanitized)
            },
            isError = errorMessage != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            leadingIcon = leadingContent,
            trailingIcon = trailingContent,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MinhaDespensaText(
                        text = label,
                        fontStyle = typography.bodySmall,
                        color = if (errorMessage != null) colors.error else colors.onSecondaryContainer.copy(alpha = 0.70f),
                        fontWeight = FontWeight.Normal,
                    )
                    if (isRequired) {
                        MinhaDespensaText(
                            text = " *",
                            fontStyle = typography.bodySmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            placeholder = {
                MinhaDespensaText(
                    text = placeholder,
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.42f),
                    fontWeight = FontWeight.Light,
                )
            },
            supportingText = if (errorMessage != null || maxCharacters != null) {
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (errorMessage != null) {
                            MinhaDespensaText(
                                text = errorMessage,
                                fontStyle = typography.bodySmall,
                                color = colors.error,
                                fontWeight = FontWeight.Normal,
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

                        if (maxCharacters != null) {
                            MinhaDespensaText(
                                text = "${value.length}/$maxCharacters",
                                fontStyle = typography.bodySmall,
                                color = colors.onSecondaryContainer.copy(alpha = 0.50f),
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }
                }
            } else null,
        )
    }
}
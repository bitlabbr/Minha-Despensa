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

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun CustomText(
    text: String?,
    alignment: TextAlign = TextAlign.Left,
    color: Color = MinhaDespensaTheme.color.onPrimary,
    fontStyle: TextStyle = MinhaDespensaTheme.typography.displayMedium,
    modifier: Modifier = Modifier
        .wrapContentWidth()
) {
    val customText = text ?: ""
    Text(
        textAlign = alignment,
        text = customText,
        color = color,
        modifier = modifier,
        style = fontStyle
    )
}
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

package com.bitlabbr.minhadespensa.uisystem.components.core.text

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun MinhaDespensaText(
    text: String?,
    modifier: Modifier = Modifier.wrapContentWidth(),
    alignment: TextAlign = TextAlign.Start,
    fontWeight: FontWeight? = null,
    color: Color = getAppColors().onSurface,
    fontStyle: TextStyle = MinhaDespensaTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    Text(
        text = text.orEmpty(),
        modifier = modifier,
        color = color,
        style = fontStyle,
        fontWeight = fontWeight,
        textAlign = alignment,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
    )
}

@Composable
fun MinhaDespensaText(
    text: AnnotatedString,
    modifier: Modifier = Modifier.wrapContentWidth(),
    alignment: TextAlign = TextAlign.Start,
    color: Color = getAppColors().onSurface,
    fontStyle: TextStyle = MinhaDespensaTheme.typography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = fontStyle,
        textAlign = alignment,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
    )
}
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

package com.bitlabbr.minhadespensa.uisystem.components.core.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun MinhaDespensaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val shape = MinhaDespensaButtonDefaults.primaryShape()
    val backgroundBrush = MinhaDespensaButtonDefaults.primaryActionBrush()
    val textColor = MinhaDespensaButtonDefaults.primaryTextColor()
    val dimens = MinhaDespensaTheme.dimens

    val isClickable = enabled && !isLoading
    val finalAlpha = if (isClickable) 1f else 0.45f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinhaDespensaButtonDefaults.MinHeight)
            .clip(shape)
            .background(backgroundBrush)
            .clickable(
                enabled = isClickable,
                onClick = onClick,
            )
            .padding(
                horizontal = dimens.paddingLarge,
                vertical = dimens.paddingMedium,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColor.copy(alpha = finalAlpha),
                        modifier = Modifier.size(20.dp),
                    )
                }
                MinhaDespensaText(
                    text = text,
                    color = textColor.copy(alpha = finalAlpha),
                    fontStyle = MinhaDespensaTheme.typography.button,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun MinhaDespensaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    textColor: Color = getAppColors().onSecondaryContainer
) {
    val shape = MinhaDespensaButtonDefaults.secondaryShape()
    val containerColor = MinhaDespensaButtonDefaults.secondaryContainerColor()
    val borderBrush = MinhaDespensaButtonDefaults.secondaryBorderBrush()
    val dimens = MinhaDespensaTheme.dimens

    val finalAlpha = if (enabled) 1f else 0.45f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinhaDespensaButtonDefaults.MinHeight)
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(
                horizontal = dimens.paddingMedium,
                vertical = dimens.paddingMedium,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = finalAlpha),
                    modifier = Modifier.size(20.dp),
                )
            }
            MinhaDespensaText(
                text = text,
                color = textColor.copy(alpha = finalAlpha),
                fontStyle = MinhaDespensaTheme.typography.button,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun MinhaDespensaExpandButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapsedText: String = "Mostrar mais",
    expandedText: String = "Mostrar menos",
) {
    val shape = MinhaDespensaButtonDefaults.ExpandShape
    val containerColor = MinhaDespensaButtonDefaults.expandButtonContainerColor()
    val contentColor = getAppColors().onSecondaryContainer
    val dimens = MinhaDespensaTheme.dimens

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ExpandArrowRotation",
    )

    Box(
        modifier = modifier
            .height(MinhaDespensaButtonDefaults.ExpandButtonHeight)
            .clip(shape)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.paddingMedium),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MinhaDespensaText(
                text = if (isExpanded) expandedText else collapsedText,
                color = contentColor.copy(alpha = 0.85f),
                fontStyle = MinhaDespensaTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = contentColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation),
            )
        }
    }
}

@Composable
fun MinhaDespensaClickableText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = getAppColors().primary,
    fontStyle: TextStyle = MinhaDespensaTheme.typography.bodySmall,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val interactionSource = remember { MutableInteractionSource() }

    MinhaDespensaText(
        text = text,
        modifier = modifier
            .clip(MinhaDespensaTheme.dimens.cardCorner.let { RoundedCornerShape(it * 0.3f) })
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = color),
                onClick = onClick,
            )
            .padding(
                horizontal = MinhaDespensaTheme.dimens.paddingSmall,
                vertical = 4.dp,
            ),
        color = color,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
    )
}
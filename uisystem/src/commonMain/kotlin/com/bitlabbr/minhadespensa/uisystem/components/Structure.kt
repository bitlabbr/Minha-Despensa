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

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun SimpleRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MinhaDespensaTheme.dimens.paddingSmall),
        content = content
    )
}


@Composable
fun PrimaryContainerHeader(
    textTop: String,
    textBottom: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = MinhaDespensaTheme.dimens.paddingLarge,
                start = MinhaDespensaTheme.dimens.paddingMedium,
                end = MinhaDespensaTheme.dimens.paddingMedium
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            CustomText(
                text = textTop,
                color = MinhaDespensaTheme.color.onPrimaryContainer,
                fontStyle = MinhaDespensaTheme.typography.displayLarge,
                fontWeight = FontWeight.Light
            )
            CustomText(
                text = textBottom,
                color = MinhaDespensaTheme.color.onPrimaryContainer,
                fontStyle = MinhaDespensaTheme.typography.displayLarge
            )
        }

        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Abrir opções",
                tint = MinhaDespensaTheme.color.onPrimaryContainer.copy(.6f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun SecondaryContainerHeader(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = MinhaDespensaTheme.dimens.paddingMedium,
                start = MinhaDespensaTheme.dimens.paddingSmall,
                top = MinhaDespensaTheme.dimens.paddingSmall,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomText(
            text = text,
            color = MinhaDespensaTheme.color.onSecondaryContainer,
            fontStyle = MinhaDespensaTheme.typography.displayMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun SimpleColumn(
    layouts: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier.padding(MinhaDespensaTheme.dimens.paddingMedium)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        layouts.forEach { layout ->
            layout()
        }
    }
}

@Composable
fun CustomColumn(
    alignmentStrategy: CommonConstants.ColumnAlignment = CommonConstants.ColumnAlignment.ALIGNMENT_TOP,
    layouts: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    if (alignmentStrategy == CommonConstants.ColumnAlignment.ALIGNMENT_TOP) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            layouts.forEach { layout ->
                layout()
            }
        }
    } else if (alignmentStrategy == CommonConstants.ColumnAlignment.ALIGNMENT_CENTER) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            layouts.forEach { layout ->
                layout()
            }
        }
    }
}
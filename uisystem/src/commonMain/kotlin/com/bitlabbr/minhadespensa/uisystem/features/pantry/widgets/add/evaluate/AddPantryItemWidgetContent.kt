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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.evaluate

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.CallToActionGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.*
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPantryItemWidgetContent(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCallToAction: Boolean,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    if (isCallToAction) {
        CallToActionGlassCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingSmall)
                .animateContentSize(),
        ) {
            Content(dimens, onClick, colors, typography)
        }
    } else {
        SecondaryContainerGlassCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingSmall)
                .animateContentSize(),
        ) {
            Content(dimens, onClick, colors, typography)
        }
    }

}

@Composable
private fun Content(
    dimens: AppDimens,
    onClick: () -> Unit,
    colors: ColorScheme,
    typography: AppTypography
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner))
            .clickable(onClick = onClick)
            .padding(dimens.paddingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    actionGradient(
                        primary = colors.primary,
                        secondary = colors.primary.copy(alpha = 0.1f),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(Res.string.plus_icon_desc),
                tint = colors.onSecondaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(Modifier.width(dimens.paddingMedium))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            MinhaDespensaText(
                text = stringResource(Res.string.register_product_widget_title),
                fontStyle = typography.bodyLarge,
                color = colors.onSecondaryContainer.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            MinhaDespensaText(
                text = stringResource(Res.string.register_product_widget_desc),
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                fontWeight = FontWeight.Normal,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(Res.string.chevron_right_icon_desc),
            tint = colors.onSecondaryContainer.copy(alpha = 0.70f),
            modifier = Modifier.size(24.dp)
        )
    }
}
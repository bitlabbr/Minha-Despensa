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

package com.bitlabbr.minhadespensa.uisystem.components.core.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.BaseGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.snackbar_dismiss_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun MinhaDespensaSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    type: MinhaDespensaSnackbarType = MinhaDespensaSnackbarType.SUCCESS,
    icon: ImageVector = type.defaultIcon,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    val accentColor = MinhaDespensaSnackbarDefaults.accentColor(type)

    BaseGlassCard(
        backgroundBrush = MinhaDespensaSnackbarDefaults.backgroundBrush(),
        borderBrush = MinhaDespensaSnackbarDefaults.borderBrush(accentColor),
        shape = MinhaDespensaSnackbarDefaults.shape(),
        borderWidth = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.paddingMedium,
                    vertical = dimens.paddingSmall + 2.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall + 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            MinhaDespensaText(
                text = message,
                fontStyle = typography.bodySmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )

            if (onDismiss != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.snackbar_dismiss_text),
                        tint = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
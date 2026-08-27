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

package com.bitlabbr.minhadespensa.uisystem.components.core.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinhaDespensaDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    properties: DialogProperties = MinhaDespensaDialogDefaults.properties,
    shape: Shape = MinhaDespensaDialogDefaults.shape(),
    backgroundBrush: Brush = MinhaDespensaDialogDefaults.backgroundBrush(),
    border: androidx.compose.foundation.BorderStroke = MinhaDespensaDialogDefaults.borderStroke(),
    buttons: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    val colors = getAppColors()

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        modifier = modifier.padding(horizontal = dimens.paddingLarge),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(MinhaDespensaDialogDefaults.Elevation, shape)
                .clip(shape)
                .background(backgroundBrush)
                .border(border, shape)
                .padding(dimens.paddingMedium),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingMedium),
            ) {
                if (title != null || description != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        if (title != null) {
                            MinhaDespensaText(
                                text = title,
                                fontStyle = typography.displayMedium,
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (description != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            MinhaDespensaText(
                                text = description,
                                fontStyle = typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.65f),
                            )
                        }
                    }
                }
                content()
                if (buttons != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    buttons()
                }
            }
        }
    }
}
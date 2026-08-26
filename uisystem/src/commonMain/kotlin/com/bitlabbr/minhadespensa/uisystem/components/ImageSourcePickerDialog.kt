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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourcePickerDialog(
    onDismissRequest: () -> Unit,
    onCameraSelect: () -> Unit,
    onGallerySelect: () -> Unit
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        SecondaryContainerGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomText(
                    text = "Foto do Produto",
                    fontStyle = typography.displayMedium,
                    color = colors.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )

                CustomText(
                    text = "Selecione como deseja adicionar a foto:",
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                ImageOptionItem(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Tirar Foto",
                    subtitle = "Abrir a câmera do celular",
                    onClick = {
                        onDismissRequest()
                        onCameraSelect()
                    }
                )

                ImageOptionItem(
                    icon = Icons.Rounded.PhotoLibrary,
                    title = "Escolher da Galeria",
                    subtitle = "Carregar uma imagem salva",
                    onClick = {
                        onDismissRequest()
                        onGallerySelect()
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner / 2))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            CustomText(
                text = title,
                fontStyle = typography.bodyLarge,
                color = colors.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            CustomText(
                text = subtitle,
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}
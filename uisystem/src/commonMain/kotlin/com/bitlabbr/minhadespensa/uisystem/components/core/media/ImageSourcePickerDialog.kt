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

package com.bitlabbr.minhadespensa.uisystem.components.core.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.actionSoftGradient
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.register_product_form_button_cancel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageSourcePickerDialog(
    onDismissRequest: () -> Unit,
    onCameraSelect: () -> Unit,
    onGallerySelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens

    MinhaDespensaDialog(
        onDismissRequest = onDismissRequest,
        title = "Adicionar Foto",
        description = "Escolha como deseja importar a imagem:",
        modifier = modifier,
        buttons = {
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.register_product_form_button_cancel),
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ImageSourceOptionTile(
                icon = Icons.Rounded.CameraAlt,
                title = "Tirar Foto",
                subtitle = "Abrir a câmera do celular",
                onClick = {
                    onDismissRequest()
                    onCameraSelect()
                },
            )

            ImageSourceOptionTile(
                icon = Icons.Rounded.PhotoLibrary,
                title = "Escolher da Galeria",
                subtitle = "Carregar da galeria de fotos",
                onClick = {
                    onDismissRequest()
                    onGallerySelect()
                },
            )
        }
    }
}

@Composable
private fun ImageSourceOptionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens
    val shape = RoundedCornerShape(dimens.cardCorner * 0.45f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.onSurface.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = colors.onSurface.copy(alpha = 0.08f),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimens.paddingMedium,
                vertical = dimens.paddingSmall + 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(actionSoftGradient(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            MinhaDespensaText(
                text = title,
                fontStyle = typography.bodyLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            MinhaDespensaText(
                text = subtitle,
                fontStyle = typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.60f),
                fontWeight = FontWeight.Light,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.size(22.dp),
        )
    }
}
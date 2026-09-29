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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.actionSoftGradient
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.image_picker_desc
import minhadespensa.uisystem.generated.resources.image_picker_icon_desc
import minhadespensa.uisystem.generated.resources.image_picker_picture_desc
import minhadespensa.uisystem.generated.resources.image_picker_remove_picture
import minhadespensa.uisystem.generated.resources.image_preview_desc
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImagePickerCard(
    imageBytes: ByteArray?,
    onClick: () -> Unit,
    onClearImage: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val cardShape = ImagePickerDefaults.cardShape()
    val imageShape = ImagePickerDefaults.imageShape()
    val borderStroke = ImagePickerDefaults.borderStroke()
    val containerColor = ImagePickerDefaults.containerColor()

    val imageBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(containerColor)
            .border(borderStroke, cardShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.paddingMedium),
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = stringResource(Res.string.image_picker_picture_desc),
                    modifier = Modifier
                        .size(ImagePickerDefaults.PreviewImageSize)
                        .clip(imageShape)
                        .border(borderStroke, imageShape),
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.height(dimens.paddingSmall))

                MinhaDespensaText(
                    text = stringResource(Res.string.image_preview_desc),
                    color = colors.onSecondaryContainer.copy(alpha = 0.70f),
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Light,
                    alignment = TextAlign.Center,
                )
            }

            if (onClearImage != null) {
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(dimens.paddingSmall)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.image_picker_remove_picture),
                        tint = colors.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ImagePickerDefaults.EmptyStateHeight)
                    .padding(dimens.paddingMedium),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(actionSoftGradient(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = stringResource(Res.string.image_picker_icon_desc),
                        tint = colors.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(dimens.paddingSmall))

                MinhaDespensaText(
                    text = stringResource(Res.string.image_picker_desc),
                    color = colors.onSecondaryContainer.copy(alpha = 0.72f),
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
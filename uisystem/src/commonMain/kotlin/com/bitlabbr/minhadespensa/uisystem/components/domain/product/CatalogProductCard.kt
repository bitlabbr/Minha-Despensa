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

package com.bitlabbr.minhadespensa.uisystem.components.domain.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FoodBank
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogProductUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.AppDimens
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CatalogProductCard(
    product: CatalogProductUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageBytes: ByteArray? = null,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val imageBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    val formattedWeight = if (product.netWeight % 1.0 == 0.0) {
        product.netWeight.toLong().toString()
    } else {
        product.netWeight.toString().replace('.', ',')
    }

    ItemContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner * 0.75f))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ItemImage(dimens, colors, imageBitmap, product)

            Spacer(modifier = Modifier.height(dimens.paddingSmall))

            MinhaDespensaText(
                text = product.name,
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            MinhaDespensaText(
                text = product.brand ?: product.category,
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            MinhaDespensaText(
                text = "$formattedWeight ${product.measureUnit.toAbbreviation()}",
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = colors.onSecondaryContainer.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun ItemImage(
    dimens: AppDimens,
    colors: ColorScheme,
    imageBitmap: ImageBitmap?,
    product: CatalogProductUiModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(dimens.cardCorner * 0.4f))
            .background(colors.onSurface.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Fastfood,
                contentDescription = null,
                tint = colors.onSecondaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(50.dp),
            )
        }
    }
}
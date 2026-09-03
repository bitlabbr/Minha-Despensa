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

package com.bitlabbr.minhadespensa.uisystem.components.domain.pantry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PantryItemCard(
    product: PantryItemUiModel,
    imageBytes: ByteArray? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val imageBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    val expirationInfo = remember(product.expirationDate, product.isExpired) {
        resolveExpirationStatus(product.expirationDate, product.isExpired)
    }

    val formattedQuantity = remember(product.quantity) {
        if (product.quantity % 1.0 == 0.0) {
            product.quantity.toLong().toString()
        } else {
            product.quantity.toString().replace('.', ',')
        }
    }

    SecondaryContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Imagem do Produto + Tag Flutuante de Validade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(dimens.cardCorner * 0.4f))
                    .background(colors.onSurface.copy(alpha = 0.06f)),
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
                        imageVector = Icons.Rounded.Kitchen,
                        contentDescription = null,
                        tint = colors.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp),
                    )
                }

                // Tag de Validade
                if (expirationInfo != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(expirationInfo.backgroundColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (expirationInfo.showWarningIcon) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = expirationInfo.textColor,
                                    modifier = Modifier.size(10.dp),
                                )
                            }
                            MinhaDespensaText(
                                text = expirationInfo.label,
                                fontStyle = typography.bodySmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = expirationInfo.textColor,
                            )
                        }
                    }
                }
            }

            // Nome do Produto
            MinhaDespensaText(
                text = product.name,
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Categoria
            MinhaDespensaText(
                text = product.category,
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Estoque / Quantidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MinhaDespensaText(
                    text = "$formattedQuantity ${product.measureUnit.toAbbreviation()}",
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )

                if (product.quantity <= 0.0) {
                    MinhaDespensaText(
                        text = "Esgotado",
                        fontStyle = typography.bodySmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.error,
                    )
                }
            }
        }
    }
}

private data class ExpirationVisualState(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color,
    val showWarningIcon: Boolean = false,
)

private fun resolveExpirationStatus(expirationDate: Long?, isExpired: Boolean): ExpirationVisualState? {
    if (expirationDate == null) return null

    val now = Clock.System.now().toEpochMilliseconds()
    val diffMillis = expirationDate - now
    val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

    return when {
        isExpired || diffDays < 0 -> ExpirationVisualState(
            label = "Vencido",
            textColor = Color.White,
            backgroundColor = Color(0xFFDC2626),
            showWarningIcon = true,
        )

        diffDays == 0 -> ExpirationVisualState(
            label = "Hoje",
            textColor = Color.White,
            backgroundColor = Color(0xFFEA580C),
            showWarningIcon = true,
        )

        diffDays <= 3 -> ExpirationVisualState(
            label = "${diffDays}d",
            textColor = Color(0xFF78350F),
            backgroundColor = Color(0xFFFDE68A),
            showWarningIcon = true,
        )

        else -> {
            val date = Instant.fromEpochMilliseconds(expirationDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            val day = date.dayOfMonth.toString().padStart(2, '0')
            val month = date.monthNumber.toString().padStart(2, '0')
            ExpirationVisualState(
                label = "$day/$month",
                textColor = Color(0xFF047857),
                backgroundColor = Color(0xFFD1FAE5),
                showWarningIcon = false,
            )
        }
    }
}
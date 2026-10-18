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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice

@Composable
fun InvoiceItemRow(
    item: InvoiceItem,
    onToggleSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    ItemContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
            .clickable { onToggleSelect(!item.isSelected) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                IconButton(onClick = { onToggleSelect(!item.isSelected) }) {
                    Icon(
                        imageVector = if (item.isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (item.isSelected) colors.primary else colors.onSecondaryContainer.copy(alpha = 0.4f),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MinhaDespensaText(
                        text = item.name,
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isSelected) colors.onSecondaryContainer else colors.onSecondaryContainer.copy(alpha = 0.5f),
                    )

                    val quantityFormatted = if (item.quantity % 1.0 == 0.0) {
                        "${item.quantity.toInt()} ${item.measureUnit.toAbbreviation()}"
                    } else {
                        "${item.quantity} ${item.measureUnit.toAbbreviation()}"
                    }

                    val eanInfo = item.ean?.let { " • EAN: $it" } ?: ""

                    MinhaDespensaText(
                        text = "$quantityFormatted$eanInfo",
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(end = dimens.paddingSmall),
            ) {
                item.computedTotalPriceInCents?.let { totalCents ->
                    MinhaDespensaText(
                        text = totalCents.formatPrice(includeCurrencySymbol = true),
                        fontStyle = typography.priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isSelected) colors.primary else colors.onSecondaryContainer.copy(alpha = 0.5f),
                    )
                }

                item.unitPriceInCents?.let { unitCents ->
                    MinhaDespensaText(
                        text = "${unitCents.formatPrice(includeCurrencySymbol = true)} / un",
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

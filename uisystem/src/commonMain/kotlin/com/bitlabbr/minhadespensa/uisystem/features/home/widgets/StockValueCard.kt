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

package com.bitlabbr.minhadespensa.uisystem.features.home.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomePantryStockUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.home_stock_value_average_unit_price
import minhadespensa.uisystem.generated.resources.home_stock_value_items_count
import minhadespensa.uisystem.generated.resources.home_stock_value_no_prices
import minhadespensa.uisystem.generated.resources.home_stock_value_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun StockValueCard(
    stockValue: HomePantryStockUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceContainer.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = colors.outline.copy(alpha = 0.2f),
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Inventory2,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    MinhaDespensaText(
                        text = stringResource(Res.string.home_stock_value_title),
                        fontStyle = typography.bodyLarge,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            MinhaDespensaText(
                text = stockValue.formattedTotalValue,
                fontStyle = typography.displayLarge,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
            )

            if (stockValue.hasPricedItems) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.home_stock_value_items_count, stockValue.itemsWithPriceCount),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f),
                    )

                    stockValue.formattedAverageUnitPrice?.let { avgPrice ->
                        MinhaDespensaText(
                            text = "•",
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.4f),
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.home_stock_value_average_unit_price, avgPrice),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            } else {
                MinhaDespensaText(
                    text = stringResource(Res.string.home_stock_value_no_prices),
                    fontStyle = typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

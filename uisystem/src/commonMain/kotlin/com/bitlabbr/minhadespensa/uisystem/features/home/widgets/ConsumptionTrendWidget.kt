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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.*
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaExpandButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.domain.pantry.ConsumptionTrendItemTile
import com.bitlabbr.minhadespensa.uisystem.mapper.getIconPainterFromString
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme.dimens
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

@Composable
fun ConsumptionTrendWidget(data: HomeWidget.ConsumptionTrend) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxInitialItems = 4
    val content = data.items
    val hasMoreItems = content.size > maxInitialItems
    val visibleItems = if (isExpanded || !hasMoreItems) content else content.take(maxInitialItems)

    SecondaryContainerGlassCard(
        modifier = Modifier
            .animateContentSize()
            .padding(
                horizontal = MinhaDespensaTheme.dimens.paddingSmall,
                vertical = MinhaDespensaTheme.dimens.paddingSmall
            ),
        content = {
            SecondaryContainerHeader(
                text = "ITENS MAIS CONSUMIDOS NO MÊS"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall)
            ) {
                visibleItems.forEach { item ->
                    ConsumptionTrendItemTile(
                        productName = item.productName,
                        productCategory = item.productCategory,
                        iconPainter = getIconPainterFromString(item.iconPainterURI),
                        productMeasureUnity = item.productMeasureUnity,
                        consumptionAmount = item.consumptionAmount,
                    )
                }

                if (hasMoreItems) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MinhaDespensaExpandButton(
                            isExpanded = isExpanded,
                            collapsedText = "Mostrar mais (${content.size - maxInitialItems})",
                            expandedText = "Mostrar menos",
                            onClick = { isExpanded = !isExpanded }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimens.paddingSmall))
        }
    )
}

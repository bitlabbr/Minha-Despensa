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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaExpandButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.pantry.ConsumptionTrendItemTile
import com.bitlabbr.minhadespensa.uisystem.features.home.model.ConsumptionTrendItemUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.getIconPainterFromString
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.action_show_less
import minhadespensa.uisystem.generated.resources.action_show_more_count
import minhadespensa.uisystem.generated.resources.home_consumption_empty_desc
import minhadespensa.uisystem.generated.resources.home_consumption_empty_title
import minhadespensa.uisystem.generated.resources.home_consumption_section_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConsumptionTrendWidget(
    items: List<ConsumptionTrendItemUiModel>,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxInitialItems = 4
    val hasMoreItems = items.size > maxInitialItems
    val visibleItems = if (isExpanded || !hasMoreItems) items else items.take(maxInitialItems)
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    SecondaryContainerGlassCard(
        modifier = modifier
            .animateContentSize()
            .padding(horizontal = dimens.paddingSmall),
        content = {
            SecondaryContainerHeader(
                text = stringResource(Res.string.home_consumption_section_title)
            )

            if (items.isEmpty()) {
                // Empty state encorajador
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.paddingMedium, vertical = dimens.paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.home_consumption_empty_title),
                        fontStyle = typography.bodyLarge,
                        color = colors.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        alignment = TextAlign.Center,
                    )
                    MinhaDespensaText(
                        text = stringResource(Res.string.home_consumption_empty_desc),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                        alignment = TextAlign.Center,
                    )
                }
            } else {
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
                                collapsedText = stringResource(
                                    Res.string.action_show_more_count,
                                    items.size - maxInitialItems
                                ),
                                expandedText = stringResource(Res.string.action_show_less),
                                onClick = { isExpanded = !isExpanded }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimens.paddingSmall))
        }
    )
}

/**
 * Sobrecarga de compatibilidade retroativa para HomeWidget.ConsumptionTrend
 */
@Composable
fun ConsumptionTrendWidget(
    data: HomeWidget.ConsumptionTrend,
    modifier: Modifier = Modifier,
) {
    ConsumptionTrendWidget(
        items = data.items,
        modifier = modifier,
    )
}

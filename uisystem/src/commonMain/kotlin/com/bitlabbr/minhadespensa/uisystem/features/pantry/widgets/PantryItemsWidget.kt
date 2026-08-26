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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.getIconPainterFromString
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.defaultButtonColor
import com.bitlabbr.minhadespensa.uisystem.theme.expiringItemContainerColor
import com.bitlabbr.minhadespensa.uisystem.theme.expiringItemContentColor
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun PantryItemsWidget(data: PantryWidget.PantryCategories) {
    val colors = MinhaDespensaTheme.color
    val dimens = MinhaDespensaTheme.dimens

    val maxInitialItems = 4
    val categories: Map<String, List<CatalogProduct>> = data.categorizedProductsMap
    val hasMoreItems = categories.size > maxInitialItems
    var isExpanded by remember { mutableStateOf(false) }
    val toggleExpanded = remember { { isExpanded = !isExpanded } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf("Todos")) }

    val groupedFilteredCategories: Map<String, List<CatalogProduct>> = remember(searchQuery, selectedCategories) {
        categories.filter { item ->
            val matchesSearch = item.key.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategories.contains("Todos") || selectedCategories.contains(item.key)
            matchesSearch && matchesCategory
        }
    }

    SecondaryContainerGlassCard(
        modifier = Modifier
            .animateContentSize()
            .padding(
                horizontal = MinhaDespensaTheme.dimens.paddingSmall,
                vertical = MinhaDespensaTheme.dimens.paddingSmall
            ),
        content = {
            SecondaryContainerHeader(
                text = "Produtos"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.paddingSmall)
            ) {
                items(PantryMockData.categories) { category ->
                    val isSelected = selectedCategories.contains(category)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (category == "Todos") {
                                setOf("Todos")
                            } else {
                                val newSelection = selectedCategories.toMutableSet()
                                newSelection.remove("Todos")
                                if (isSelected) newSelection.remove(category) else newSelection.add(category)
                                if (newSelection.isEmpty()) setOf("Todos") else newSelection
                            }
                        },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.paddingSmall))

            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                groupedFilteredCategories.forEach { (categoryName, itemsInCategory) ->
                    Column(
                        modifier = Modifier.padding(dimens.paddingSmall)
                    ) {
                        SecondaryContainerHeader(categoryName.uppercase())
                        val visibleItems =
                            if (isExpanded || !hasMoreItems) itemsInCategory else itemsInCategory.take(maxInitialItems)
                        visibleItems.forEach { item ->
                            PantryItemTile(
                                productName = item.name,
                                containerColor = expiringItemContainerColor,
                                contentColor = expiringItemContentColor,
                                iconPainter = getIconPainterFromString(item.thumbnailUrl),
                                itemCount = "20",
                            )
                        }
                        if (hasMoreItems) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = toggleExpanded,
                                    colors = ButtonDefaults.buttonColors(containerColor = defaultButtonColor)
                                ) {
                                    CustomText(
                                        text = if (isExpanded) "Ver Menos" else "Ver Tudo",
                                        color = colors.onSecondaryContainer,
                                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                                        alignment = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
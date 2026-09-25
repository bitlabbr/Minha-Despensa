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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogProductPickerSheet(
    products: List<CatalogProduct>,
    onProductSelected: (CatalogProduct) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            val query = searchQuery.trim().lowercase()
            products.filter { product ->
                product.name.lowercase().contains(query) ||
                    (product.brand?.lowercase()?.contains(query) == true) ||
                    product.category.lowercase().contains(query)
            }
        }
    }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MinhaDespensaText(
                text = stringResource(Res.string.shopping_assistant_replace_picker_title),
                fontStyle = typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer,
            )

            ProductTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = stringResource(Res.string.shopping_assistant_replace_picker_title),
                placeholder = stringResource(Res.string.shopping_assistant_replace_picker_search_placeholder),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = colors.onSecondaryContainer.copy(alpha = 0.6f),
                    )
                },
            )

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.shopping_assistant_replace_picker_empty),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ItemContainerGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                                .clickable { onProductSelected(product) },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimens.paddingSmall),
                            ) {
                                MinhaDespensaText(
                                    text = product.name,
                                    fontStyle = typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSecondaryContainer,
                                )
                                val brandText = product.brand ?: stringResource(Res.string.planned_list_no_brand)
                                MinhaDespensaText(
                                    text = "$brandText • ${product.category}",
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Cancelar",
                    style = typography.button,
                )
            }
        }
    }
}

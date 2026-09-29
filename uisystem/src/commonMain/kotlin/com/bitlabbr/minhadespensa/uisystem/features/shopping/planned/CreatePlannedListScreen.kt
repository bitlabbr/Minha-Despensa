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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.planned

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePlannedListScreen(
    onNavigateBack: () -> Unit,
    onListSaved: (listId: String) -> Unit,
    viewModel: PlannedListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                leftContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.register_product_form_back_button_desc),
                            tint = colors.onPrimaryContainer,
                        )
                    }
                },
                centerContent = {
                    MinhaDespensaText(
                        text = stringResource(Res.string.planned_list_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer,
                    )
                },
            )
        },
        bottomBar = {
            SecondaryContainerGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = dimens.paddingSmall, vertical = dimens.paddingSmall),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    uiState.errorMessage?.let { error ->
                        MinhaDespensaText(
                            text = error,
                            color = colors.error,
                            fontStyle = typography.bodySmall,
                        )
                    }

                    val count = uiState.selectedQuantities.size
                    val saveButtonText = if (count > 0) {
                        stringResource(Res.string.planned_list_save_button, count)
                    } else {
                        stringResource(Res.string.planned_list_select_products)
                    }

                    MinhaDespensaPrimaryButton(
                        text = saveButtonText,
                        onClick = { viewModel.savePlannedList(onListSaved) },
                        enabled = uiState.canSave,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            contentPadding = PaddingValues(vertical = dimens.paddingSmall),
        ) {
            item {
                ProductTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = stringResource(Res.string.planned_list_name_label),
                    placeholder = stringResource(Res.string.planned_list_name_placeholder),
                    isRequired = true,
                )
            }

            item {
                ProductTextField(
                    value = uiState.budgetInput,
                    onValueChange = viewModel::onBudgetChange,
                    label = stringResource(Res.string.planned_list_budget_label),
                    placeholder = stringResource(Res.string.planned_list_budget_placeholder),
                    keyboardType = KeyboardType.Decimal,
                )
            }

            item {
                ProductTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = stringResource(Res.string.planned_list_search_catalog),
                    placeholder = stringResource(Res.string.catalog_searchbar_widget_placeholder),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer.copy(alpha = 0.6f),
                        )
                    },
                )
            }

            item {
                MinhaDespensaText(
                    text = stringResource(Res.string.planned_list_catalog_products),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (uiState.availableProducts.isEmpty()) {
                item {
                    MinhaDespensaText(
                        text = stringResource(Res.string.planned_list_empty_products),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                items(uiState.availableProducts, key = { it.id }) { product ->
                    val selectedQty = uiState.selectedQuantities[product.id] ?: 0.0

                    ItemContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.cardCorner * 0.6f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (selectedQty > 0.0) {
                                    IconButton(onClick = { viewModel.onDecreaseQuantity(product.id) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Remove,
                                            contentDescription = "Diminuir",
                                            tint = colors.onSecondaryContainer,
                                        )
                                    }
                                    MinhaDespensaText(
                                        text = if (selectedQty % 1.0 == 0.0) selectedQty.toLong().toString() else selectedQty.toString(),
                                        fontStyle = typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                    )
                                }

                                IconButton(onClick = { viewModel.onIncreaseQuantity(product.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Adicionar",
                                        tint = colors.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
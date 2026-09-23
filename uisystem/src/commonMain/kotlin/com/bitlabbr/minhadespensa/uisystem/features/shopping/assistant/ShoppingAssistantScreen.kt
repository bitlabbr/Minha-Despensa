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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductBottomSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantSubFlow
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.AddCartItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingAssistantScreen(
    listId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ShoppingAssistantViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    LaunchedEffect(listId) {
        viewModel.startSession(listId)
    }

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
                        text = uiState.listTitle.ifBlank { stringResource(Res.string.shopping_assistant_title) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.shopping_assistant_cart_count,
                                uiState.checkedCount,
                                uiState.totalCount,
                            ),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer,
                        )
                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.shopping_assistant_total,
                                uiState.totalCartValueInCents.formatPrice(includeCurrencySymbol = true),
                            ),
                            fontStyle = typography.priceLabel,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    ) {
                        MinhaDespensaSecondaryButton(
                            text = stringResource(Res.string.shopping_assistant_scan_barcode),
                            onClick = viewModel::onScanBarcodeClicked,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Rounded.DocumentScanner,
                        )

                        MinhaDespensaPrimaryButton(
                            text = stringResource(Res.string.shopping_assistant_finalize_stock),
                            onClick = { viewModel.onFinalizePurchase(onNavigateBack) },
                            enabled = uiState.checkedCount > 0 && !uiState.isFinalizing,
                            isLoading = uiState.isFinalizing,
                            modifier = Modifier.weight(1.3f),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(dimens.paddingMedium),
                contentAlignment = Alignment.Center,
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.shopping_assistant_empty_cart),
                    fontStyle = typography.bodyLarge,
                    color = colors.onBackground.copy(alpha = 0.7f),
                    alignment = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                contentPadding = PaddingValues(vertical = dimens.paddingSmall),
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    ItemContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                            .clickable { viewModel.onEditItemClicked(item) },
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
                                IconButton(
                                    onClick = { viewModel.onToggleItemChecked(item.id, !item.isChecked) },
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (item.isChecked) colors.primary else colors.onSecondaryContainer.copy(alpha = 0.5f),
                                    )
                                }

                                Column {
                                    MinhaDespensaText(
                                        text = item.displayName,
                                        fontStyle = typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onSecondaryContainer,
                                    )
                                    val priceText = item.priceAtTime?.let { it.formatPrice(includeCurrencySymbol = true) }
                                        ?: stringResource(Res.string.shopping_assistant_no_price)
                                    val qtyText = if (item.quantity % 1.0 == 0.0) item.quantity.toLong().toString() else item.quantity.toString()
                                    MinhaDespensaText(
                                        text = stringResource(Res.string.shopping_assistant_item_summary, qtyText, priceText),
                                        fontStyle = typography.bodySmall,
                                        color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                                    )
                                }
                            }

                            if (item.subtotalInCents > 0.0) {
                                MinhaDespensaText(
                                    text = item.subtotalInCents.toLong().formatPrice(includeCurrencySymbol = true),
                                    fontStyle = typography.priceLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // SUBFLUXOS MODULARES
    when (val subFlow = uiState.activeSubFlow) {
        is ShoppingAssistantSubFlow.BarcodeScanner -> {
            BarcodeScannerModal(
                onBarcodeScanned = viewModel::onBarcodeScanned,
                onDismissRequest = viewModel::onCloseSubFlow,
            )
        }

        is ShoppingAssistantSubFlow.CreateProduct -> {
            RegisterProductBottomSheet(
                isOpen = true,
                prefilledEan = subFlow.initialEan,
                onProductCreated = viewModel::onProductCreatedFromCatalog,
                onDismiss = viewModel::onCloseSubFlow,
            )
        }

        is ShoppingAssistantSubFlow.AddItemDetails -> {
            AddCartItemDetailsSheet(
                product = subFlow.product,
                rawText = subFlow.rawText,
                initialQuantity = subFlow.initialQuantity,
                initialPrice = subFlow.initialPriceInCents,
                onConfirm = { qty, price ->
                    viewModel.onConfirmItemDetails(
                        product = subFlow.product,
                        rawText = subFlow.rawText,
                        quantity = qty,
                        priceInCents = price,
                        existingItemId = subFlow.existingItemId,
                    )
                },
                onDismiss = viewModel::onCloseSubFlow,
            )
        }

        null -> Unit
    }
}
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.navigation.BackHandler
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductBottomSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.CartItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantSubFlow
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.AddCartItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.CatalogProductPickerSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.ReplaceItemOptionsSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.ShoppingFinancialDashboard
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

    var showExitDialog by remember { mutableStateOf(false) }
    var showFinalizeConfirmationDialog by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var itemPendingDelete by remember { mutableStateOf<CartItemUiModel?>(null) }
    var editingTitleText by remember { mutableStateOf("") }
    var editingBudgetText by remember { mutableStateOf("") }

    val handleBackPress: () -> Unit = {
        if (uiState.activeSubFlow != null) {
            viewModel.onCloseSubFlow()
        } else if (uiState.isCompleted) {
            onNavigateBack()
        } else if (uiState.hasChanges) {
            showExitDialog = true
        } else if (uiState.isDirectShopping) {
            viewModel.discardSession(onNavigateBack)
        } else {
            onNavigateBack()
        }
    }

    val backHandlerEnabled = uiState.activeSubFlow != null || (!uiState.isCompleted && (uiState.hasChanges || uiState.isDirectShopping))
    BackHandler(enabled = backHandlerEnabled) {
        handleBackPress()
    }

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
                    IconButton(onClick = handleBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.register_product_form_back_button_desc),
                            tint = colors.onPrimaryContainer,
                        )
                    }
                },
                centerContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !uiState.isCompleted) {
                                editingTitleText = uiState.listTitle
                                editingBudgetText = uiState.budgetInCents?.let { (it / 100.0).toString().replace('.', ',') } ?: ""
                                showEditTitleDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        MinhaDespensaText(
                            text = uiState.listTitle.ifBlank { stringResource(Res.string.shopping_assistant_title) },
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer,
                        )
                        if (!uiState.isCompleted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(Res.string.shopping_assistant_edit_title),
                                tint = colors.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
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
                            color = if (uiState.isOverBudget) colors.error else colors.primary,
                        )
                    }

                    if (!uiState.isCompleted) {
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
                                onClick = { showFinalizeConfirmationDialog = true },
                                enabled = uiState.checkedCount > 0 && !uiState.isFinalizing,
                                isLoading = uiState.isFinalizing,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ShoppingFinancialDashboard(
                cartValueInCents = uiState.totalCartValueInCents,
                budgetInCents = uiState.budgetInCents,
                remainingBudgetInCents = uiState.remainingBudgetInCents,
                budgetProgress = uiState.budgetProgress,
                isOverBudget = uiState.isOverBudget,
                checkedCount = uiState.checkedCount,
                totalCount = uiState.totalCount,
                isCompleted = uiState.isCompleted,
                onEditBudget = {
                    editingBudgetText = uiState.budgetInCents?.let { (it / 100.0).toString().replace('.', ',') } ?: ""
                    showEditBudgetDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.paddingSmall, vertical = 6.dp),
            )

            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                        .padding(horizontal = dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                    ItemContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                            .clickable(enabled = !uiState.isCompleted) { viewModel.onEditItemClicked(item) },
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
                                    enabled = !uiState.isCompleted,
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (item.subtotalInCents > 0.0) {
                                    MinhaDespensaText(
                                        text = item.subtotalInCents.toLong().formatPrice(includeCurrencySymbol = true),
                                        fontStyle = typography.priceLabel,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                    )
                                }

                                if (!uiState.isCompleted) {
                                    IconButton(
                                        onClick = { viewModel.onReplaceItemClicked(item) },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapHoriz,
                                            contentDescription = stringResource(Res.string.shopping_assistant_replace_item_action),
                                            tint = colors.onSecondaryContainer.copy(alpha = 0.7f),
                                        )
                                    }

                                    IconButton(
                                        onClick = { itemPendingDelete = item },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(Res.string.shopping_assistant_remove_item_action),
                                            tint = colors.error.copy(alpha = 0.7f),
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
            key(subFlow.existingItemId ?: subFlow.product?.id ?: subFlow.rawText ?: "cart_details") {
                AddCartItemDetailsSheet(
                    product = subFlow.product,
                    rawText = subFlow.rawText,
                    initialQuantity = subFlow.initialQuantity,
                    initialPrice = subFlow.initialPriceInCents,
                    isReplacement = subFlow.isReplacement,
                    onReplaceItem = if (!subFlow.isReplacement && subFlow.existingItemId != null) {
                        val item = uiState.items.find { it.id == subFlow.existingItemId }
                        if (item != null) { { viewModel.onReplaceItemClicked(item) } } else null
                    } else null,
                    onRemoveItem = if (!subFlow.isReplacement && subFlow.existingItemId != null) {
                        val item = uiState.items.find { it.id == subFlow.existingItemId }
                        if (item != null) {
                            {
                                viewModel.onCloseSubFlow()
                                itemPendingDelete = item
                            }
                        } else null
                    } else null,
                    onConfirm = { qty, price ->
                        viewModel.onConfirmItemDetails(
                            product = subFlow.product,
                            rawText = subFlow.rawText,
                            quantity = qty,
                            priceInCents = price,
                            existingItemId = subFlow.existingItemId,
                            isReplacement = subFlow.isReplacement,
                        )
                    },
                    onDismiss = viewModel::onCloseSubFlow,
                )
            }
        }

        is ShoppingAssistantSubFlow.ReplaceItemOptions -> {
            ReplaceItemOptionsSheet(
                item = subFlow.item,
                onScanBarcode = { viewModel.onStartReplaceBarcodeScan(subFlow.item) },
                onSearchCatalog = { viewModel.onStartReplaceCatalogSearch(subFlow.item) },
                onDismiss = viewModel::onCloseSubFlow,
            )
        }

        is ShoppingAssistantSubFlow.ReplaceItemBarcodeScanner -> {
            BarcodeScannerModal(
                onBarcodeScanned = { ean -> viewModel.onBarcodeScannedForReplacement(subFlow.item, ean) },
                onDismissRequest = viewModel::onCloseSubFlow,
            )
        }

        is ShoppingAssistantSubFlow.SearchCatalogForReplacement -> {
            CatalogProductPickerSheet(
                products = uiState.availableProducts,
                onProductSelected = { product -> viewModel.onReplacementProductSelected(subFlow.item, product) },
                onDismiss = viewModel::onCloseSubFlow,
            )
        }

        null -> Unit
    }

    if (showExitDialog) {
        MinhaDespensaDialog(
            onDismissRequest = { showExitDialog = false },
            title = stringResource(Res.string.shopping_assistant_exit_dialog_title),
            description = stringResource(Res.string.shopping_assistant_exit_dialog_desc),
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_assistant_exit_dialog_save),
                        onClick = {
                            showExitDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.Save,
                    )

                    MinhaDespensaSecondaryButton(
                        text = stringResource(Res.string.shopping_assistant_exit_dialog_discard),
                        onClick = {
                            showExitDialog = false
                            if (uiState.isDirectShopping) {
                                viewModel.discardSession(onNavigateBack)
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.DeleteOutline,
                    )

                    TextButton(
                        onClick = { showExitDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_exit_dialog_cancel),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        ) {
        }
    }

    if (showFinalizeConfirmationDialog) {
        MinhaDespensaDialog(
            onDismissRequest = { showFinalizeConfirmationDialog = false },
            title = stringResource(Res.string.shopping_assistant_finalize_dialog_title),
            description = stringResource(Res.string.shopping_assistant_finalize_dialog_desc),
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_assistant_finalize_dialog_confirm),
                        onClick = {
                            showFinalizeConfirmationDialog = false
                            viewModel.onFinalizePurchase(onNavigateBack)
                        },
                        isLoading = uiState.isFinalizing,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.CheckCircle,
                    )

                    TextButton(
                        onClick = { showFinalizeConfirmationDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_finalize_dialog_cancel),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        ) {
        }
    }

    if (showEditTitleDialog) {
        MinhaDespensaDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = stringResource(Res.string.shopping_assistant_edit_title_dialog_title),
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_assistant_edit_title_save),
                        onClick = {
                            val trimmed = editingTitleText.trim()
                            val parsedBudget = editingBudgetText
                                .replace(',', '.')
                                .toDoubleOrNull()
                                ?.let { (it * 100).roundToLong() }
                            if (trimmed.isNotBlank()) {
                                viewModel.onUpdateListDetails(trimmed, parsedBudget)
                            }
                            showEditTitleDialog = false
                        },
                        enabled = editingTitleText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.Save,
                    )

                    TextButton(
                        onClick = { showEditTitleDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_exit_dialog_cancel),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        ) {
            ProductTextField(
                value = editingTitleText,
                onValueChange = { editingTitleText = it },
                label = stringResource(Res.string.shopping_assistant_edit_title_label),
                placeholder = stringResource(Res.string.shopping_assistant_edit_title_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )

            ProductTextField(
                value = editingBudgetText,
                onValueChange = { newBudget ->
                    editingBudgetText = newBudget.filter { it.isDigit() || it == ',' || it == '.' }.take(10)
                },
                label = stringResource(Res.string.shopping_assistant_edit_budget_label),
                placeholder = stringResource(Res.string.shopping_assistant_edit_budget_placeholder),
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showEditBudgetDialog) {
        MinhaDespensaDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = stringResource(Res.string.shopping_assistant_edit_budget_dialog_title),
            description = stringResource(Res.string.shopping_assistant_edit_budget_dialog_desc),
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_assistant_edit_title_save),
                        onClick = {
                            val parsedBudget = editingBudgetText
                                .replace(',', '.')
                                .toDoubleOrNull()
                                ?.let { (it * 100).roundToLong() }
                            viewModel.onUpdateListDetails(uiState.listTitle, parsedBudget)
                            showEditBudgetDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.Save,
                    )

                    if (uiState.budgetInCents != null) {
                        TextButton(
                            onClick = {
                                viewModel.onUpdateListDetails(uiState.listTitle, null)
                                showEditBudgetDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MinhaDespensaText(
                                text = stringResource(Res.string.shopping_assistant_edit_budget_clear),
                                fontStyle = typography.bodySmall,
                                color = colors.error,
                            )
                        }
                    }

                    TextButton(
                        onClick = { showEditBudgetDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_exit_dialog_cancel),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        ) {
            ProductTextField(
                value = editingBudgetText,
                onValueChange = { newBudget ->
                    editingBudgetText = newBudget.filter { it.isDigit() || it == ',' || it == '.' }.take(10)
                },
                label = stringResource(Res.string.shopping_assistant_edit_budget_label),
                placeholder = stringResource(Res.string.shopping_assistant_edit_budget_placeholder),
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    itemPendingDelete?.let { item ->
        MinhaDespensaDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = stringResource(Res.string.shopping_assistant_remove_item_dialog_title),
            description = stringResource(
                Res.string.shopping_assistant_remove_item_dialog_desc,
                item.displayName,
            ),
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_assistant_remove_item_dialog_confirm),
                        onClick = {
                            viewModel.onRemoveItem(item.id)
                            itemPendingDelete = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.DeleteOutline,
                    )

                    TextButton(
                        onClick = { itemPendingDelete = null },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_exit_dialog_cancel),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        ) {
        }
    }
}
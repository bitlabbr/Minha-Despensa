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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductBottomSheet
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantSubFlow
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets.AddCartItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingAssistantScreen(
    listId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ShoppingAssistantViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(listId) {
        viewModel.startSession(listId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.listTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Carrinho (${uiState.checkedCount}/${uiState.totalCount})",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Total: R$:${uiState.totalCartValueInCents}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = viewModel::onScanBarcodeClicked,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.DocumentScanner, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Escanear")
                        }

                        Button(
                            onClick = { viewModel.onFinalizePurchase(onNavigateBack) },
                            enabled = uiState.checkedCount > 0 && !uiState.isFinalizing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                            modifier = Modifier.weight(1.3f),
                        ) {
                            if (uiState.isFinalizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                )
                            } else {
                                Text("Finalizar e Estocar")
                            }
                        }
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
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nenhum item no carrinho.\nBipe o código de barras do primeiro produto!",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEditItemClicked(item) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isChecked) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                IconButton(
                                    onClick = { viewModel.onToggleItemChecked(item.id, !item.isChecked) },
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    )
                                }

                                Column {
                                    Text(
                                        text = item.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "${item.quantity}x • ${item.priceAtTime?.let { "R$ " + it.formatPrice() } ?: "Sem preço"}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }

                            if (item.subtotalInCents > 0.0) {
                                Text(
                                    text = "Total: R$ ${uiState.totalCartValueInCents.formatPrice()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
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
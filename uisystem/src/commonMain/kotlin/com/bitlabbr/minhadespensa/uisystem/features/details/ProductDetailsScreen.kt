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

package com.bitlabbr.minhadespensa.uisystem.features.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerSection
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.details.model.*
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.AddPantryItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: ProductDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {
            MinhaDespensaTopBar(
                leftContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = colors.onPrimaryContainer,
                        )
                    }
                },
                centerContent = {
                    MinhaDespensaText(
                        text = uiState.product?.name ?: stringResource(Res.string.product_details_title),
                        fontWeight = FontWeight.Bold,
                        fontStyle = typography.displayMedium,
                        color = colors.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        },
        bottomBar = {
            if (uiState.product != null) {
                Surface(
                    color = colors.surface.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MinhaDespensaSecondaryButton(
                            text = stringResource(Res.string.product_details_action_add_to_pantry),
                            onClick = viewModel::onOpenAddStockSheet,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Rounded.Kitchen,
                        )
                        MinhaDespensaPrimaryButton(
                            text = stringResource(Res.string.product_details_action_add_to_list),
                            onClick = viewModel::onOpenAddToListDialog,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Rounded.AddShoppingCart,
                        )
                    }
                }
            }
        },
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (uiState.product != null) {
            val product = uiState.product!!

            // --- HERO SECTION: Foto e Identificação do Produto ---
            ProductHeroSection(product = product)

            Spacer(modifier = Modifier.height(16.dp))

            // --- SEÇÃO 1: ESTOQUE NA DESPENSA ---
            ProductPantrySection(
                pantryStock = uiState.pantryStock,
                onAddStock = viewModel::onOpenAddStockSheet,
                onConsume = viewModel::onOpenConsumeDialog,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- SEÇÃO 2: HISTÓRICO DE PREÇOS ---
            ProductPriceHistorySection(
                latestPrice = uiState.latestPrice,
                averagePrice = uiState.averagePrice,
                lowestPrice = uiState.lowestPrice,
                highestPrice = uiState.highestPrice,
                priceHistory = uiState.priceHistory,
            )

            Spacer(modifier = Modifier.height(32.dp))
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                MinhaDespensaText(
                    text = uiState.error?.asString() ?: stringResource(Res.string.no_itens_found),
                    color = colors.onSurface.copy(alpha = 0.6f),
                    alignment = TextAlign.Center,
                )
            }
        }
    }

    // --- SUBFLUXOS E MODAIS ---
    when (val subFlow = uiState.activeSubFlow) {
        is ProductDetailsSubFlow.AddPantryStock -> {
            AddPantryItemDetailsSheet(
                product = subFlow.product,
                onConfirm = { _, qty, expDate, batch ->
                    viewModel.onAddPantryStock(qty, expDate, batch)
                },
                onDismiss = viewModel::onDismissSubFlow,
            )
        }

        is ProductDetailsSubFlow.ConsumeStock -> {
            ConsumeStockDialog(
                maxQuantity = uiState.pantryStock?.totalQuantity ?: 1.0,
                unit = uiState.product?.measureUnit?.toAbbreviation() ?: "un",
                onConfirm = { qty ->
                    viewModel.onConsumeStock(quantity = qty)
                },
                onDismiss = viewModel::onDismissSubFlow,
            )
        }

        is ProductDetailsSubFlow.SelectShoppingList -> {
            SelectShoppingListDialog(
                shoppingLists = uiState.activeShoppingLists,
                onListSelected = { listId ->
                    viewModel.onAddToShoppingList(listId)
                },
                onDismiss = viewModel::onDismissSubFlow,
            )
        }

        null -> Unit
    }
}

// -------------------------------------------------------------------------
// COMPONENTES DE SEÇÃO
// -------------------------------------------------------------------------

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ProductHeroSection(product: ProductDetailsInfoUiModel) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens

    val imageBitmap = remember(product.imageBytes) {
        product.imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    ItemContainerGlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Imagem do Produto
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(dimens.cardCorner * 0.75f))
                    .background(colors.surfaceContainerHigh.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Kitchen,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            // Nome do Produto
            MinhaDespensaText(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontStyle = MinhaDespensaTheme.typography.displayMedium,
                color = colors.onSurface,
                alignment = TextAlign.Center,
            )

            // Linha de Tags / Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth(),
            ) {
                // Categoria
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.primary.copy(alpha = 0.12f),
                        labelColor = colors.primary,
                    ),
                    border = null,
                )

                // Marca (se houver)
                if (!product.brand.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = product.brand,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colors.secondary.copy(alpha = 0.12f),
                            labelColor = colors.onSurface,
                        ),
                        border = null,
                    )
                }

                // Conteúdo / Peso Líquido
                AssistChip(
                    onClick = {},
                    label = {
                        val formattedWeight = if (product.netWeight % 1.0 == 0.0) {
                            product.netWeight.toLong().toString()
                        } else {
                            product.netWeight.toString().replace('.', ',')
                        }
                        Text(
                            text = "$formattedWeight ${product.measureUnit.toAbbreviation()}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.surfaceContainerHigh.copy(alpha = 0.6f),
                        labelColor = colors.onSurfaceVariant,
                    ),
                    border = null,
                )
            }

            // Código de Barras (EAN)
            if (!product.ean.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = stringResource(Res.string.product_details_ean_label),
                        tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                    MinhaDespensaText(
                        text = product.ean,
                        fontStyle = MinhaDespensaTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductPantrySection(
    pantryStock: ProductPantryStockUiModel?,
    onAddStock: () -> Unit,
    onConsume: () -> Unit,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    SecondaryContainerSection(
        title = stringResource(Res.string.product_details_section_pantry),
    ) {
        if (pantryStock != null && pantryStock.hasStock) {
            // Destaque de Estoque e Validade Mais Próxima
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    MinhaDespensaText(
                        text = stringResource(Res.string.product_details_pantry_total_stock),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    val formattedQty = if (pantryStock.totalQuantity % 1.0 == 0.0) {
                        pantryStock.totalQuantity.toLong().toString()
                    } else {
                        pantryStock.totalQuantity.toString().replace('.', ',')
                    }
                    MinhaDespensaText(
                        text = "$formattedQty ${pantryStock.measureUnit.toAbbreviation()}",
                        fontStyle = typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                }

                // Badge de Status de Validade
                if (pantryStock.closestExpirationDate != null) {
                    val formattedDate = formatEpochDate(pantryStock.closestExpirationDate)
                    val badgeBg = if (pantryStock.isExpired) {
                        colors.error.copy(alpha = 0.15f)
                    } else {
                        colors.primary.copy(alpha = 0.15f)
                    }
                    val badgeColor = if (pantryStock.isExpired) colors.error else colors.primary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (pantryStock.isExpired) Icons.Rounded.WarningAmber else Icons.Rounded.Event,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(14.dp),
                            )
                            MinhaDespensaText(
                                text = formattedDate,
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                            )
                        }
                    }
                }
            }

            // Lista dos lotes cadastrados
            if (pantryStock.batches.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = colors.onSurface.copy(alpha = 0.08f),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pantryStock.batches.forEach { batch ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                val batchQty = if (batch.quantity % 1.0 == 0.0) {
                                    batch.quantity.toLong().toString()
                                } else {
                                    batch.quantity.toString().replace('.', ',')
                                }
                                MinhaDespensaText(
                                    text = "$batchQty ${pantryStock.measureUnit.toAbbreviation()}",
                                    fontStyle = typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface,
                                )
                                if (batch.batchNumber != null) {
                                    MinhaDespensaText(
                                        text = stringResource(Res.string.product_details_pantry_batch_label, batch.batchNumber),
                                        fontStyle = typography.bodySmall,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                            }

                            if (batch.expirationDate != null) {
                                MinhaDespensaText(
                                    text = formatEpochDate(batch.expirationDate),
                                    fontStyle = typography.bodySmall,
                                    color = if (batch.isExpired) colors.error else colors.onSurfaceVariant,
                                    fontWeight = if (batch.isExpired) FontWeight.Bold else FontWeight.Normal,
                                )
                            } else {
                                MinhaDespensaText(
                                    text = stringResource(Res.string.product_details_pantry_no_expiration),
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }

            // Botão de Consumo
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.product_details_pantry_consume_button),
                onClick = onConsume,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                leadingIcon = Icons.Rounded.RemoveCircleOutline,
            )
        } else {
            // Sem estoque
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.product_details_pantry_out_of_stock),
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = typography.bodyLarge,
                    color = colors.onSurface,
                )
                MinhaDespensaText(
                    text = stringResource(Res.string.product_details_pantry_out_of_stock_desc),
                    fontStyle = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    alignment = TextAlign.Center,
                )
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.product_details_action_add_to_pantry),
                    onClick = onAddStock,
                    leadingIcon = Icons.Rounded.Add,
                )
            }
        }
    }
}

@Composable
private fun ProductPriceHistorySection(
    latestPrice: Long?,
    averagePrice: Long?,
    lowestPrice: Long?,
    highestPrice: Long?,
    priceHistory: List<ProductPriceEntryUiModel>,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    SecondaryContainerSection(
        title = stringResource(Res.string.product_details_section_prices),
    ) {
        if (priceHistory.isNotEmpty() && latestPrice != null) {
            // Resumo Financeiro (Último, Médio, Menor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    MinhaDespensaText(
                        text = stringResource(Res.string.product_details_price_latest),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    MinhaDespensaText(
                        text = latestPrice.formatPrice(includeCurrencySymbol = true),
                        fontStyle = typography.priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                }

                if (averagePrice != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.product_details_price_average),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                        MinhaDespensaText(
                            text = averagePrice.formatPrice(includeCurrencySymbol = true),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = colors.onSurface.copy(alpha = 0.08f),
            )

            // Lista de Compras Anteriores
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                priceHistory.take(5).forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            MinhaDespensaText(
                                text = entry.storeName,
                                fontStyle = typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                            )
                            MinhaDespensaText(
                                text = formatEpochDate(entry.date),
                                fontStyle = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }

                        MinhaDespensaText(
                            text = entry.priceInCents.formatPrice(includeCurrencySymbol = true),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.product_details_price_empty),
                    fontStyle = typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    alignment = TextAlign.Center,
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// DIÁLOGOS E MODAIS
// -------------------------------------------------------------------------

@Composable
private fun ConsumeStockDialog(
    maxQuantity: Double,
    unit: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialQty = if (maxQuantity in 0.0..1.0) maxQuantity else 1.0
    var quantityToConsume by remember(maxQuantity) { mutableStateOf(initialQty) }
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.product_details_pantry_consume_dialog_title),
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.shopping_lists_delete_dialog_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.product_details_pantry_consume_confirm),
                    onClick = { onConfirm(quantityToConsume) },
                    modifier = Modifier.weight(1f),
                    enabled = quantityToConsume > 0.0 && quantityToConsume <= maxQuantity,
                )
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.product_details_pantry_consume_dialog_desc),
                    fontStyle = typography.bodyLarge,
                    color = colors.onSurface,
                    alignment = TextAlign.Center,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IconButton(
                        onClick = {
                            val next = quantityToConsume - 1.0
                            quantityToConsume = if (next < 1.0 && maxQuantity < 1.0) maxQuantity else maxOf(1.0, next)
                        },
                        enabled = quantityToConsume > 1.0,
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Diminuir")
                    }

                    val formattedQty = if (quantityToConsume % 1.0 == 0.0) {
                        quantityToConsume.toLong().toString()
                    } else {
                        quantityToConsume.toString().replace('.', ',')
                    }
                    MinhaDespensaText(
                        text = "$formattedQty $unit",
                        fontStyle = typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )

                    IconButton(
                        onClick = {
                            quantityToConsume = minOf(maxQuantity, quantityToConsume + 1.0)
                        },
                        enabled = quantityToConsume < maxQuantity,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Aumentar")
                    }
                }
            }
        },
    )
}

@Composable
private fun SelectShoppingListDialog(
    shoppingLists: List<ShoppingListOptionUiModel>,
    onListSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.product_details_select_list_title),
        buttons = {
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.shopping_lists_delete_dialog_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        content = {
            if (shoppingLists.isEmpty()) {
                MinhaDespensaText(
                    text = stringResource(Res.string.product_details_no_active_lists),
                    fontStyle = typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    alignment = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.product_details_select_list_desc),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )

                    shoppingLists.forEach { list ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onListSelected(list.id) },
                            color = colors.surfaceContainerHigh.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MinhaDespensaText(
                                    text = list.name,
                                    fontStyle = typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface,
                                )
                                MinhaDespensaText(
                                    text = "${list.itemsCount} itens",
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

private fun formatEpochDate(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth.toString().padStart(2, '0')}/${dateTime.monthNumber.toString().padStart(2, '0')}/${dateTime.year}"
}

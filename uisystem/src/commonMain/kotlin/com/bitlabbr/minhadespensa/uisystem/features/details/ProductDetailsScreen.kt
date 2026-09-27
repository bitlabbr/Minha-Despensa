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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerSection
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.media.ImagePickerCard
import com.bitlabbr.minhadespensa.uisystem.components.core.media.ImageSourcePickerDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.media.rememberImagePickerManager
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductDropdownField
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.features.details.model.*
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.AddPantryItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.mapper.toLabel
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
    fromPantry: Boolean = false,
    onNavigateBack: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: ProductDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {
            MinhaDespensaTopBar()
        },
        bottomBar = {
            if (uiState.product != null && !uiState.isEditing) {
                Surface(
                    color = colors.surface.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!fromPantry) {
                            MinhaDespensaSecondaryButton(
                                text = stringResource(Res.string.product_details_action_add_to_pantry),
                                onClick = viewModel::onOpenAddStockSheet,
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Rounded.Kitchen,
                            )
                        }
                        MinhaDespensaPrimaryButton(
                            text = stringResource(Res.string.product_details_action_add_to_list),
                            onClick = viewModel::onOpenAddToListDialog,
                            modifier = if (!fromPantry) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Rounded.AddShoppingCart,
                        )
                    }
                }
            }
        },
    ) {
        if (!uiState.isEditing) {
            PrimaryContainerHeader(
                textTop = stringResource(Res.string.product_details_header_top),
                textBottom = stringResource(Res.string.product_details_header_bottom),
                onBackClick = onNavigateBack,
                actionIcon = Icons.Rounded.Edit,
                actionContentDescription = stringResource(Res.string.product_details_action_edit),
                onActionClick = viewModel::onStartEdit,
            )
        } else {
            PrimaryContainerHeader(
                textTop = stringResource(Res.string.product_details_edit_header_top),
                textBottom = stringResource(Res.string.product_details_edit_header_bottom),
                onBackClick = viewModel::onCancelEdit,
                actionIcon = Icons.Rounded.Close,
                actionContentDescription = stringResource(Res.string.product_details_cancel_edit),
                onActionClick = viewModel::onCancelEdit,
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (uiState.product != null) {
            val product = uiState.product!!

            if (uiState.isEditing) {
                // --- MODO DE EDIÇÃO: Todos os campos editáveis ---
                ProductEditSection(
                    formState = uiState.editForm,
                    onFormChange = viewModel::onFormChange,
                    onSave = viewModel::onSaveProduct,
                    onCancel = viewModel::onCancelEdit,
                )
            } else {
                // --- MODO DE VISUALIZAÇÃO: Todos os campos do produto exibidos ---
                ProductHeroSection(
                    product = product,
                    onEditClick = viewModel::onStartEdit,
                )

                // --- SEÇÃO 1: ESTOQUE NA DESPENSA ---
                ProductPantrySection(
                    pantryStock = uiState.pantryStock,
                    onAddStock = viewModel::onOpenAddStockSheet,
                    onConsume = viewModel::onOpenConsumeDialog,
                )

                // --- SEÇÃO 2: HISTÓRICO E VARIAÇÃO DE PREÇOS ---
                ProductPriceHistorySection(
                    latestPrice = uiState.latestPrice,
                    averagePrice = uiState.averagePrice,
                    lowestPrice = uiState.lowestPrice,
                    highestPrice = uiState.highestPrice,
                    priceVariationPercentage = uiState.priceVariationPercentage,
                    priceDifference = uiState.priceDifference,
                    priceHistory = uiState.priceHistory,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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

    // --- SUBFLUXOS E MODAIS (se acionados) ---
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
                unit = uiState.pantryStock?.measureUnit?.toAbbreviation() ?: "un",
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
// COMPONENTES DE VISUALIZAÇÃO E EDIÇÃO DO PRODUTO
// -------------------------------------------------------------------------

@OptIn(ExperimentalResourceApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ProductHeroSection(
    product: ProductDetailsInfoUiModel,
    onEditClick: () -> Unit,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    val imageBitmap = remember(product.imageBytes) {
        product.imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    SecondaryContainerGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimens.paddingSmall,
                vertical = dimens.paddingSmall,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.paddingSmall / 2,
                    vertical = dimens.paddingMedium,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Imagem do Produto
            Box(
                modifier = Modifier
                    .size(150.dp)
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
                fontStyle = typography.displayMedium,
                color = colors.onSurface,
                alignment = TextAlign.Center,
            )

            // Exibição de TODOS os campos do produto
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Categoria
                ProductDetailBadge(
                    label = stringResource(Res.string.product_details_category_label),
                    value = product.category,
                    icon = Icons.Rounded.Category,
                )

                // Marca
                val brandText = if (!product.brand.isNullOrBlank()) {
                    product.brand
                } else {
                    stringResource(Res.string.product_details_no_brand)
                }
                ProductDetailBadge(
                    label = stringResource(Res.string.product_details_brand_label),
                    value = brandText,
                    icon = Icons.Rounded.Sell,
                )

                // Conteúdo / Peso Líquido
                val formattedWeight = if (product.netWeight % 1.0 == 0.0) {
                    product.netWeight.toLong().toString()
                } else {
                    product.netWeight.toString().replace('.', ',')
                }
                ProductDetailBadge(
                    label = stringResource(Res.string.product_details_netweight_label),
                    value = "$formattedWeight ${product.measureUnit.toAbbreviation()}",
                    icon = Icons.Rounded.Scale,
                )

                // Unidade de Medida Completa
                ProductDetailBadge(
                    label = stringResource(Res.string.register_product_form_basic_info_unit_label),
                    value = product.measureUnit.toLabel(),
                    icon = Icons.Rounded.Straighten,
                )

                // Código de Barras (EAN)
                val eanText = if (!product.ean.isNullOrBlank()) {
                    product.ean
                } else {
                    stringResource(Res.string.product_details_no_ean)
                }
                ProductDetailBadge(
                    label = stringResource(Res.string.product_details_ean_label),
                    value = eanText,
                    icon = Icons.Rounded.QrCode,
                )

                // Observações (se houver)
                if (!product.notes.isNullOrBlank()) {
                    ProductDetailBadge(
                        label = stringResource(Res.string.product_details_notes_label),
                        value = product.notes,
                        icon = Icons.AutoMirrored.Rounded.Notes,
                    )
                }
            }

            // Botão de Ação para Iniciar Edição
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.product_details_action_edit),
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                leadingIcon = Icons.Rounded.Edit,
            )
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

                if (pantryStock.closestExpirationDate != null) {
                    val formattedDate = formatEpochDate(pantryStock.closestExpirationDate)
                    val badgeBg = if (pantryStock.isExpired) colors.error.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f)
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

            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.product_details_pantry_consume_button),
                onClick = onConsume,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                leadingIcon = Icons.Rounded.RemoveCircleOutline,
            )
        } else {
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
private fun ProductEditSection(
    formState: ProductFormState,
    onFormChange: (ProductFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens

    var showImageSourcePicker by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val imagePickerManager = rememberImagePickerManager { bytes ->
        onFormChange(formState.copy(imageBytes = bytes))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Foto do Produto
        SecondaryContainerSection(
            title = stringResource(Res.string.register_product_form_image_title),
        ) {
            ImagePickerCard(
                imageBytes = formState.imageBytes,
                onClick = { showImageSourcePicker = true },
                onClearImage = { onFormChange(formState.copy(imageBytes = null)) },
            )
        }

        // 2. Informações Básicas
        SecondaryContainerSection(
            title = stringResource(Res.string.register_product_form_basic_info_section_title),
        ) {
            // Nome
            ProductTextField(
                value = formState.name,
                onValueChange = { onFormChange(formState.copy(name = it, nameError = null)) },
                label = stringResource(Res.string.register_product_form_basic_info_product_label),
                placeholder = stringResource(Res.string.register_product_form_basic_info_product_placeholder),
                isRequired = true,
                maxCharacters = CoreConstants.Product.NAME_MAX_LENGTH,
                errorMessage = formState.nameError?.asString(),
            )

            // Peso Líquido e Unidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                ProductTextField(
                    modifier = Modifier.weight(1f),
                    value = formState.netWeight,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }.take(8)
                        onFormChange(formState.copy(netWeight = filtered, netWeightError = null))
                    },
                    label = stringResource(Res.string.register_product_form_basic_info_netweight_label),
                    placeholder = stringResource(Res.string.register_product_form_basic_info_netweight_placeholder),
                    keyboardType = KeyboardType.Decimal,
                    isRequired = true,
                    maxCharacters = CoreConstants.Product.WEIGHT_MAX_LENGTH,
                    errorMessage = formState.netWeightError?.asString(),
                )

                ProductDropdownField(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.register_product_form_basic_info_unit_label),
                    selectedOption = formState.measureUnit,
                    placeholder = stringResource(Res.string.register_product_form_basic_info_unit_placeholder),
                    options = MeasureUnit.entries,
                    optionLabel = { it.toLabel() },
                    isRequired = true,
                    errorMessage = formState.measureUnitError?.asString(),
                    onSelected = { unit ->
                        onFormChange(formState.copy(measureUnit = unit, measureUnitError = null))
                    },
                )
            }

            // Categoria
            ProductDropdownField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.register_product_form_basic_info_product_category),
                selectedOption = formState.category.takeIf { it.isNotBlank() },
                placeholder = stringResource(Res.string.register_product_form_basic_info_category_placeholder),
                options = formState.availableCategories.filter {
                    !it.equals(stringResource(Res.string.category_filter_all), ignoreCase = true)
                },
                optionLabel = { it },
                isRequired = true,
                errorMessage = formState.categoryError?.asString(),
                onSelected = { onFormChange(formState.copy(category = it, categoryError = null)) },
            )
        }

        // 3. Detalhes Adicionais
        SecondaryContainerSection(
            title = stringResource(Res.string.register_product_form_detail_info_section_title),
        ) {
            // Marca
            ProductTextField(
                value = formState.brand,
                onValueChange = { onFormChange(formState.copy(brand = it)) },
                label = stringResource(Res.string.register_product_form_detail_info_brand_label),
                placeholder = stringResource(Res.string.register_product_form_detail_info_brand_placeholder),
                maxCharacters = CoreConstants.Product.BRAND_MAX_LENGTH,
            )

            // EAN com Scanner
            ProductTextField(
                value = formState.ean,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(14)
                    onFormChange(formState.copy(ean = digits, eanError = null))
                },
                label = stringResource(Res.string.register_product_form_detail_info_ean_label),
                placeholder = stringResource(Res.string.register_product_form_detail_info_ean_placeholder),
                keyboardType = KeyboardType.Number,
                maxCharacters = 14,
                errorMessage = formState.eanError?.asString(),
                trailingContent = {
                    if (formState.isCheckingEan) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(2.dp),
                            strokeWidth = 2.dp,
                            color = colors.primary,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(dimens.cardCorner * 0.35f))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .clickable { showBarcodeScanner = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DocumentScanner,
                                contentDescription = stringResource(Res.string.register_product_form_detail_info_ean_icon_description),
                                tint = if (formState.eanError != null) colors.error else colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
            )

            // Observações
            ProductTextField(
                value = formState.notes,
                onValueChange = { onFormChange(formState.copy(notes = it)) },
                label = stringResource(Res.string.register_product_form_detail_info_notes_label),
                placeholder = stringResource(Res.string.register_product_form_detail_info_notes_placeholder),
                minLines = 3,
                singleLine = false,
                maxCharacters = CoreConstants.Product.NOTES_MAX_LENGTH,
            )
        }

        // 4. Botões de Ação
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.product_details_cancel_edit),
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !formState.isSaving,
            )
            MinhaDespensaPrimaryButton(
                text = if (formState.isSaving) {
                    stringResource(Res.string.register_product_form_button_saving)
                } else {
                    stringResource(Res.string.product_details_save_changes)
                },
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = formState.isFormValid && !formState.isSaving,
                leadingIcon = if (!formState.isSaving) Icons.Rounded.Check else null,
            )
        }
    }

    if (showImageSourcePicker) {
        ImageSourcePickerDialog(
            onDismissRequest = { showImageSourcePicker = false },
            onCameraSelect = {
                showImageSourcePicker = false
                imagePickerManager.launchCamera()
            },
            onGallerySelect = {
                showImageSourcePicker = false
                imagePickerManager.launchGallery()
            },
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onBarcodeScanned = { ean ->
                showBarcodeScanner = false
                val digits = ean.filter { it.isDigit() }.take(14)
                onFormChange(formState.copy(ean = digits, eanError = null))
            },
            onDismissRequest = { showBarcodeScanner = false },
        )
    }
}

@Composable
private fun ProductDetailBadge(
    label: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.cardCorner * 0.45f),
        color = colors.surface.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MinhaDespensaText(
                    text = "$label:",
                    fontStyle = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
                MinhaDespensaText(
                    text = value,
                    fontStyle = typography.bodySmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// SEÇÃO DE HISTÓRICO E VARIAÇÃO DE PREÇOS
// -------------------------------------------------------------------------

@Composable
private fun ProductPriceHistorySection(
    latestPrice: Long?,
    averagePrice: Long?,
    lowestPrice: Long?,
    highestPrice: Long?,
    priceVariationPercentage: Double?,
    priceDifference: Long?,
    priceHistory: List<ProductPriceEntryUiModel>,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    SecondaryContainerSection(
        title = stringResource(Res.string.product_details_section_prices),
    ) {
        if (priceHistory.isNotEmpty() && latestPrice != null) {
            // --- CARD DE DESTAQUE: VARIAÇÃO DE PREÇO ---
            if (lowestPrice != null && highestPrice != null && highestPrice > lowestPrice) {
                val formattedPct = priceVariationPercentage?.let {
                    val rounded = (it * 10).toLong() / 10.0
                    if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString().replace('.', ',')
                } ?: "0"
                val formattedDiff = priceDifference?.formatPrice(includeCurrencySymbol = true) ?: ""

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            MinhaDespensaText(
                                text = stringResource(Res.string.product_details_price_variation_title),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.product_details_price_variation_desc, formattedPct),
                                fontStyle = typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                            )
                            if (formattedDiff.isNotBlank()) {
                                MinhaDespensaText(
                                    text = stringResource(Res.string.product_details_price_difference, formattedDiff),
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else if (priceHistory.size == 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceContainerHigh.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.08f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.product_details_price_single_recorded),
                            fontStyle = typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            // --- GRID DE MÉTRICAS FINANCEIRAS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Menor Preço
                if (lowestPrice != null) {
                    PriceMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.product_details_price_lowest),
                        value = lowestPrice.formatPrice(includeCurrencySymbol = true),
                        indicatorColor = Color(0xFF4CAF50),
                    )
                }

                // Maior Preço
                if (highestPrice != null) {
                    PriceMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.product_details_price_highest),
                        value = highestPrice.formatPrice(includeCurrencySymbol = true),
                        indicatorColor = if (highestPrice > (lowestPrice ?: 0L)) colors.error else colors.primary,
                    )
                }

                // Preço Médio
                if (averagePrice != null) {
                    PriceMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.product_details_price_average),
                        value = averagePrice.formatPrice(includeCurrencySymbol = true),
                        indicatorColor = colors.onSurfaceVariant,
                    )
                }

                // Último Preço
                PriceMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.product_details_price_latest),
                    value = latestPrice.formatPrice(includeCurrencySymbol = true),
                    indicatorColor = colors.primary,
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = colors.onSurface.copy(alpha = 0.08f),
            )

            // --- LISTA DE COMPRAS REGISTRADAS ---
            MinhaDespensaText(
                text = stringResource(Res.string.product_details_price_purchases_title),
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                priceHistory.take(10).forEach { entry ->
                    val isLowest = lowestPrice != null && entry.priceInCents == lowestPrice && priceHistory.size > 1
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surfaceContainerHigh.copy(alpha = 0.35f),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    MinhaDespensaText(
                                        text = entry.storeName.ifBlank { stringResource(Res.string.product_details_store_fallback) },
                                        fontStyle = typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface,
                                    )
                                    if (isLowest) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                        ) {
                                            MinhaDespensaText(
                                                text = stringResource(Res.string.product_details_price_lowest),
                                                fontStyle = typography.bodySmall,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            )
                                        }
                                    }
                                }
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
                                color = if (isLowest) Color(0xFF4CAF50) else colors.primary,
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
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

@Composable
private fun PriceMetricCard(
    label: String,
    value: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceContainerHigh.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            MinhaDespensaText(
                text = label,
                fontStyle = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MinhaDespensaText(
                text = value,
                fontStyle = typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = indicatorColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// -------------------------------------------------------------------------
// DIÁLOGOS E MODAIS ADICIONAIS
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

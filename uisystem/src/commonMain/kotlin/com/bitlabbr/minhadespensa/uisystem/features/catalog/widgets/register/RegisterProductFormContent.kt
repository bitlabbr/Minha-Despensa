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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaExpandButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerSection
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.media.ImagePickerCard
import com.bitlabbr.minhadespensa.uisystem.components.core.media.ImageSourcePickerDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.media.rememberImagePickerManager
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductDropdownField
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.mapper.toLabel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterProductFormContent(
    state: ProductFormState,
    onStateChange: (ProductFormState) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    var isExpanded by remember { mutableStateOf(false) }
    var showImageSourcePicker by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val imagePickerManager = rememberImagePickerManager { bytes ->
        onStateChange(state.copy(imageBytes = bytes))
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = dimens.paddingLarge),
    ) {
        item {
            PrimaryContainerHeader(
                textTop = stringResource(Res.string.register_product_form_header_title_top),
                textBottom = stringResource(Res.string.register_product_form_header_title_bottom),
                description = stringResource(Res.string.register_product_form_header_title_desc),
                actionIcon = Icons.Rounded.Close,
                actionContentDescription = stringResource(Res.string.register_product_form_header_close_button_desc),
                onActionClick = onCancelClick,
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // Seção de Imagem
                SecondaryContainerSection(
                    title = stringResource(Res.string.register_product_form_image_title),
                ) {
                    ImagePickerCard(
                        imageBytes = state.imageBytes,
                        onClick = { showImageSourcePicker = true },
                        onClearImage = { onStateChange(state.copy(imageBytes = null)) },
                    )
                }

                // Dados Básicos
                SecondaryContainerSection(
                    title = stringResource(Res.string.register_product_form_basic_info_section_title),
                ) {
                    ProductTextField(
                        value = state.name,
                        onValueChange = { onStateChange(state.copy(name = it, nameError = null)) },
                        label = stringResource(Res.string.register_product_form_basic_info_product_label),
                        placeholder = stringResource(Res.string.register_product_form_basic_info_product_placeholder),
                        isRequired = true,
                        maxCharacters = CoreConstants.Product.NAME_MAX_LENGTH,
                        errorMessage = state.nameError?.asString(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    ) {
                        ProductTextField(
                            modifier = Modifier.weight(1f),
                            value = state.netWeight,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }.take(8)
                                onStateChange(state.copy(netWeight = filtered, netWeightError = null))
                            },
                            label = stringResource(Res.string.register_product_form_basic_info_netweight_label),
                            placeholder = stringResource(Res.string.register_product_form_basic_info_netweight_placeholder),
                            keyboardType = KeyboardType.Decimal,
                            isRequired = true,
                            maxCharacters = CoreConstants.Product.WEIGHT_MAX_LENGTH,
                            errorMessage = state.netWeightError?.asString(),
                        )

                        ProductDropdownField(
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.register_product_form_basic_info_unit_label),
                            selectedOption = state.measureUnit,
                            placeholder = stringResource(Res.string.register_product_form_basic_info_unit_placeholder),
                            options = MeasureUnit.entries,
                            optionLabel = { it.toLabel() },
                            isRequired = true,
                            errorMessage = state.measureUnitError?.asString(),
                            onSelected = { unit ->
                                onStateChange(state.copy(measureUnit = unit, measureUnitError = null))
                            },
                        )
                    }

                    ProductDropdownField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.register_product_form_basic_info_product_category),
                        selectedOption = state.category.takeIf { it.isNotBlank() },
                        placeholder = stringResource(Res.string.register_product_form_basic_info_category_placeholder),
                        options = state.availableCategories.filter {
                            !it.equals(
                                stringResource(Res.string.category_filter_all),
                                ignoreCase = true
                            )
                        },
                        optionLabel = { it },
                        isRequired = true,
                        errorMessage = state.categoryError?.asString(),
                        onSelected = { onStateChange(state.copy(category = it, categoryError = null)) },
                    )
                }

                // Seção Expansível
                if (isExpanded) {
                    SecondaryContainerSection(
                        title = stringResource(Res.string.register_product_form_detail_info_section_title),
                    ) {
                        ProductTextField(
                            value = state.ean,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(14)
                                onStateChange(state.copy(ean = digits, eanError = null))
                            },
                            label = stringResource(Res.string.register_product_form_detail_info_ean_label),
                            placeholder = stringResource(Res.string.register_product_form_detail_info_ean_placeholder),
                            keyboardType = KeyboardType.Number,
                            maxCharacters = 14,
                            errorMessage = state.eanError?.asString(),
                            trailingContent = {
                                if (state.isCheckingEan) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(2.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.onSecondaryContainer,
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
                                            tint = if (state.eanError != null) colors.error else colors.onSecondaryContainer.copy(
                                                alpha = 0.75f
                                            ),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                        )

                        ProductTextField(
                            value = state.brand,
                            onValueChange = { onStateChange(state.copy(brand = it)) },
                            label = stringResource(Res.string.register_product_form_detail_info_brand_label),
                            placeholder = stringResource(Res.string.register_product_form_detail_info_brand_placeholder),
                            maxCharacters = CoreConstants.Product.BRAND_MAX_LENGTH,
                        )

                        ProductTextField(
                            value = state.notes,
                            onValueChange = { onStateChange(state.copy(notes = it)) },
                            label = stringResource(Res.string.register_product_form_detail_info_notes_label),
                            placeholder = stringResource(Res.string.register_product_form_detail_info_notes_placeholder),
                            minLines = 3,
                            singleLine = false,
                            maxCharacters = CoreConstants.Product.NOTES_MAX_LENGTH,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MinhaDespensaExpandButton(
                        isExpanded = isExpanded,
                        onClick = { isExpanded = !isExpanded },
                        collapsedText = stringResource(Res.string.register_product_form_expand_button_see_more),
                        expandedText = stringResource(Res.string.register_product_form_expand_button_see_less),
                    )
                }

                Spacer(Modifier.height(dimens.paddingMedium))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.paddingSmall),
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaSecondaryButton(
                        text = stringResource(Res.string.register_product_form_button_cancel),
                        modifier = Modifier.weight(1f),
                        onClick = onCancelClick,
                    )

                    MinhaDespensaPrimaryButton(
                        text = if (state.isSaving) stringResource(Res.string.register_product_form_button_saving) else stringResource(
                            Res.string.register_product_form_button_save
                        ),
                        modifier = Modifier.weight(1.75f),
                        enabled = state.isFormValid,
                        isLoading = state.isSaving,
                        onClick = onSaveClick,
                    )
                }
            }
        }
    }

    if (showImageSourcePicker) {
        ImageSourcePickerDialog(
            onDismissRequest = { showImageSourcePicker = false },
            onCameraSelect = { imagePickerManager.launchCamera() },
            onGallerySelect = { imagePickerManager.launchGallery() },
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onBarcodeScanned = { scannedCode ->
                val numbersOnly = scannedCode.filter { it.isDigit() }.take(14)
                onStateChange(state.copy(ean = numbersOnly, eanError = null))
                showBarcodeScanner = false
            },
            onDismissRequest = { showBarcodeScanner = false },
        )
    }
}
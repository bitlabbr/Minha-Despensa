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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductDropdownField
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.mapper.toLabel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductSheet(
    isOpen: Boolean,
    formState: ProductFormState,
    onFormChange: (ProductFormState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) return

    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    var isExpanded by remember { mutableStateOf(false) }
    var showImageSourcePicker by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val imagePickerManager = rememberImagePickerManager { bytes ->
        onFormChange(formState.copy(imageBytes = bytes))
    }

    val selectedMeasureUnit = remember(formState.measureUnit) {
        MeasureUnit.entries.find { it.name == formState.measureUnit }
    }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                PrimaryContainerHeader(
                    textTop = stringResource(Res.string.register_product_form_header_title_top),
                    textBottom = stringResource(Res.string.register_product_form_header_title_bottom),
                    description = stringResource(Res.string.register_product_form_header_title_desc),
                    actionIcon = Icons.Rounded.Close,
                    actionContentDescription = stringResource(Res.string.register_product_form_header_close_button_desc),
                    onActionClick = onDismiss,
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    SecondaryContainerSection(
                        title = stringResource(Res.string.register_product_form_image_title),
                    ) {
                        ImagePickerCard(
                            imageBytes = formState.imageBytes,
                            onClick = { showImageSourcePicker = true },
                            onClearImage = { onFormChange(formState.copy(imageBytes = null)) },
                        )
                    }

                    SecondaryContainerSection(
                        title = stringResource(Res.string.register_product_form_basic_info_section_title),
                    ) {
                        ProductTextField(
                            value = formState.name,
                            onValueChange = { onFormChange(formState.copy(name = it, nameError = null)) },
                            label = stringResource(Res.string.register_product_form_basic_info_product_label),
                            placeholder = stringResource(Res.string.register_product_form_basic_info_product_placeholder),
                            isRequired = true,
                            maxCharacters = CoreConstants.Product.NAME_MAX_LENGTH,
                            errorMessage = formState.nameError?.asString(),
                        )

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
                                selectedOption = selectedMeasureUnit,
                                placeholder = stringResource(Res.string.register_product_form_basic_info_unit_placeholder),
                                options = MeasureUnit.entries,
                                optionLabel = { it.toLabel() },
                                isRequired = true,
                                errorMessage = formState.measureUnitError?.asString(),
                                onSelected = { unit ->
                                    onFormChange(formState.copy(measureUnit = unit.name, measureUnitError = null))
                                },
                            )
                        }

                        ProductDropdownField(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.register_product_form_basic_info_product_category),
                            selectedOption = formState.category.takeIf { it.isNotBlank() },
                            placeholder = stringResource(Res.string.register_product_form_basic_info_category_placeholder),
                            options = formState.availableCategories.filter {
                                !it.equals(
                                    stringResource(Res.string.category_filter_all),
                                    ignoreCase = true
                                )
                            },
                            optionLabel = { it },
                            isRequired = true,
                            errorMessage = formState.categoryError?.asString(),
                            onSelected = { selectedCategory ->
                                onFormChange(formState.copy(category = selectedCategory, categoryError = null))
                            },
                        )
                    }

                    if (isExpanded) {
                        SecondaryContainerSection(
                            title = stringResource(Res.string.register_product_form_detail_info_section_title),
                        ) {
                            ProductTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = formState.ean,
                                onValueChange = { input ->
                                    val numbersOnly = input.filter { it.isDigit() }.take(14)
                                    onFormChange(formState.copy(ean = numbersOnly, eanError = null))
                                },
                                label = stringResource(Res.string.register_product_form_detail_info_ean_label),
                                placeholder = stringResource(Res.string.register_product_form_detail_info_ean_placeholder),
                                keyboardType = KeyboardType.Number,
                                maxCharacters = 14,
                                errorMessage = formState.eanError?.asString(),
                                trailingContent = {
                                    if (formState.isCheckingEan) {
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
                                                tint = if (formState.eanError != null) colors.error else colors.onSecondaryContainer.copy(
                                                    alpha = 0.75f
                                                ),
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                },
                            )

                            ProductTextField(
                                value = formState.brand,
                                onValueChange = { onFormChange(formState.copy(brand = it)) },
                                label = stringResource(Res.string.register_product_form_detail_info_brand_label),
                                placeholder = stringResource(Res.string.register_product_form_detail_info_brand_placeholder),
                                maxCharacters = CoreConstants.Product.BRAND_MAX_LENGTH,
                            )

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
                            onClick = onDismiss,
                        )

                        MinhaDespensaPrimaryButton(
                            text = if (formState.isSaving) stringResource(Res.string.register_product_form_button_saving) else stringResource(
                                Res.string.register_product_form_button_save
                            ),
                            modifier = Modifier.weight(1.75f),
                            enabled = formState.isFormValid,
                            isLoading = formState.isSaving,
                            onClick = onSave,
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
                    onFormChange(formState.copy(ean = numbersOnly, eanError = null))
                    showBarcodeScanner = false
                },
                onDismissRequest = { showBarcodeScanner = false },
            )
        }
    }
}
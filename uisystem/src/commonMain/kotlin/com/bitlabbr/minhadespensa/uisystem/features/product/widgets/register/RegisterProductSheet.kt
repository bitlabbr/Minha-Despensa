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
package com.bitlabbr.minhadespensa.uisystem.features.product.widgets.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.*
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.PantryMockData
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductSheet(
    state: ProductFormState,
    sheetState: SheetState,
    onBack: () -> Unit,
    onCancel: () -> Unit = onBack,
    onSave: () -> Unit,
    onStateChange: (ProductFormState) -> Unit,
    onDismiss: () -> Unit
) {
    val typography = MinhaDespensaTheme.typography
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    var isExpanded by remember { mutableStateOf(false) }
    val toggleExpanded = remember { { isExpanded = !isExpanded } }
    var showImageSourcePicker by remember { mutableStateOf(false) }
    val imagePickerManager = rememberImagePickerManager { bytes ->
        onStateChange(state.copy(imageBytes = bytes))
    }
    val selectedMeasureUnit = remember(state.measureUnit) {
        MeasureUnit.entries.find { it.name == state.measureUnit }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface.copy(alpha = 0.95f),
        modifier = Modifier.padding(
            horizontal = dimens.paddingSmall
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimens.paddingLarge)
        ) {
            item {
                SheetHeader(onBack = onBack)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    FormSection(
                        title = stringResource(Res.string.register_product_form_image_title),
                    ) {
                        ImagePickerCard(
                            imageBytes = state.imageBytes,
                            onClick = { showImageSourcePicker = true },
                            onClearImage = { onStateChange(state.copy(imageBytes = null)) }
                        )
                    }

                    FormSection(
                        title = stringResource(Res.string.register_product_form_basic_info_section_title),
                    ) {
                        ProductTextField(
                            value = state.name,
                            onValueChange = { onStateChange(state.copy(name = it)) },
                            label = stringResource(Res.string.register_product_form_basic_info_product_label),
                            placeholder = stringResource(Res.string.register_product_form_basic_info_product_placeholder),
                            isRequired = true,
                            maxCharacters = CoreConstants.Product.NAME_MAX_LENGTH,
                            errorMessage = state.nameError
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
                                    onStateChange(state.copy(netWeight = filtered))
                                },
                                label = stringResource(Res.string.register_product_form_basic_info_netweight_label),
                                placeholder = stringResource(Res.string.register_product_form_basic_info_netweight_placeholder),
                                keyboardType = KeyboardType.Decimal,
                                isRequired = true,
                                errorMessage = state.netWeightError
                            )

                            ProductDropdownField(
                                modifier = Modifier.weight(1f),
                                label = stringResource(Res.string.register_product_form_basic_info_unit_label),
                                selectedOption = selectedMeasureUnit,
                                placeholder = stringResource(Res.string.register_product_form_basic_info_unit_placeholder),
                                options = MeasureUnit.entries,
                                optionLabel = { it.toLabel() },
                                isRequired = true,
                                onSelected = { unit ->
                                    onStateChange(state.copy(measureUnit = unit.name))
                                },
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                        ) {
                            ProductDropdownField(
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.register_product_form_basic_info_product_category),
                                selectedOption = state.category.takeIf { it.isNotBlank() },
                                placeholder = stringResource(Res.string.register_product_form_basic_info_category_placeholder),
                                options = PantryMockData.categories,
                                optionLabel = { it },
                                isRequired = true,
                                onSelected = { selectedCategory ->
                                    onStateChange(state.copy(category = selectedCategory))
                                },
                            )
                        }
                    }

                    if (isExpanded) {
                        FormSection(
                            title = stringResource(Res.string.register_product_form_detail_info_section_title),
                        ) {

                            ProductTextField(
                                value = state.ean,
                                onValueChange = { input ->
                                    val numbersOnly = input.filter { it.isDigit() }.take(14)
                                    onStateChange(state.copy(ean = numbersOnly))
                                },
                                label = stringResource(Res.string.register_product_form_detail_info_ean_label),
                                placeholder = stringResource(Res.string.register_product_form_detail_info_ean_placeholder),
                                keyboardType = KeyboardType.Number,
                                maxCharacters = 14,
                                errorMessage = state.eanError,
                                trailingContent = {
                                    if (state.isCheckingEan) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp).padding(2.dp),
                                            strokeWidth = 2.dp,
                                            color = colors.primary
                                        )
                                    } else {
                                        IconButton(onClick = {}) {
                                            Icon(
                                                imageVector = Icons.Rounded.QrCodeScanner,
                                                contentDescription = stringResource(Res.string.register_product_form_detail_info_ean_icon_description),
                                                tint = if (state.eanError != null) colors.error else colors.onSecondaryContainer.copy(alpha = .72f),
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
                                maxCharacters = CoreConstants.Product.NOTES_MAX_LENGTH,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = toggleExpanded,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.onPrimaryContainer,
                            )
                        ) {
                            CustomText(
                                text = if (isExpanded) stringResource(Res.string.register_product_form_expand_button_see_less)
                                else stringResource(Res.string.register_product_form_expand_button_see_more),
                                color = colors.primary,
                                fontStyle = MinhaDespensaTheme.typography.bodySmall,
                                alignment = TextAlign.Center
                            )
                        }
                    }
                    Spacer(Modifier.height(dimens.paddingMedium))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.paddingSmall),
                        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            onClick = onCancel,
                            shape = RoundedCornerShape(dimens.cardCorner),
                            border = BorderStroke(
                                1.dp,
                                colors.onPrimaryContainer.copy(alpha = .45f),
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.onPrimaryContainer,
                            ),
                        ) {
                            CustomText(
                                text = stringResource(Res.string.register_product_form_button_cancel),
                                fontStyle = typography.bodySmall,
                                color = colors.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Button(
                            modifier = Modifier
                                .weight(1.75f)
                                .height(54.dp),
                            onClick = onSave,
                            enabled = state.isFormValid,
                            shape = RoundedCornerShape(dimens.cardCorner),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                                disabledContainerColor = colors.primary.copy(alpha = 0.4f),
                                disabledContentColor = colors.onPrimary.copy(alpha = 0.6f)
                            ),
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colors.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                CustomText(
                                    text = stringResource(Res.string.register_product_form_button_save),
                                    fontStyle = typography.bodySmall,
                                    color = colors.onPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showImageSourcePicker) {
            ImageSourcePickerDialog(
                onDismissRequest = { showImageSourcePicker = false },
                onCameraSelect = { imagePickerManager.launchCamera() },
                onGallerySelect = { imagePickerManager.launchGallery() }
            )
        }
    }
}

@Composable
private fun SheetHeader(
    onBack: () -> Unit,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimens.paddingSmall,
                end = dimens.paddingMedium,
                bottom = dimens.paddingLarge,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.register_product_form_back_button_desc),
                tint = colors.onPrimaryContainer.copy(alpha = .72f),
                modifier = Modifier.size(30.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimens.paddingSmall),
        ) {
            CustomText(
                text = stringResource(Res.string.register_product_form_header_title_top),
                color = colors.onPrimaryContainer,
                fontStyle = typography.displayLarge,
                fontWeight = FontWeight.Light,
            )
            CustomText(
                text = stringResource(Res.string.register_product_form_header_title_bottom),
                color = colors.onPrimaryContainer,
                fontStyle = typography.displayLarge,
                fontWeight = FontWeight.SemiBold,
            )
            CustomText(
                text = stringResource(Res.string.register_product_form_header_title_desc),
                color = colors.onPrimaryContainer.copy(alpha = .65f),
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Light,
            )
        }
    }
}


@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

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
                    vertical = dimens.paddingSmall,
                ),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CustomText(
                text = title,
                color = colors.onSecondaryContainer.copy(alpha = .75f),
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}

@Composable
private fun ProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isRequired: Boolean = false,
    maxCharacters: Int? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    trailingContent: (@Composable (() -> Unit))? = null,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val hasSupportingText = errorMessage != null || (maxCharacters != null && value.isNotEmpty())

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { input ->
            val sanitized = if (maxCharacters != null) input.take(maxCharacters) else input
            onValueChange(sanitized)
        },
        isError = errorMessage != null,
        textStyle = typography.bodySmall.copy(color = colors.onSecondaryContainer),
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomText(
                    text = label,
                    fontStyle = typography.bodySmall,
                    color = if (errorMessage != null) colors.error else colors.onSecondaryContainer.copy(alpha = .70f),
                    fontWeight = FontWeight.Light,
                )
                if (isRequired) {
                    CustomText(
                        text = " *",
                        fontStyle = typography.bodySmall,
                        color = colors.secondary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        placeholder = {
            CustomText(
                text = placeholder,
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = .42f),
                fontWeight = FontWeight.Light,
            )
        },

        supportingText = if (hasSupportingText) {
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (errorMessage != null) {
                        CustomText(
                            text = errorMessage,
                            fontStyle = typography.bodySmall,
                            color = colors.error,
                            fontWeight = FontWeight.Normal
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    if (maxCharacters != null) {
                        CustomText(
                            text = "${value.length}/$maxCharacters",
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        trailingIcon = trailingContent,
        shape = RoundedCornerShape(dimens.cardCorner * .55f),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onSecondaryContainer,
            unfocusedTextColor = colors.onSecondaryContainer,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = colors.primary,
            focusedBorderColor = colors.primary.copy(alpha = .75f),
            unfocusedBorderColor = colors.onSecondaryContainer.copy(alpha = .24f),
            errorBorderColor = colors.error,
            errorLabelColor = colors.error,
            errorSupportingTextColor = colors.error
        ),
    )
}

@Composable
fun <T> ProductDropdownField(
    label: String,
    selectedOption: T?,
    placeholder: String,
    options: List<T>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    isRequired: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens

    Box(modifier = modifier) {
        ProductTextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedOption?.let(optionLabel).orEmpty(),
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            isRequired = isRequired,
            trailingContent = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Abrir opções de $label",
                        tint = colors.onSecondaryContainer.copy(alpha = .72f),
                    )
                }
            },
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(dimens.cardCorner * 0.6f),
            containerColor = colors.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, colors.onSecondaryContainer.copy(alpha = 0.15f)),
            shadowElevation = 10.dp,
            modifier = Modifier.padding(vertical = 4.dp).widthIn(min = 120.dp, max = 320.dp)
        ) {
            options.forEach { option ->
                val labelText = optionLabel(option)
                val isSelected = option == selectedOption

                DropdownMenuItem(
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CustomText(
                                text = labelText,
                                fontStyle = MinhaDespensaTheme.typography.bodySmall,
                                color = if (isSelected) colors.primary else colors.onSecondaryContainer,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                alignment = TextAlign.Start
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MenuDefaults.itemColors(
                        textColor = colors.onSecondaryContainer
                    )
                )
            }
        }
    }
}

fun MeasureUnit.toLabel(): String = when (this) {
    MeasureUnit.KILOGRAM -> "Quilograma (kg)"
    MeasureUnit.GRAM -> "Grama (g)"
    MeasureUnit.LITER -> "Litro (L)"
    MeasureUnit.MILLILITER -> "Mililitro (ml)"
    MeasureUnit.UNIT -> "Unidade (un)"
    MeasureUnit.PACKAGE -> "Pacote"
}
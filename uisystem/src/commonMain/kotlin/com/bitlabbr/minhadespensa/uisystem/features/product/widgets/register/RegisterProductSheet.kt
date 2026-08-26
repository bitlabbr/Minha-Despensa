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
package com.bitlabbr.minhadespensa.uisystem.features.product.widgets.RegisterProduct

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
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
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.PantryMockData
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.defaultButtonColor

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
    val colors = MinhaDespensaTheme.color
    val dimens = MinhaDespensaTheme.dimens
    var isExpanded by remember { mutableStateOf(false) }

    val toggleExpanded = remember { { isExpanded = !isExpanded } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface.copy(alpha = 0.95f),
        modifier = Modifier.padding(
            horizontal = dimens.paddingSmall),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            item {
                ProductCreateHeader(onBack = onBack)

                PrimaryContainerGlassCard(
                    modifier = Modifier
                        .animateContentSize()
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
                                top = dimens.paddingMedium,
                                bottom = dimens.paddingLarge,
                            ),
                    ) {
                        FormSection(
                            title = "FOTO DO PRODUTO",
                        ) {
                            ImagePickerCard(onClick = {})
                        }

                        FormSection(
                            title = "INFORMAÇÕES BÁSICAS",
                        ) {
                            ProductTextField(
                                value = state.name,
                                onValueChange = {
                                    onStateChange(state.copy(name = it))
                                },
                                label = "Nome do produto",
                                placeholder = "Ex.: Arroz Parboilizado",
                            )

                            Spacer(Modifier.height(dimens.paddingSmall))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                            ) {
                                ProductDropdownField(
                                    modifier = Modifier.weight(1f),
                                    label = "Categoria",
                                    value = state.category,
                                    placeholder = "Selecione",
                                    options = PantryMockData.categories,
                                    onSelected = {
                                        onStateChange(state.copy(category = it))
                                    },
                                )

                                ProductDropdownField(
                                    modifier = Modifier.weight(1f),
                                    label = "Unidade",
                                    value = state.unit.name,
                                    placeholder = "Selecione",
                                    options = MeasureUnit.entries.map { it.name },
                                    onSelected = {
                                        onStateChange(state.copy(unit = MeasureUnit.valueOf(it)))
                                    },
                                )
                            }

                            Spacer(Modifier.height(dimens.paddingSmall))

                            ProductTextField(
                                value = state.ean,
                                onValueChange = {
                                    onStateChange(state.copy(ean = it))
                                },
                                label = "Código de barras",
                                placeholder = "Opcional",
                                keyboardType = KeyboardType.Number,
                                trailingContent = {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            imageVector = Icons.Rounded.QrCodeScanner,
                                            contentDescription = "Escanear código de barras",
                                            tint = MinhaDespensaTheme.color.onSecondaryContainer.copy(alpha = .72f),
                                        )
                                    }
                                },
                            )
                        }

                        if (isExpanded) {
                            FormSection(
                                title = "DETALHES ADICIONAIS",
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                                ) {
                                    ProductTextField(
                                        modifier = Modifier.weight(1f),
                                        value = state.price,
                                        onValueChange = {
                                            onStateChange(state.copy(price = it))
                                        },
                                        label = "Preço médio",
                                        placeholder = "R$ 0,00",
                                        keyboardType = KeyboardType.Decimal,
                                    )

                                    ProductTextField(
                                        modifier = Modifier.weight(1f),
                                        value = state.averageShelfLifeDays,
                                        onValueChange = {
                                            // Allow only digits for the days
                                            onStateChange(state.copy(averageShelfLifeDays = it.filter { c -> c.isDigit() }))
                                        },
                                        label = "Validade média",
                                        placeholder = "Dias",
                                        keyboardType = KeyboardType.Number,
                                    )
                                }

                                Spacer(Modifier.height(dimens.paddingSmall))

                                ProductTextField(
                                    value = state.notes,
                                    onValueChange = {
                                        onStateChange(state.copy(notes = it))
                                    },
                                    label = "Observações",
                                    placeholder = "Marca preferida, local de compra...",
                                    minLines = 3,
                                )
                            }

                            FormSection(
                                title = "CONFIGURAÇÕES",
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimens.paddingSmall),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        CustomText(
                                            text = "Lembrete de vencimento",
                                            fontStyle = typography.bodySmall,
                                            color = colors.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        CustomText(
                                            text = "Receba um aviso antes da validade",
                                            fontStyle = typography.bodySmall,
                                            color = colors.onSecondaryContainer.copy(alpha = .65f),
                                            fontWeight = FontWeight.Light,
                                        )
                                    }

                                    Switch(
                                        checked = state.expirationReminderEnabled,
                                        onCheckedChange = {
                                            onStateChange(
                                                state.copy(expirationReminderEnabled = it)
                                            )
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.onPrimary,
                                            checkedTrackColor = colors.primary,
                                            uncheckedThumbColor = colors.onPrimaryContainer.copy(alpha = .65f),
                                            uncheckedTrackColor = colors.primaryContainer.copy(alpha = .55f),
                                        ),
                                    )
                                }

                                if (state.expirationReminderEnabled) {
                                    Spacer(Modifier.height(dimens.paddingSmall))

                                    ProductDropdownField(
                                        label = "Avisar com antecedência",
                                        value = "${state.reminderDaysBefore} dias",
                                        placeholder = "Selecione",
                                        options = listOf(
                                            "1 dia",
                                            "2 dias",
                                            "3 dias",
                                            "5 dias",
                                            "7 dias",
                                            "15 dias",
                                        ),
                                        onSelected = { selected ->
                                            val days = selected.filter { it.isDigit() }.toIntOrNull() ?: 3
                                            onStateChange(
                                                state.copy(reminderDaysBefore = days.toString())
                                            )
                                        },
                                    )
                                }
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
                                    text = if (isExpanded) "Ver Menos" else "Adicionar Mais informação",
                                    color = colors.onPrimaryContainer,
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
                                    text = "Cancelar",
                                    fontStyle = typography.bodySmall,
                                    color = colors.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Button(
                                modifier = Modifier
                                    .weight(1.55f)
                                    .height(54.dp),
                                onClick = onSave,
                                enabled = !state.isSaving &&
                                        state.name.isNotBlank() &&
                                        state.category.isNotBlank() &&
                                        state.unit.name.isNotBlank(),
                                shape = RoundedCornerShape(dimens.cardCorner),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary,
                                    disabledContainerColor = colors.primary.copy(alpha = 0.5f),
                                    disabledContentColor = colors.onPrimary.copy(alpha = 0.7f)
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
                                        text = "Salvar produto",
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
        }
    }
}

@Composable
fun UnitSelector(
    selectedUnit: MeasureUnit,
    onUnitSelected: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Dropdown Box visual simulation
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedUnit.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Unit") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false // Disable input, but allow click via Box
        )

        // Transparent overlay to capture click
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MeasureUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductCreateHeader(
    onBack: () -> Unit,
) {
    val colors = MinhaDespensaTheme.color
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
                contentDescription = "Voltar",
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
                text = "REGISTRAR",
                color = colors.onPrimaryContainer,
                fontStyle = typography.displayLarge,
                fontWeight = FontWeight.Light,
            )
            CustomText(
                text = "PRODUTO",
                color = colors.onPrimaryContainer,
                fontStyle = typography.displayLarge,
                fontWeight = FontWeight.SemiBold,
            )
            CustomText(
                text = "Cadastre um novo item para sua despensa",
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
    val colors = MinhaDespensaTheme.color
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
        ) {
            Spacer(Modifier.height(dimens.paddingSmall))
            CustomText(
                text = title,
                color = colors.onSecondaryContainer.copy(alpha = .75f),
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(dimens.paddingMedium))

            content()
        }
    }
}

@Composable
private fun ImagePickerCard(
    onClick: () -> Unit,
) {
    val colors = MinhaDespensaTheme.color
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = null,
                tint = colors.onSecondaryContainer.copy(alpha = .72f),
                modifier = Modifier.size(38.dp),
            )

            CustomText(
                text = "Adicionar foto do produto",
                color = colors.onSecondaryContainer,
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )

            CustomText(
                text = "Tire uma foto ou escolha da galeria",
                color = colors.onSecondaryContainer.copy(alpha = .60f),
                fontStyle = typography.bodySmall,
                fontWeight = FontWeight.Light,
            )
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
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    trailingContent: (@Composable (() -> Unit))? = null,
) {
    val colors = MinhaDespensaTheme.color
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        textStyle = typography.bodySmall.copy(
            color = colors.onSecondaryContainer,
        ),
        label = {
            CustomText(
                text = label,
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = .70f),
                fontWeight = FontWeight.Light,
            )
        },
        placeholder = {
            CustomText(
                text = placeholder,
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = .42f),
                fontWeight = FontWeight.Light,
            )
        },
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
        ),
    )
}

@Composable
private fun ProductDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ProductTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            value = value,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            trailingContent = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Abrir opções de $label",
                        tint = MinhaDespensaTheme.color.onSecondaryContainer.copy(alpha = .72f),
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
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        CustomText(
                            text = option,
                            fontStyle = MinhaDespensaTheme.typography.bodySmall,
                            color = MinhaDespensaTheme.color.onBackground,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}
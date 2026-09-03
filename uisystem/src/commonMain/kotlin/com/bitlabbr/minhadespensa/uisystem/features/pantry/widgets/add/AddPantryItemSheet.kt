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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaExpandButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerSection
import com.bitlabbr.minhadespensa.uisystem.components.core.chip.MinhaDespensaFilterChip
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPantryItemSheet(
    isOpen: Boolean,
    formState: PantryItemFormState,
    onFormChange: (PantryItemFormState) -> Unit,
    onSearchEan: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToCatalogRegister: (String) -> Unit,
    initialOpenScanner: Boolean = false,
) {
    if (!isOpen) return

    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    var isExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(initialOpenScanner) }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = dimens.paddingMedium),
        ) {
            item {
                PrimaryContainerHeader(
                    textTop = "Adicionar à",
                    textBottom = "Despensa",
                    description = "Vincule um produto do catálogo ao seu estoque físico",
                    actionIcon = Icons.Rounded.Close,
                    onActionClick = onDismiss,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    // 1. Identificação do Produto no Catálogo
                    SecondaryContainerSection(title = "Produto do Catálogo") {
                        ProductTextField(
                            value = formState.ean,
                            onValueChange = { input ->
                                val numbers = input.filter { it.isDigit() }.take(14)
                                onFormChange(formState.copy(ean = numbers))
                                onSearchEan(numbers)
                            },
                            label = "Código de Barras (EAN)",
                            placeholder = "Escaneie ou digite o código",
                            keyboardType = KeyboardType.Number,
                            maxCharacters = 14,
                            trailingContent = {
                                if (formState.isSearchingCatalog) {
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
                                            contentDescription = "Abrir Scanner",
                                            tint = colors.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                        )

                        // Preview do Produto Encontrado
                        formState.selectedProduct?.let { product ->
                            SecondaryContainerGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimens.paddingSmall),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        MinhaDespensaText(
                                            text = product.name,
                                            fontStyle = typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        MinhaDespensaText(
                                            text = "${product.brand ?: "Genérico"} • ${product.category}",
                                            fontStyle = typography.bodySmall,
                                            color = colors.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                    MinhaDespensaText(
                                        text = "${product.netWeight} ${product.measureUnit.toAbbreviation()}",
                                        fontStyle = typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary,
                                    )
                                }
                            }
                        }

                        // Alerta se não existir no catálogo
                        if (formState.productNotFound && formState.ean.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                MinhaDespensaText(
                                    text = "Este produto ainda não existe no catálogo.",
                                    color = colors.error,
                                    fontStyle = typography.bodySmall,
                                )
                                MinhaDespensaSecondaryButton(
                                    text = "Cadastrar no Catálogo Primeiro",
                                    onClick = { onNavigateToCatalogRegister(formState.ean) },
                                )
                            }
                        }
                    }

                    // 2. Dados da Ocorrência na Despensa (Quantidade e Validade)
                    SecondaryContainerSection(title = "Estoque e Validade") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Controle Rápido de Quantidade
                            IconButton(
                                onClick = {
                                    val current = formState.quantity.replace(',', '.').toDoubleOrNull() ?: 1.0
                                    if (current > 1.0) {
                                        val next = current - 1.0
                                        onFormChange(
                                            formState.copy(
                                                quantity = if (next % 1.0 == 0.0) next.toLong()
                                                    .toString() else next.toString()
                                            )
                                        )
                                    }
                                },
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Diminuir")
                            }

                            ProductTextField(
                                modifier = Modifier.weight(1f),
                                value = formState.quantity,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                                    onFormChange(formState.copy(quantity = filtered))
                                },
                                label = "Quantidade *",
                                placeholder = "1",
                                keyboardType = KeyboardType.Decimal,
                                isRequired = true,
                                errorMessage = formState.quantityError?.asString(),
                            )

                            IconButton(
                                onClick = {
                                    val current = formState.quantity.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val next = current + 1.0
                                    onFormChange(
                                        formState.copy(
                                            quantity = if (next % 1.0 == 0.0) next.toLong()
                                                .toString() else next.toString()
                                        )
                                    )
                                },
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Aumentar")
                            }
                        }

                        // Atalhos Rápidos de Data de Validade
                        MinhaDespensaText(
                            text = "Atalhos de Validade",
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                        ) {
                            val timeZone = TimeZone.currentSystemDefault()
                            val now = Clock.System.now()

                            listOf(
                                "+7d" to 7,
                                "+15d" to 15,
                                "+30d" to 30,
                                "+60d" to 60,
                            ).forEach { (label, days) ->
                                val targetDate = now.plus(days, DateTimeUnit.DAY, timeZone)
                                val isSelected = formState.expirationDate != null &&
                                        Instant.fromEpochMilliseconds(formState.expirationDate)
                                            .toLocalDateTime(timeZone).date == targetDate.toLocalDateTime(timeZone).date

                                MinhaDespensaFilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onFormChange(formState.copy(expirationDate = targetDate.toEpochMilliseconds()))
                                    },
                                    label = label,
                                )
                            }
                        }
                    }

                    // 3. Seção Expansível (Lote e Preço)
                    if (isExpanded) {
                        SecondaryContainerSection(title = "Informações Adicionais") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                            ) {
                                ProductTextField(
                                    modifier = Modifier.weight(1f),
                                    value = formState.price,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }.take(8)
                                        onFormChange(formState.copy(price = filtered))
                                    },
                                    label = "Preço Pago (R$)",
                                    placeholder = "0,00",
                                    keyboardType = KeyboardType.Decimal,
                                )

                                ProductTextField(
                                    modifier = Modifier.weight(1f),
                                    value = formState.batchNumber,
                                    onValueChange = { onFormChange(formState.copy(batchNumber = it.take(20))) },
                                    label = "Lote",
                                    placeholder = "LOTE123",
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MinhaDespensaExpandButton(
                            isExpanded = isExpanded,
                            onClick = { isExpanded = !isExpanded },
                            collapsedText = "Mais detalhes (Preço/Lote)",
                            expandedText = "Menos detalhes",
                        )
                    }

                    // Ações de Rodapé
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.paddingSmall),
                        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    ) {
                        MinhaDespensaSecondaryButton(
                            text = "Cancelar",
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss,
                        )

                        MinhaDespensaPrimaryButton(
                            text = if (formState.isSaving) "Adicionando..." else "Adicionar à Despensa",
                            modifier = Modifier.weight(1.8f),
                            enabled = formState.isFormValid,
                            isLoading = formState.isSaving,
                            onClick = onSave,
                        )
                    }
                }
            }
        }

        if (showBarcodeScanner) {
            BarcodeScannerModal(
                onBarcodeScanned = { scannedCode ->
                    val numbers = scannedCode.filter { it.isDigit() }.take(14)
                    onFormChange(formState.copy(ean = numbers))
                    onSearchEan(numbers)
                    showBarcodeScanner = false
                },
                onDismissRequest = { showBarcodeScanner = false },
            )
        }
    }
}
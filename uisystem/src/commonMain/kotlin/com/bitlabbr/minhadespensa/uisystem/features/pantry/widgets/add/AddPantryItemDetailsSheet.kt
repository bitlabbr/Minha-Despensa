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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogProductUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPantryItemDetailsSheet(
    product: CatalogProductUiModel,
    onConfirm: (productId: String, quantity: Double, expirationDate: Long?, batchNumber: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    var quantityText by remember { mutableStateOf("1.0") }
    var expirationDate by remember { mutableStateOf<Long?>(null) }
    var batchNumber by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = dimens.paddingMedium),
        ) {
            item {
                PrimaryContainerHeader(
                    textTop = "Adicionar à",
                    textBottom = "Despensa",
                    description = "Defina a quantidade e a validade para o seu estoque",
                    actionIcon = Icons.Rounded.Close,
                    onActionClick = onDismiss,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    // Preview do Produto
                    SecondaryContainerSection(title = "Produto Selecionado") {
                        SecondaryContainerGlassCard(
                            modifier = Modifier.fillMaxWidth(),
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

                    // Quantidade e Validade
                    SecondaryContainerSection(title = "Estoque e Validade") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    val current = quantityText.replace(',', '.').toDoubleOrNull() ?: 1.0
                                    if (current > 1.0) {
                                        val next = current - 1.0
                                        quantityText = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                                    }
                                },
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Diminuir")
                            }

                            ProductTextField(
                                modifier = Modifier.weight(1f),
                                value = quantityText,
                                onValueChange = { input ->
                                    quantityText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                                },
                                label = "Quantidade *",
                                placeholder = "1",
                                keyboardType = KeyboardType.Decimal,
                                isRequired = true,
                            )

                            IconButton(
                                onClick = {
                                    val current = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val next = current + 1.0
                                    quantityText = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                                },
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Aumentar")
                            }
                        }

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
                                val isSelected = expirationDate != null &&
                                        Instant.fromEpochMilliseconds(expirationDate!!).toLocalDateTime(timeZone).date == targetDate.toLocalDateTime(timeZone).date

                                MinhaDespensaFilterChip(
                                    selected = isSelected,
                                    onClick = { expirationDate = targetDate.toEpochMilliseconds() },
                                    label = label,
                                )
                            }
                        }
                    }

                    // Seção Expansível
                    if (isExpanded) {
                        SecondaryContainerSection(title = "Informações Adicionais") {
                            ProductTextField(
                                value = batchNumber,
                                onValueChange = { batchNumber = it.take(20) },
                                label = "Lote",
                                placeholder = "LOTE123",
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MinhaDespensaExpandButton(
                            isExpanded = isExpanded,
                            onClick = { isExpanded = !isExpanded },
                            collapsedText = "Mais detalhes (Lote)",
                            expandedText = "Menos detalhes",
                        )
                    }

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
                            text = "Adicionar à Despensa",
                            modifier = Modifier.weight(1.8f),
                            isLoading = isSaving,
                            enabled = !isSaving && (quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0,
                            onClick = {
                                val qty = quantityText.replace(',', '.').toDoubleOrNull() ?: 1.0
                                isSaving = true
                                onConfirm(product.id, qty, expirationDate, batchNumber.takeIf { it.isNotBlank() })
                            },
                        )
                    }
                }
            }
        }
    }
}
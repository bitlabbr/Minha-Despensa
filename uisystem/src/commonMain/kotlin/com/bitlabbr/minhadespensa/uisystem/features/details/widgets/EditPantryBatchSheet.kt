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

package com.bitlabbr.minhadespensa.uisystem.features.details.widgets

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
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerSection
import com.bitlabbr.minhadespensa.uisystem.components.core.chip.MinhaDespensaFilterChip
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.features.details.model.PantryBatchUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import kotlinx.datetime.*
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPantryBatchSheet(
    batch: PantryBatchUiModel,
    measureUnit: MeasureUnit = MeasureUnit.UNIT,
    onConfirm: (batchId: String, quantity: Double, expirationDate: Long?, batchNumber: String?, unitPriceInCents: Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    var quantityText by remember(batch.id) {
        mutableStateOf(
            if (batch.quantity % 1.0 == 0.0) batch.quantity.toLong().toString() else batch.quantity.toString()
        )
    }

    var priceText by remember(batch.id) {
        mutableStateOf(batch.unitPriceInCents?.formatPrice() ?: "")
    }

    var expirationDate by remember(batch.id) {
        mutableStateOf(batch.expirationDate)
    }

    var batchNumber by remember(batch.id) {
        mutableStateOf(batch.batchNumber ?: "")
    }

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
                    textTop = "Editar",
                    textBottom = "Lote da Despensa",
                    description = "Ajuste a quantidade, validade, lote e preço unitário",
                    actionIcon = Icons.Rounded.Close,
                    onActionClick = onDismiss,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    // Seção de Quantidade e Preço
                    SecondaryContainerSection(title = "Estoque e Valor") {
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
                                label = "Quantidade (${measureUnit.toAbbreviation()})",
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

                        ProductTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = priceText,
                            onValueChange = { input ->
                                priceText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(8)
                            },
                            label = "Preço Unitário (R$)",
                            placeholder = "0,00",
                            keyboardType = KeyboardType.Decimal,
                        )
                    }

                    // Seção de Validade
                    SecondaryContainerSection(title = "Data de Validade") {
                        val timeZone = TimeZone.currentSystemDefault()
                        val now = Clock.System.now()

                        val currentFormatted = expirationDate?.let {
                            val dt = Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone)
                            val day = dt.dayOfMonth.toString().padStart(2, '0')
                            val month = dt.monthNumber.toString().padStart(2, '0')
                            "$day/$month/${dt.year}"
                        }

                        MinhaDespensaText(
                            text = if (currentFormatted != null) "Validade atual: $currentFormatted" else "Sem validade definida",
                            fontStyle = typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (currentFormatted != null) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                        ) {
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

                            if (expirationDate != null) {
                                MinhaDespensaFilterChip(
                                    selected = false,
                                    onClick = { expirationDate = null },
                                    label = "Limpar",
                                )
                            }
                        }
                    }

                    // Seção de Identificação do Lote
                    SecondaryContainerSection(title = "Identificação do Lote") {
                        ProductTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it.take(20) },
                            label = "Número do Lote",
                            placeholder = "Ex: LOTE123",
                        )
                    }

                    // Ações
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.paddingSmall, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                    ) {
                        MinhaDespensaSecondaryButton(
                            text = "Cancelar",
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss,
                        )

                        MinhaDespensaPrimaryButton(
                            text = "Salvar Lote",
                            modifier = Modifier.weight(1.8f),
                            isLoading = isSaving,
                            enabled = !isSaving && (quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0) >= 0.0,
                            onClick = {
                                val qty = quantityText.replace(',', '.').toDoubleOrNull() ?: 1.0
                                val price = priceText.replace(',', '.').toDoubleOrNull()?.let { (it * 100).roundToLong() }
                                isSaving = true
                                onConfirm(
                                    batch.id,
                                    qty,
                                    expirationDate,
                                    batchNumber.takeIf { it.isNotBlank() },
                                    price,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

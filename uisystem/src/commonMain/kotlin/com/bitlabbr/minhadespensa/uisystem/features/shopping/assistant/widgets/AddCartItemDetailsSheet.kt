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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCartItemDetailsSheet(
    product: CatalogProduct?,
    rawText: String?,
    initialQuantity: Double,
    initialPrice: Long?,
    onConfirm: (quantity: Double, price: Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    var quantityText by remember {
        mutableStateOf(
            if (initialQuantity % 1.0 == 0.0) initialQuantity.toLong().toString() else initialQuantity.toString()
        )
    }

    var priceText by remember {
        mutableStateOf(initialPrice?.formatPrice() ?: "")
    }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = product?.name ?: rawText ?: "Adicionar ao Carrinho",
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                product?.brand?.let {
                    Text(
                        text = "$it • ${product.category}",
                        style = typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
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

                    ProductTextField(
                        modifier = Modifier.weight(1.2f),
                        value = priceText,
                        onValueChange = { input ->
                            priceText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(8)
                        },
                        label = "Preço Unitário (R$)",
                        placeholder = "0,00",
                        keyboardType = KeyboardType.Decimal,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val qty = quantityText.replace(',', '.').toDoubleOrNull() ?: 1.0
                            val price = priceText.replace(',', '.').toLongOrNull()
                            onConfirm(qty, price)
                        },
                        enabled = (quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier.weight(1.5f),
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
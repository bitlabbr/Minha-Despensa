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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCartItemDetailsSheet(
    product: CatalogProduct?,
    rawText: String?,
    initialQuantity: Double,
    initialPrice: Long?,
    isReplacement: Boolean = false,
    onReplaceItem: (() -> Unit)? = null,
    onRemoveItem: (() -> Unit)? = null,
    onConfirm: (name: String?, quantity: Double, price: Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    val colors = getAppColors()

    var customNameText by remember(product?.id, rawText) {
        mutableStateOf(rawText ?: "")
    }

    var quantityText by remember(product?.id, rawText, initialQuantity) {
        mutableStateOf(
            if (initialQuantity % 1.0 == 0.0) initialQuantity.toLong().toString() else initialQuantity.toString()
        )
    }

    var priceText by remember(product?.id, rawText, initialPrice) {
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
                if (isReplacement) {
                    Text(
                        text = stringResource(Res.string.shopping_assistant_replace_confirm_title),
                        style = typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                }

                if (product != null) {
                    Text(
                        text = product.name,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    product.brand?.let {
                        Text(
                            text = "$it • ${product.category}",
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                } else {
                    Text(
                        text = if (rawText.isNullOrBlank()) {
                            stringResource(Res.string.shopping_assistant_add_text_item_title)
                        } else {
                            stringResource(Res.string.shopping_assistant_edit_text_item_title)
                        },
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    ProductTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = customNameText,
                        onValueChange = { customNameText = it },
                        label = stringResource(Res.string.shopping_assistant_item_name_label),
                        placeholder = stringResource(Res.string.shopping_assistant_item_name_placeholder),
                        maxCharacters = CoreConstants.Product.NAME_MAX_LENGTH,
                        isRequired = true,
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
                        label = "Quantidade",
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

                if (onReplaceItem != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onReplaceItem,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.shopping_assistant_replace_item_action))
                    }
                }

                if (onRemoveItem != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRemoveItem,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error,
                        ),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colors.error,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.shopping_assistant_remove_item_action),
                            color = colors.error,
                        )
                    }
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

                    val isNameValid = product != null || customNameText.isNotBlank()
                    val qtyValue = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val isQtyValid = qtyValue > 0.0

                    Button(
                        onClick = {
                            val price = priceText.replace(',', '.').toDoubleOrNull()?.let { (it * 100).roundToLong() }
                            val finalName = if (product != null) null else customNameText.trim()
                            onConfirm(finalName, qtyValue, price)
                        },
                        enabled = isNameValid && isQtyValid,
                        modifier = Modifier.weight(1.5f),
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
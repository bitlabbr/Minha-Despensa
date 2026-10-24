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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceReviewBottomSheet(
    invoice: Invoice,
    isImporting: Boolean = false,
    onToggleItem: (itemId: String, isSelected: Boolean) -> Unit,
    onConfirmImport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleAllItems: ((Boolean) -> Unit)? = null,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    val allSelected = invoice.items.isNotEmpty() && invoice.items.all { it.isSelected }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        header = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.paddingMedium)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.invoice_review_title),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer,
                        )

                        val formattedDate = formatEmissionDate(invoice.emissionDate)
                        val storeDetails = buildString {
                            append(invoice.storeName)
                            if (!formattedDate.isNullOrBlank()) {
                                append(" • ")
                                append(formattedDate)
                            }
                        }

                        MinhaDespensaText(
                            text = storeDetails,
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isImporting,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(Res.string.action_cancel),
                            tint = colors.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.invoice_review_total_selected,
                                invoice.totalSelectedItems,
                                invoice.items.size,
                            ),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        MinhaDespensaText(
                            text = "•",
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.4f),
                        )

                        MinhaDespensaText(
                            text = invoice.totalSelectedInCents.formatPrice(includeCurrencySymbol = true),
                            fontStyle = typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            maxLines = 1,
                        )
                    }

                    if (onToggleAllItems != null && invoice.items.isNotEmpty()) {
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentSize provides 0.dp
                        ) {
                            TextButton(
                                onClick = { onToggleAllItems(!allSelected) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                            ) {
                                MinhaDespensaText(
                                    text = stringResource(
                                        if (allSelected) Res.string.invoice_review_deselect_all
                                        else Res.string.invoice_review_select_all
                                    ),
                                    fontStyle = typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = colors.outline.copy(alpha = 0.12f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        },
        footer = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.paddingMedium)
                    .padding(top = 8.dp),
            ) {
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.invoice_review_action_import, invoice.totalSelectedItems),
                    onClick = onConfirmImport,
                    enabled = invoice.totalSelectedItems > 0 && !isImporting,
                    isLoading = isImporting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                horizontal = dimens.paddingMedium,
                vertical = dimens.paddingSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(invoice.items, key = { it.id }) { item ->
                InvoiceItemRow(
                    item = item,
                    onToggleSelect = { isSelected ->
                        onToggleItem(item.id, isSelected)
                    },
                )
            }
        }
    }
}

private fun formatEmissionDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val dt = raw.take(19)
        if (dt.contains("T")) {
            val parts = dt.split("T")
            val dateParts = parts[0].split("-")
            val timeParts = parts.getOrNull(1)?.take(5)
            if (dateParts.size == 3) {
                "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}" + if (timeParts != null) " $timeParts" else ""
            } else dt
        } else dt
    } catch (_: Exception) {
        raw
    }
}

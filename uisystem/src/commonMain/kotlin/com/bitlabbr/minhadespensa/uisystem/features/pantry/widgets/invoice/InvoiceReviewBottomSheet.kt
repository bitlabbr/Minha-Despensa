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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
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
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MinhaDespensaText(
                    text = stringResource(Res.string.invoice_review_title),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
                MinhaDespensaText(
                    text = stringResource(Res.string.invoice_review_subtitle),
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }

            // Store Info & Totals Summary Card
            SecondaryContainerGlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MinhaDespensaText(
                            text = invoice.storeName,
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSecondaryContainer,
                        )

                        invoice.emissionDate?.let { date ->
                            MinhaDespensaText(
                                text = date,
                                fontStyle = typography.bodySmall,
                                color = colors.onSecondaryContainer.copy(alpha = 0.6f),
                            )
                        }
                    }

                    HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.invoice_review_total_selected,
                                invoice.totalSelectedItems,
                                invoice.totalSelectedInCents.formatPrice(includeCurrencySymbol = true),
                            ),
                            fontStyle = typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                        )

                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.invoice_review_total_invoice,
                                invoice.totalInvoiceInCents.formatPrice(includeCurrencySymbol = true),
                            ),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Products list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
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

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.invoice_review_action_import, invoice.totalSelectedItems),
                    onClick = onConfirmImport,
                    enabled = invoice.totalSelectedItems > 0 && !isImporting,
                    isLoading = isImporting,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isImporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cardCorner * 0.5f)),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.pantry_add_option_cancel),
                        fontStyle = typography.button,
                    )
                }
            }
        }
    }
}

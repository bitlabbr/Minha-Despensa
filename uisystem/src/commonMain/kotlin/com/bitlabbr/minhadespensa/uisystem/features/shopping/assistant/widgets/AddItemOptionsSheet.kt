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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemOptionsSheet(
    onScanBarcode: () -> Unit,
    onSearchCatalog: () -> Unit,
    onAddTextItem: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens
    val colors = getAppColors()
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
                    text = stringResource(Res.string.shopping_assistant_add_item_title),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
                MinhaDespensaText(
                    text = stringResource(Res.string.shopping_assistant_add_item_subtitle),
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }

            // Option 1: Scan Barcode
            ItemContainerGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                    .clickable(onClick = onScanBarcode),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingMedium),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DocumentScanner,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_scan),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSecondaryContainer,
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_scan_desc),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                        )
                    }
                }
            }

            // Option 2: Search Catalog
            ItemContainerGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                    .clickable(onClick = onSearchCatalog),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingMedium),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_catalog),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSecondaryContainer,
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_catalog_desc),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                        )
                    }
                }
            }

            // Option 3: Free-Text Item
            ItemContainerGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
                    .clickable(onClick = onAddTextItem),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingMedium),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_text),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSecondaryContainer,
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_add_option_text_desc),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Cancelar",
                    style = typography.button,
                )
            }
        }
    }
}

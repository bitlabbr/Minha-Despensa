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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.CatalogProductPickerSheet as SharedCatalogProductPickerSheet
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogProductPickerSheet(
    products: List<CatalogProduct>,
    onProductSelected: (CatalogProduct) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(Res.string.shopping_assistant_replace_picker_title),
    searchPlaceholder: String = stringResource(Res.string.shopping_assistant_replace_picker_search_placeholder),
    emptyMessage: String = stringResource(Res.string.shopping_assistant_replace_picker_empty),
    onAddAsText: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    SharedCatalogProductPickerSheet(
        products = products,
        onProductSelected = onProductSelected,
        onDismiss = onDismiss,
        title = title,
        searchPlaceholder = searchPlaceholder,
        emptyMessage = emptyMessage,
        onAddAsText = onAddAsText,
        modifier = modifier,
    )
}

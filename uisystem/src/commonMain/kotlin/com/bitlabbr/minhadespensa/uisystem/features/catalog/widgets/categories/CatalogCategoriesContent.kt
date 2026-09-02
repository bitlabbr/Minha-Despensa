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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.components.core.chip.MinhaDespensaFilterChip
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.category_filter_all
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogCategoriesContent(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens
    val isAllSelected = selectedCategory == null

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = dimens.paddingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        item {
            MinhaDespensaFilterChip(
                selected = isAllSelected,
                onClick = { onCategorySelected(null) },
                label = stringResource(Res.string.category_filter_all),
            )
        }

        items(categories) { category ->
            MinhaDespensaFilterChip(
                selected = category.equals(selectedCategory, ignoreCase = true),
                onClick = { onCategorySelected(category) },
                label = category,
            )
        }
    }
}
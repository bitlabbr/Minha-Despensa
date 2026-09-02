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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.bitlabbr.minhadespensa.uisystem.components.core.search.MinhaDespensaSearchBar
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun PantrySearchBarContent(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<PantryItemUiModel>,
    onResultClick: (PantryItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.product_searchbar_widget_placeholder),
) {
    val dimens = MinhaDespensaTheme.dimens
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val itemsByName = remember(searchResults) { searchResults.associateBy { it.name } }
    val searchNames = remember(searchResults) { searchResults.map { it.name } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingSmall),
    ) {
        MinhaDespensaSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { submittedTerm ->
                val matchedItem = itemsByName[submittedTerm.trim()] ?: searchResults.firstOrNull()
                if (matchedItem != null) {
                    onResultClick(matchedItem)
                }
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            searchResults = searchNames,
            onResultClick = { clickedName ->
                itemsByName[clickedName]?.let { selectedItem ->
                    onResultClick(selectedItem)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            placeholder = placeholder,
            supportingContent = { itemName ->
                val item = itemsByName[itemName]
                val infoText = when {
                    item == null -> stringResource(Res.string.pantry_search_bar_no_info)
                    item.isExpired -> stringResource(Res.string.pantry_search_bar_expired_product_warn)
                    else -> "${stringResource(Res.string.pantry_search_bar_sock_quantity)}: ${item.quantity} ${item.measureUnit.toAbbreviation()}"
                }

                MinhaDespensaText(
                    text = infoText,
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = if (item?.isExpired == true) {
                        MinhaDespensaTheme.color.error
                    } else {
                        getAppColors().onSecondaryContainer.copy(alpha = 0.7f)
                    },
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = getAppColors().primary,
                )
            },
        )
    }
}
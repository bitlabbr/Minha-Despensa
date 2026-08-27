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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.searchbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.bitlabbr.minhadespensa.uisystem.components.core.search.MinhaDespensaSearchBar
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.product_searchbar_widget_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryItemSearchBarWidget(
    modifier: Modifier = Modifier,
    viewModel: PantryViewModel = koinViewModel(),
    placeholder: String = stringResource(Res.string.product_searchbar_widget_placeholder)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.pantryUiState.collectAsState()

    ProductSearchBarWidgetContent(
        modifier = modifier,
        query = searchQuery,
        onQueryChange = viewModel::onSearchQueryChanged,
        searchResults = uiState.searchResults,
        onResultClick = viewModel::onSearchResultSelected,
        placeholder = placeholder
    )
}

@Composable
fun ProductSearchBarWidgetContent(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<PantryItemUiModel>,
    onResultClick: (PantryItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar na despensa..."
) {
    val dimens = MinhaDespensaTheme.dimens
    val searchNames = searchResults.map { it.name }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingSmall)
    ) {
        MinhaDespensaSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { submittedTerm ->
                val matchedItem = searchResults.firstOrNull {
                    it.name.equals(submittedTerm.trim(), ignoreCase = true)
                } ?: searchResults.firstOrNull()

                if (matchedItem != null) {
                    onResultClick(matchedItem)
                }

                keyboardController?.hide()
                focusManager.clearFocus()
            },
            searchResults = searchNames,
            onResultClick = { clickedName ->
                val selectedItem = searchResults.firstOrNull { it.name == clickedName }
                if (selectedItem != null) {
                    onResultClick(selectedItem)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            placeholder = placeholder,
            supportingContent = { itemName ->
                val item = searchResults.firstOrNull { it.name == itemName }
                val infoText = when {
                    item == null -> "Sem informação"
                    item.isExpired -> "Atenção: Produto Vencido"
                    else -> "Estoque: ${item.quantity} ${item.measureUnit.name.lowercase()}"
                }

                MinhaDespensaText(
                    text = infoText,
                    fontStyle = MinhaDespensaTheme.typography.bodySmall,
                    color = if (item?.isExpired == true) {
                        MinhaDespensaTheme.color.error
                    } else {
                        getAppColors().onSecondaryContainer.copy(alpha = 0.7f)
                    }
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = getAppColors().primary
                )
            }
        )
    }
}
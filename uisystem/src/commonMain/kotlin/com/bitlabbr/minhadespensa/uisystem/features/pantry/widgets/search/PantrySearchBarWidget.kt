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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.product_searchbar_widget_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantrySearchBarWidget(
    modifier: Modifier = Modifier,
    viewModel: PantryViewModel = koinViewModel(),
    placeholder: String = stringResource(Res.string.product_searchbar_widget_placeholder),
    onItemSelected: (PantryItemUiModel) -> Unit = viewModel::onSearchResultSelected,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    PantrySearchBarContent(
        query = searchQuery,
        onQueryChange = viewModel::onSearchQueryChanged,
        searchResults = uiState.searchResults,
        onResultClick = onItemSelected,
        placeholder = placeholder,
        modifier = modifier,
    )
}
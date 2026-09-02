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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.components.core.card.CategorizedContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryCategoriesWidget(
    modifier: Modifier = Modifier,
    viewModel: PantryViewModel = koinViewModel(),
    onProductClick: (PantryItemUiModel) -> Unit = viewModel::onSearchResultSelected,
) {
    val uiState by viewModel.uiState.collectAsState()

    CategorizedContainerGlassCard(
        categories = uiState.filterState.availableCategories,
        selectedCategory = uiState.filterState.selectedCategory,
        onCategorySelected = viewModel::onCategorySelected,
        isLoading = uiState.listState.isLoading,
        isEmpty = uiState.listState.products.isEmpty(),
        isRootEmpty = uiState.listState.isCatalogEmpty,
        error = uiState.listState.error,
        modifier = modifier,
    ) {
        PantryProductGridContent(
            products = uiState.listState.products,
            onProductClick = onProductClick,
            onGetProductImage = viewModel::getProductImage,
        )
    }
}
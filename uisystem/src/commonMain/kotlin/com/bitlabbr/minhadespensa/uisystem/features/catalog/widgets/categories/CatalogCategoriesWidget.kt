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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.features.catalog.CatalogViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogCategoriesWidget(
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CatalogCategoriesContent(
        modifier = modifier,
        categories = uiState.filterState.availableCategories,
        selectedCategory = uiState.filterState.selectedCategory,
        onCategorySelected = viewModel::onCategorySelected,
    )
}
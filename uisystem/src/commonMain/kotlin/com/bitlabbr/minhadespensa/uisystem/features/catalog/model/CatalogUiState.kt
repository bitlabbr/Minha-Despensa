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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.model

import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.model.UiText

data class SearchSubState(
    val query: String = "",
    val searchResults: List<CatalogProductUiModel> = emptyList(),
    val isSearching: Boolean = false,
)

data class CategoryFilterSubState(
    val availableCategories: List<String> = emptyList(),
    val selectedCategory: String? = null,
)

data class ProductListSubState(
    val products: List<CatalogProductUiModel> = emptyList(),
    val isCatalogEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class CatalogUiState(
    val searchState: SearchSubState = SearchSubState(),
    val filterState: CategoryFilterSubState = CategoryFilterSubState(),
    val listState: ProductListSubState = ProductListSubState(),
    val formState: ProductFormState = ProductFormState(),
    val isFormOpen: Boolean = false,
    val userMessage: UiText? = null,
)
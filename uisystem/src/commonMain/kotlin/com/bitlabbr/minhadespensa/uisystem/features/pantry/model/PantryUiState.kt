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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.model

data class PantryFilterSubState(
    val availableCategories: List<String> = emptyList(),
    val selectedCategory: String? = null,
)

data class PantryListSubState(
    val products: List<PantryItemUiModel> = emptyList(),
    val isCatalogEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class PantryUiState(
    val filterState: PantryFilterSubState = PantryFilterSubState(),
    val listState: PantryListSubState = PantryListSubState(),
    val allActivePantryItems: List<PantryItemUiModel> = emptyList(),
    val expiringPantryItems: List<PantryItemUiModel> = emptyList(),
    val searchResults: List<PantryItemUiModel> = emptyList(),
    val selectedPantryItem: PantryItemUiModel? = null,
    val activeSubFlow: PantrySubFlow? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

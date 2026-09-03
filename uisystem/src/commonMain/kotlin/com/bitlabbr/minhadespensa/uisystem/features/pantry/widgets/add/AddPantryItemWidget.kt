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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddPantryItemWidget(
    modifier: Modifier = Modifier,
    viewModel: PantryViewModel = koinViewModel(),
    isCallToAction: Boolean = false,
    onNavigateToCatalogRegister: (String) -> Unit = {},
) {
    val isItemSheetOpen by viewModel.isItemSheetOpen.collectAsState()
    val itemFormState by viewModel.itemFormState.collectAsState()

    Box(modifier = modifier) {
        AddPantryItemWidgetContent(
            onClick = { viewModel.openAddPantryItemSheet(startWithScanner = false) },
            isCallToAction = isCallToAction,
        )

        AddPantryItemSheet(
            isOpen = isItemSheetOpen,
            formState = itemFormState,
            initialOpenScanner = viewModel.isStartWithScanner,
            onFormChange = viewModel::onItemFormChange,
            onSearchEan = viewModel::onEanScannedOrTyped,
            onSave = viewModel::savePantryItem,
            onDismiss = viewModel::closeAddPantryItemSheet,
            onNavigateToCatalogRegister = { ean ->
                viewModel.closeAddPantryItemSheet()
                onNavigateToCatalogRegister(ean)
            },
        )
    }
}
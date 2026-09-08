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

package com.bitlabbr.minhadespensa.uisystem.features.pantry


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.scanner.BarcodeScannerModal
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductBottomSheet
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantrySubFlow
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.AddPantryItemDetailsSheet
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.AddPantryItemWidget
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.categories.PantryCategoriesWidget
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.search.PantrySearchBarWidget
import minhadespensa.uisystem.generated.resources.Pantry
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.maine
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryScreen(
    bottomPadding: Dp = 0.dp,
    viewModel: PantryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {
            MinhaDespensaTopBar()
        },
    ) {
        PrimaryContainerHeader(
            textTop = stringResource(Res.string.maine),
            textBottom = stringResource(Res.string.Pantry),
        ) {}

        PantrySearchBarWidget(viewModel = viewModel)
        PantryCategoriesWidget(viewModel = viewModel)
        AddPantryItemWidget(viewModel = viewModel)
    }

    when (val subFlow = uiState.activeSubFlow) {
        is PantrySubFlow.BarcodeScanner -> {
            BarcodeScannerModal(
                onBarcodeScanned = { ean ->
                    viewModel.onBarcodeScanned(ean)
                },
                onDismissRequest = viewModel::onDismissSubFlow,
            )
        }

        is PantrySubFlow.CreateCatalogProduct -> {
            RegisterProductBottomSheet(
                isOpen = true,
                prefilledEan = subFlow.initialEan,
                availableCategories = uiState.filterState.availableCategories,
                onProductCreated = { createdProduct ->
                    viewModel.onProductCreatedFromCatalog(createdProduct)
                },
                onDismiss = viewModel::onDismissSubFlow,
            )
        }

        is PantrySubFlow.AddItemDetails -> {
            AddPantryItemDetailsSheet(
                product = subFlow.product,
                onConfirm = { productId, qty, expirationDate, batch ->
                    viewModel.onConfirmAddPantryItem(productId, qty, expirationDate, batch)
                },
                onDismiss = viewModel::onDismissSubFlow,
            )
        }

        null -> Unit
    }
}
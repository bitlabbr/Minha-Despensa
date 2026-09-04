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


import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
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
    onItemClick: (PantryItemUiModel) -> Unit = {},
    viewModel: PantryViewModel = koinViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {
            MinhaDespensaTopBar()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        PrimaryContainerHeader(
            textTop = stringResource(Res.string.maine),
            textBottom = stringResource(Res.string.Pantry)
        ) {}
        PantrySearchBarWidget(
            onItemSelected = { item ->
                viewModel.onSearchResultSelected(item)
                onItemClick(item)
            },
        )
        PantryCategoriesWidget(
            onProductClick = onItemClick,
        )
        AddPantryItemWidget(
            isCallToAction = true
        )
    }
}

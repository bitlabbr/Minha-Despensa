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

package com.bitlabbr.minhadespensa.uisystem.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.catalog.CatalogViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.PantryItemsWidget
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.searchbar.PantryItemSearchBarWidget
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductWidget
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Pantry
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.maine
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryScreen(
    bottomPadding: Dp = 0.dp,
    viewModel: CatalogViewModel = koinViewModel()
) {
    val pantryViewModel = koinViewModel<PantryViewModel>()
    val appColors = getAppColors()
    val appTypography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Scaffold(
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    MinhaDespensaText(
                        text = "",
                        fontStyle = appTypography.displayMedium,
                        color = appColors.onBackground
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPadding
                )
            ) {
                item {
                    PrimaryContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = dimens.paddingSmall, vertical = dimens.paddingSmall)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = dimens.paddingLarge,
                                    bottom = dimens.paddingLarge
                                ),
                            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall)
                        ) {
                            PrimaryContainerHeader(
                                textTop = stringResource(Res.string.maine),
                                textBottom = stringResource(Res.string.Pantry)
                            ) {}

                            //PantryItemSearchBarWidget(viewModel = pantryViewModel)

//                            PantryItemsWidget()
//
//                            RegisterProductWidget()
                            RegisterProductWidget(
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
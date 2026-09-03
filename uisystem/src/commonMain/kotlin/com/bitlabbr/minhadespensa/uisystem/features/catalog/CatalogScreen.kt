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

package com.bitlabbr.minhadespensa.uisystem.features.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogProductUiModel
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.categories.CatalogCategoriesWidget
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.RegisterProductWidget
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.search.CatalogSearchBarWidget
import com.bitlabbr.minhadespensa.uisystem.theme.AppDimens
import com.bitlabbr.minhadespensa.uisystem.theme.AppTypography
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogScreen(
    onProductClick: (CatalogProductUiModel) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    Scaffold(
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    MinhaDespensaText(
                        text = "",
                        fontStyle = typography.displayMedium,
                        color = colors.onBackground,
                    )
                },
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPadding,
                ),
            ) {
                item {
                    PrimaryContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = dimens.paddingSmall, vertical = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = dimens.paddingLarge,
                                    bottom = dimens.paddingLarge,
                                ),
                            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                        ) {
                            PrimaryContainerHeader(
                                textTop = stringResource(Res.string.product_catalog_title_top),
                                textBottom = stringResource(Res.string.product_catalog_title_bottom),
                            )

                            CatalogSearchBarWidget(
                                viewModel = viewModel,
                                onProductSelected = onProductClick,
                            )

                            CatalogCategoriesWidget(
                                onProductClick = onProductClick,
                            )

                            RegisterProductWidget(
                                viewModel = viewModel,
                                isCallToAction = true
                            )
                        }
                    }
                }
            }
        }
    }
}
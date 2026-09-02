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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbar
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.product.CatalogProductCard
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogProductUiModel
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.CatalogUiState
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

                            SecondaryContainerGlassCard(
                                modifier = Modifier.padding(horizontal = dimens.paddingSmall),
                                content = {
                                    CatalogCategoriesWidget(
                                        viewModel = viewModel
                                    )
                                    when {
                                        uiState.listState.isLoading -> {
                                            LoadingCard(colors)
                                        }

                                        uiState.listState.products.isEmpty() -> {
                                            if (uiState.listState.isCatalogEmpty) {
                                                EmptyCatalogCard(dimens, colors, typography)
                                            } else {
                                                ProductNotFoundCard(dimens, colors, typography)
                                            }
                                        }

                                        uiState.listState.error != null -> {
                                            ErrorCard(dimens, colors, typography, uiState.listState.error)
                                        }

                                        else -> {
                                            CatalogHorizontalGrid(uiState, dimens, viewModel, onProductClick)
                                        }
                                    }
                                },
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

@Composable
private fun LoadingCard(colors: ColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun CatalogHorizontalGrid(
    uiState: CatalogUiState,
    dimens: AppDimens,
    viewModel: CatalogViewModel,
    onProductClick: (CatalogProductUiModel) -> Unit
) {
    val itemsCount = uiState.listState.products.size
    val gridHeight = if (itemsCount > 4) 420.dp else 220.dp
    val rowCount = if (itemsCount > 4) 2 else 1
    LazyHorizontalGrid(
        rows = GridCells.Fixed(rowCount),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        contentPadding = PaddingValues(
            horizontal = dimens.paddingSmall,
            vertical = dimens.paddingSmall,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        items(
            items = uiState.listState.products,
            key = { it.id },
        ) { product ->
            val imageFlow = remember(product.id) {
                viewModel.getProductImage(product.id)
            }
            val imageBytes by imageFlow.collectAsState(initial = null)

            CatalogProductCard(
                product = product,
                imageBytes = imageBytes,
                onClick = { onProductClick(product) },
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Composable
private fun ErrorCard(
    dimens: AppDimens,
    colors: ColorScheme,
    typography: AppTypography,
    error: String?
) {
    Column(
        modifier = Modifier
            .padding(dimens.paddingMedium)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MinhaDespensaText(
            text = stringResource(Res.string.error_ocurred),
            color = colors.error,
            fontStyle = typography.bodySmall,
        )
        error?.let { err ->
            MinhaDespensaText(
                text = err,
                color = colors.onSurface.copy(alpha = 0.6f),
                fontStyle = typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProductNotFoundCard(
    dimens: AppDimens,
    colors: ColorScheme,
    typography: AppTypography
) {
    Column(
        modifier = Modifier
            .padding(dimens.paddingMedium)
            .fillMaxWidth()
            .height(80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MinhaDespensaText(
            text = stringResource(Res.string.product_catalog_item_not_found_in_category_search),
            color = colors.onSurface.copy(alpha = 0.8f),
            fontStyle = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            alignment = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyCatalogCard(
    dimens: AppDimens,
    colors: ColorScheme,
    typography: AppTypography
) {
    Column(
        modifier = Modifier
            .padding(dimens.paddingMedium)
            .fillMaxWidth()
            .height(80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MinhaDespensaText(
            text = stringResource(Res.string.product_catalog_item_not_found),
            color = colors.onSurface.copy(alpha = 0.8f),
            fontStyle = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            alignment = TextAlign.Center,
        )
    }
}
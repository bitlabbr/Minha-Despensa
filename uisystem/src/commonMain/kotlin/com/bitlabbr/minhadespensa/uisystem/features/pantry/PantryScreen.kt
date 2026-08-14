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


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.CustomTopBar
import com.bitlabbr.minhadespensa.uisystem.components.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductsListViewModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.PantryMockData
import com.bitlabbr.minhadespensa.uisystem.features.product.widgets.ProductWidget
import com.bitlabbr.minhadespensa.uisystem.features.product.widgets.RegisterProduct.RegisterProductWidget
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryScreen(bottomPadding: Dp) {
    val pantryViewModel = koinViewModel<PantryViewModel>()
    val productsListViewModel = koinViewModel<ProductsListViewModel>()
    val widgets = PantryMockData.widgets

    Scaffold(
        topBar = {
            CustomTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    CustomText(
                        text = "",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = MinhaDespensaTheme.color.onBackground
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPadding + 16.dp
                )
            ) {
                item {
                    PrimaryContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = MinhaDespensaTheme.dimens.paddingSmall, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = MinhaDespensaTheme.dimens.paddingLarge,
                                    bottom = MinhaDespensaTheme.dimens.paddingLarge
                                )
                        ) {
                            PrimaryContainerHeader {}
                            widgets.forEach { widget ->
                                when (widget) {
                                    is ProductWidget -> RegisterProductWidget(viewModel = pantryViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
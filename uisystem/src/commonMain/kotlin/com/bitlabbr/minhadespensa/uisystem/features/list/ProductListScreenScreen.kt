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

package com.bitlabbr.minhadespensa.uisystem.features.list


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.GlassCard
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import kotlinx.coroutines.launch

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ProductListScreen() {
    val viewModel = koinViewModel<ProductsListViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    CustomText(
                    text = "Minha Despensa",
                    color = MinhaDespensaTheme.color.onTertiary,
                    fontStyle = MinhaDespensaTheme.typography.displayMedium
                )},
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MinhaDespensaTheme.color.onSecondary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    tint = MinhaDespensaTheme.color.onTertiary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is ProductsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductsUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                }
                is ProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text("No products. Tap + to add.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.products) { product ->
                                val appDimens = MinhaDespensaTheme.dimens
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = appDimens.paddingSmall,
                                            end = appDimens.paddingSmall,
                                            top = appDimens.paddingSmall /2,
                                            bottom = appDimens.paddingSmall /2
                                        )
                                ) {
                                    Column() {
                                        ListItem(
                                            colors = ListItemDefaults.colors(
                                                containerColor = Color.Transparent,
                                            ),
                                            headlineContent = {
                                                CustomText(
                                                    text = product.name,
                                                    color = MinhaDespensaTheme.color.onTertiary,
                                                    fontStyle = MinhaDespensaTheme.typography.bodyLarge
                                                )},
                                            supportingContent = {
                                                CustomText(
                                                    text = "${product.amount} ${product.measureUnit}",
                                                    color = MinhaDespensaTheme.color.onTertiary,
                                                    fontStyle = MinhaDespensaTheme.typography.bodyLarge
                                                )
                                            },
                                            trailingContent = {
                                                IconButton(onClick = { viewModel.removeProduct(product.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove",
                                                        tint = MinhaDespensaTheme.color.onTertiary
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showBottomSheet) {
                AddProductSheet(
                    state = formState,
                    sheetState = sheetState,
                    onNameChange = viewModel::onNameChange,
                    onQuantityChange = viewModel::onQuantityChange,
                    onUnitChange = viewModel::onUnitChange,
                    onSaveClick = {
                        viewModel.saveProduct()
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showBottomSheet = false
                        }
                    },
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.resetForm()
                    }
                )
            }
        }
    }
}
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.GlassCard
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
                title = { Text("Minha Despensa") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Text(
                                            text = "Macarrão São João",
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "2.0 UNIDADES",
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                ListItem(
                                    headlineContent = { Text(product.name) },
                                    supportingContent = {
                                        Text("${product.amount} ${product.measureUnit}")
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removeProduct(product.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                                        }
                                    }
                                )
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
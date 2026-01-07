/*
 * Copyright (c) 2026 Willian Santos
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0
 * International License (CC BY-NC 4.0).
 *
 * You may use, copy, modify, and distribute this file for non-commercial
 * purposes only, provided that proper attribution is given.
 *
 * The copyright holder retains all commercial rights and may
 * license this work under different terms.
 *
 * License: https://creativecommons.org/licenses/by-nc/4.0/
 *
 */

package com.bill.minhadespensa.uisystem.features.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bill.minhadispensa.core.domain.model.Product
import com.bill.minhadispensa.core.domain.util.AppLogger
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named


@Composable
fun ProductsListScreen() {
    val viewModel = koinViewModel<ProductsListViewModel>()
    val products by viewModel.uiState.collectAsState()
    val logger: AppLogger = koinInject(named("ui_logger"))

    val TAG = "ProductsListScreen"

    LaunchedEffect(Unit) {
        logger.d(TAG, "LaunchedEffect")
    }

    Scaffold { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            item {
                Text("Minha Despensa", modifier = Modifier.padding(bottom = 16.dp))
            }

            items(products) { product ->
                ProductItem(product)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Card(modifier = Modifier.padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name)
            Text(text = "${product.amount} ${product.unitMeasure}")
        }
    }
}
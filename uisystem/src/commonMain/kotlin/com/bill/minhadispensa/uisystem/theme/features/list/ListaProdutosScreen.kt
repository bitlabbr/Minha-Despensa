package com.bill.minhadispensa.uisystem.theme.features.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bill.minhadispensa.core.domain.model.Product
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ProductsListScreen() {
    val viewModel = koinViewModel<ProductsListViewModel>()
    val products by viewModel.uiState.collectAsState()

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
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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.categories

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.list.MinhaDespensaHorizontalGrid
import com.bitlabbr.minhadespensa.uisystem.components.domain.pantry.PantryItemCard
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import kotlinx.coroutines.flow.Flow

@Composable
fun PantryProductGridContent(
    products: List<PantryItemUiModel>,
    onProductClick: (PantryItemUiModel) -> Unit,
    onGetProductImage: (String) -> Flow<ByteArray?>,
    modifier: Modifier = Modifier,
) {
    MinhaDespensaHorizontalGrid(
        items = products,
        key = { it.id },
        modifier = modifier,
    ) { product ->
        val imageFlow = remember(product.id) { onGetProductImage(product.id) }
        val imageBytes by imageFlow.collectAsState(initial = null)

        PantryItemCard(
            product = product,
            imageBytes = imageBytes,
            onClick = { onProductClick(product) },
            modifier = Modifier.width(160.dp),
        )
    }
}
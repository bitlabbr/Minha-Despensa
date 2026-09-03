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

package com.bitlabbr.minhadespensa.uisystem.components.core.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun <T> MinhaDespensaHorizontalGrid(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    thresholdForDoubleRow: Int = 4,
    itemContent: @Composable (T) -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    val isDoubleRow = items.size > thresholdForDoubleRow
    val gridHeight = if (isDoubleRow) 420.dp else 220.dp
    val rowCount = if (isDoubleRow) 2 else 1

    LazyHorizontalGrid(
        rows = GridCells.Fixed(rowCount),
        modifier = modifier
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
            items = items,
            key = key,
        ) { item ->
            itemContent(item)
        }
    }
}
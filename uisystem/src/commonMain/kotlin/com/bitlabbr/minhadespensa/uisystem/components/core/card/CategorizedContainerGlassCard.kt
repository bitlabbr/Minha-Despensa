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

package com.bitlabbr.minhadespensa.uisystem.components.core.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.chip.MinhaDespensaFilterChip
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.AppDimens
import com.bitlabbr.minhadespensa.uisystem.theme.AppTypography
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategorizedContainerGlassCard(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    isLoading: Boolean,
    isEmpty: Boolean,
    isRootEmpty: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    emptyMessage: String = stringResource(Res.string.product_catalog_item_not_found),
    notFoundMessage: String = stringResource(Res.string.product_catalog_item_not_found_in_category_search),
    content: @Composable () -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    val isAllSelected = selectedCategory == null

    SecondaryContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingSmall),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = dimens.paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            item {
                MinhaDespensaFilterChip(
                    selected = isAllSelected,
                    onClick = { onCategorySelected(null) },
                    label = stringResource(Res.string.category_filter_all),
                )
            }

            items(categories) { category ->
                MinhaDespensaFilterChip(
                    selected = category.equals(selectedCategory, ignoreCase = true),
                    onClick = { onCategorySelected(category) },
                    label = category,
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.paddingSmall))

        if (isRootEmpty){
            ProductNotFoundCard()
        } else {
            when {
                isLoading -> {
                    LoadingCard(getAppColors().primary)
                }

                isEmpty -> {
                    EmptyCatalogCard()
                }

                error != null -> {
                    ErrorCard(error = error)
                }

                else -> {
                    content()
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = color.copy(alpha = 0.5f))
    }
}

@Composable
private fun ErrorCard(
    dimens: AppDimens = MinhaDespensaTheme.dimens,
    colors: ColorScheme = MinhaDespensaTheme.color,
    typography: AppTypography = MinhaDespensaTheme.typography,
    error: String? = null
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
    dimens: AppDimens = MinhaDespensaTheme.dimens,
    colors: ColorScheme = MinhaDespensaTheme.color,
    typography: AppTypography = MinhaDespensaTheme.typography,
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
    dimens: AppDimens = MinhaDespensaTheme.dimens,
    colors: ColorScheme = MinhaDespensaTheme.color,
    typography: AppTypography = MinhaDespensaTheme.typography,
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
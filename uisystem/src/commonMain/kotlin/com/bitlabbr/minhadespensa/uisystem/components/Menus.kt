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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MinhaDespensaTheme.color.primaryContainer,
    contentColor: Color = MinhaDespensaTheme.color.onPrimaryContainer,
    leftContent: @Composable (() -> Unit)? = null,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        title = {
            centerContent()
        },
        navigationIcon = {
            if (leftContent != null) {
                leftContent()
            }
        },
        actions = {
            if (rightContent != null) {
                rightContent()
            }
        }
    )
}

data class BottomNavItem<T : Any>(
    val title: String,
    val icon: ImageVector,
    val route: T
)

@Composable
fun CustomIconButton(
    iconPainter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(50.dp),
    backgroundColor: Color = MinhaDespensaTheme.color.onBackground,
    iconTint: Color = Color.White,
    shape: Shape = RectangleShape
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = iconPainter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(iconTint),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProductSearchBar() {
    var query by rememberSaveable { mutableStateOf("") }
    val allItems = listOf(
        "Arroz integral",
        "Feijão carioca",
        "Leite desnatado",
        "Macarrão espaguete",
        "Azeite de oliva",
        "Açúcar refinado",
        "Café moído",
        "Farinha de trigo",
    )

    val searchResults = remember(query) {
        if (query.isBlank()) allItems
        else allItems.filter { it.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier.padding(horizontal = MinhaDespensaTheme.dimens.paddingSmall),
    ) {
        CustomizableSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { term -> /* ViewModel call */ },
            searchResults = searchResults,
            onResultClick = { item ->
                query = item
            },
            placeholder = "Buscar na despensa...",
            supportingContent = { item ->
                val info = mapOf(
                    "Arroz integral" to "Estoque: 2 kg",
                    "Feijão carioca" to "Estoque: 1 kg",
                    "Leite desnatado" to "Vence em 3 dias",
                    "Macarrão espaguete" to "Estoque: 500 g",
                    "Azeite de oliva" to "Última compra: R$ 18,90",
                    "Açúcar refinado" to "Estoque: 800 g",
                    "Café moído" to "Estoque: 250 g",
                    "Farinha de trigo" to "Estoque: 1 kg",
                )
                CustomText(
                    text = info[item] ?: "Sem informação",
                    fontStyle = MinhaDespensaTheme.typography.displayMedium,
                    color = MinhaDespensaTheme.color.onSecondaryContainer.copy(alpha = 0.7f)
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = MinhaDespensaTheme.color.primary
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val appColors = MinhaDespensaTheme.color
    val appTypography = MinhaDespensaTheme.typography
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner))
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = if (isDark) 0.1f else 0.5f),
                    shape = RoundedCornerShape(MinhaDespensaTheme.dimens.cardCorner)
                ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = appColors.primary,
                focusedTextColor = appColors.onSurface,
                unfocusedTextColor = appColors.onSurface
            ),
            textStyle = appTypography.displayMedium,
            singleLine = true,
            placeholder = {
                CustomText(
                    text = placeholder,
                    color = appColors.onSurface.copy(alpha = 0.5f),
                    fontStyle = appTypography.priceLabel
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = appColors.primary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar", tint = appColors.onSurface)
                    }
                }
            }
        )

        AnimatedVisibility(visible = query.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 8.dp)
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        CustomText(
                            text = "Nenhum item encontrado",
                            color = appColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(count = searchResults.size) { index ->
                        val resultText = searchResults[index]
                        ListItem(
                            headlineContent = {
                                CustomText(text = resultText, color = appColors.onSurface)
                            },
                            supportingContent = supportingContent?.let { { it(resultText) } },
                            leadingContent = leadingContent,
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    onResultClick(resultText)
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
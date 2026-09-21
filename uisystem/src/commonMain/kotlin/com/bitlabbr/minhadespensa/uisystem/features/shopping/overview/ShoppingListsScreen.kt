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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.shopping.overview.model.ShoppingListSummaryUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListsScreen(
    bottomPadding: Dp = 0.dp,
    onNavigateToQuickList: () -> Unit,
    onNavigateToListDetails: (listId: String) -> Unit,
    viewModel: ShoppingListsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = {MinhaDespensaTopBar()},
    ) {
        PrimaryContainerHeader(
            textTop = "Minhas",
            textBottom = "Listas de Compras",
            description = "Gerencie suas compras planejadas e anotações rápidas",
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            uiState.activeLists.isEmpty() && uiState.completedLists.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimens.paddingMedium),
                    colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = colors.primary.copy(alpha = 0.7f),
                        )
                        MinhaDespensaText(
                            text = "Nenhuma lista criada ainda",
                            fontStyle = typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        MinhaDespensaText(
                            text = "Toque em 'Nova Lista' para anotar itens rapidamente",
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.activeLists.forEach { item ->
                        ShoppingListCard(
                            list = item,
                            onClick = { onNavigateToListDetails(item.id) },
                            onDelete = { viewModel.deleteList(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingListSummaryUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (list.type == ShoppingListType.SCRATCHPAD) Icons.Rounded.EditNote else Icons.Rounded.ShoppingCart,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    MinhaDespensaText(
                        text = list.name,
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(4.dp))

                MinhaDespensaText(
                    text = "${list.totalItems} itens • Criada em ${list.formattedDate}",
                    fontStyle = typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f),
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Excluir Lista",
                    tint = colors.error.copy(alpha = 0.8f),
                )
            }
        }
    }
}
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.layout.MainScreenScaffold
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.features.shopping.overview.model.ShoppingListSummaryUiModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    bottomPadding: Dp = 0.dp,
    onNavigateToQuickList: () -> Unit,
    onNavigateToAssistant: (listId: String?) -> Unit,
    onNavigateToListDetails: (listId: String) -> Unit,
    onNavigateToPlannedList: () -> Unit,
    viewModel: ShoppingListsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    var showCreateListOptions by remember { mutableStateOf(false) }

    MainScreenScaffold(
        bottomPadding = bottomPadding,
        topBar = { MinhaDespensaTopBar() },
    ) {
        PrimaryContainerHeader(
            textTop = stringResource(Res.string.shopping_lists_header_title_top),
            textBottom = stringResource(Res.string.shopping_lists_header_title_bottom),
            description = stringResource(Res.string.shopping_lists_header_description),
        )

        MinhaDespensaPrimaryButton(
            text = stringResource(Res.string.shopping_lists_direct_shopping_button),
            onClick = { onNavigateToAssistant(null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingSmall),
            leadingIcon = Icons.Rounded.ShoppingCart,
        )

        MinhaDespensaSecondaryButton(
            text = stringResource(Res.string.shopping_lists_new_list_button),
            onClick = { showCreateListOptions = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.paddingSmall),
            leadingIcon = Icons.Rounded.Add,
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
                ItemContainerGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.paddingSmall, vertical = dimens.paddingMedium),
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
                            text = stringResource(Res.string.shopping_lists_empty_title),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_lists_empty_description),
                            fontStyle = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    uiState.activeLists.forEach { item ->
                        ShoppingListCard(
                            list = item,
                            onClick = { onNavigateToAssistant(item.id) },
                            onDelete = { viewModel.deleteList(item.id) },
                        )
                    }
                }
            }
        }

        if (showCreateListOptions) {
            ModalBottomSheet(onDismissRequest = { showCreateListOptions = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(dimens.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.shopping_lists_dialog_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )

                    MinhaDespensaSecondaryButton(
                        text = stringResource(Res.string.shopping_lists_dialog_quick_option),
                        onClick = {
                            showCreateListOptions = false
                            onNavigateToQuickList()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.EditNote,
                    )

                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.shopping_lists_dialog_planned_option),
                        onClick = {
                            showCreateListOptions = false
                            onNavigateToPlannedList()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.ShoppingCart,
                    )
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
    val dimens = MinhaDespensaTheme.dimens

    ItemContainerGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCorner * 0.6f))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingMedium),
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
                    text = stringResource(
                        Res.string.shopping_lists_items_count,
                        list.totalItems,
                        list.formattedDate,
                    ),
                    fontStyle = typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f),
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(Res.string.shopping_lists_delete_content_description),
                    tint = colors.error.copy(alpha = 0.8f),
                )
            }
        }
    }
}
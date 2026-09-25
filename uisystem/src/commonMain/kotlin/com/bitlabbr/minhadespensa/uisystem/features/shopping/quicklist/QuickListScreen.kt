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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.quicklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuickListScreen(
    onNavigateBack: () -> Unit,
    onListSaved: (listId: String) -> Unit,
    viewModel: QuickListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                leftContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.register_product_form_back_button_desc),
                            tint = colors.onPrimaryContainer,
                        )
                    }
                },
                centerContent = {
                    MinhaDespensaText(
                        text = stringResource(Res.string.quick_list_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = dimens.paddingSmall, vertical = dimens.paddingSmall)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            SecondaryContainerGlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    ProductTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = stringResource(Res.string.quick_list_name_label),
                        placeholder = stringResource(Res.string.quick_list_name_placeholder),
                    )

                    ProductTextField(
                        value = uiState.budgetInput,
                        onValueChange = viewModel::onBudgetChange,
                        label = stringResource(Res.string.planned_list_budget_label),
                        placeholder = stringResource(Res.string.planned_list_budget_placeholder),
                        keyboardType = KeyboardType.Decimal,
                    )

                    ProductTextField(
                        value = uiState.rawContent,
                        onValueChange = viewModel::onContentChange,
                        label = stringResource(Res.string.quick_list_content_label),
                        placeholder = stringResource(Res.string.quick_list_content_placeholder),
                        minLines = 8,
                        singleLine = false,
                        isRequired = true,
                        errorMessage = uiState.errorMessage,
                    )

                    MinhaDespensaText(
                        text = stringResource(Res.string.quick_list_content_supporting),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.6f),
                    )

                    Spacer(Modifier.height(dimens.paddingSmall))

                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.quick_list_create_button),
                        onClick = { viewModel.saveQuickList(onListSaved) },
                        enabled = uiState.canSave,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
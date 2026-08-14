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

package com.bitlabbr.minhadespensa.uisystem.features.product.widgets.RegisterProduct

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.financialGaugePrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductWidget(
    viewModel: PantryViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    val toggleExpanded = remember { { isExpanded = !isExpanded } }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formState by viewModel.productFormState.collectAsState()
    val scope = rememberCoroutineScope()
    val colors = MinhaDespensaTheme.color
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val onDismissSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
                viewModel.resetProductForm()
            }
        }
    }

    val onSaveSheet = {
        scope.launch {
            viewModel.saveProduct().join()
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SecondaryContainerGlassCard(
            modifier = Modifier
                .animateContentSize()
                .padding(
                    horizontal = dimens.paddingSmall,
                    vertical = dimens.paddingSmall
                ),
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.cardCorner))
                        .clickable(onClick = {showBottomSheet = true})
                        .padding(dimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        financialGaugePrimaryColor.copy(alpha = .80f),
                                        financialGaugePrimaryColor.copy(alpha = .20f),
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer.copy(alpha = .55f),
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    Spacer(Modifier.width(dimens.paddingMedium))

                    Column(modifier = Modifier.weight(1f)) {
                        CustomText(
                            text = "Registrar novo produto",
                            fontStyle = typography.bodyLarge,
                            color = colors.onSecondaryContainer.copy(alpha = .75f),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        CustomText(
                            text = "Adicione um novo item à sua despensa",
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = .60f),
                            fontWeight = FontWeight.Light,
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Registrar novo produto",
                        tint = colors.onSecondaryContainer.copy(alpha = .75f),
                    )
                }
            }
        )
        if (showBottomSheet) {
            RegisterProductSheet(
                state = formState,
                sheetState = sheetState,
                onBack = {onDismissSheet.invoke()},
                onCancel = {onDismissSheet.invoke()},
                onStateChange = viewModel::onProductFormChange,
                onSave = {onSaveSheet.invoke()},
                onDismiss = {onDismissSheet.invoke()}
            )
        }
    }
}
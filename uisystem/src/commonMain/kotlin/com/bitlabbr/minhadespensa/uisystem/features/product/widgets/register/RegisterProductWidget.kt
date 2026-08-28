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

package com.bitlabbr.minhadespensa.uisystem.features.product.widgets.register

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.product.ProductViewModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.actionGradient
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductWidget(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = koinViewModel(),
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formState by viewModel.productFormState.collectAsState()
    val scope = rememberCoroutineScope()

    val onDismissSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
                viewModel.resetProductForm()
            }
        }
    }

    val onSaveSheet: () -> Unit = {
        scope.launch {
            viewModel.saveProduct().join()
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    Box(modifier = modifier) {
        RegisterProductWidgetContent(
            onClick = { showBottomSheet = true }
        )

        if (showBottomSheet) {
            RegisterProductSheet(
                state = formState,
                sheetState = sheetState,
                onBack = onDismissSheet,
                onCancel = onDismissSheet,
                onStateChange = viewModel::onProductFormChange,
                onSave = onSaveSheet,
                onDismiss = onDismissSheet,
            )
        }
    }
}


@Composable
private fun RegisterProductWidgetContent(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    SecondaryContainerGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(
                horizontal = dimens.paddingSmall,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.cardCorner))
                .clickable(onClick = onClick)
                .padding(dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // circular Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        actionGradient(
                            primary = colors.primary,
                            secondary = colors.primary.copy(alpha = 0.1f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(Res.string.plus_icon_desc),
                    tint = colors.onSecondaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(dimens.paddingMedium))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.register_product_widget_title),
                    fontStyle = typography.bodyLarge,
                    color = colors.onSecondaryContainer.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                MinhaDespensaText(
                    text = stringResource(Res.string.register_product_widget_desc),
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Normal,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(Res.string.chevron_right_icon_desc),
                tint = colors.onSecondaryContainer.copy(alpha = 0.70f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
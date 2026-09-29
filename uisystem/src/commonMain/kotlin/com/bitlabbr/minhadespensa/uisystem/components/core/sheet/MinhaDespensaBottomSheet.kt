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

package com.bitlabbr.minhadespensa.uisystem.components.core.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinhaDespensaBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden
        }),
    containerColor: Color = MinhaDespensaBottomSheetDefaults.containerColor(),
    shape: Shape = MinhaDespensaBottomSheetDefaults.shape(),
    dragHandle: @Composable (() -> Unit)? = {
        BottomSheetDefaults.DragHandle(
            color = MinhaDespensaBottomSheetDefaults.dragHandleColor()
        )
    },
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = containerColor,
        shape = shape,
        dragHandle = dragHandle,
        sheetMaxWidth = Dp.Unspecified,
        modifier = modifier
            .padding(horizontal = dimens.paddingSmall)
            .imePadding(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = dimens.paddingMedium)
        ) {
            header?.invoke()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                content = content,
            )

            footer?.invoke()
        }
    }
}
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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.CallToActionGlassCard
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryViewModel
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.action_scan_barcode
import minhadespensa.uisystem.generated.resources.action_search_catalog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddPantryItemWidget(
    modifier: Modifier = Modifier,
    viewModel: PantryViewModel = koinViewModel(),
) {
    AddPantryItemContent(
        onClickScan = viewModel::onStartScanFlow,
        onClickManual = { viewModel.onStartManualRegisterFlow() },
        modifier = modifier,
    )
}

@Composable
fun AddPantryItemContent(
    onClickScan: () -> Unit,
    onClickManual: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = MinhaDespensaTheme.dimens

    CallToActionGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.paddingSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {

            MinhaDespensaPrimaryButton(
                text = stringResource(Res.string.action_scan_barcode),
                modifier = Modifier.weight(1.4f),
                onClick = onClickScan,
            )

            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.action_search_catalog),
                modifier = Modifier.weight(1f),
                onClick = onClickManual,
            )
        }
    }
}
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

package com.bitlabbr.minhadespensa.uisystem.features.home.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.input.MinhaDespensaTextField
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.home_financial_dialog_cancel
import minhadespensa.uisystem.generated.resources.home_financial_dialog_desc
import minhadespensa.uisystem.generated.resources.home_financial_dialog_input_label
import minhadespensa.uisystem.generated.resources.home_financial_dialog_input_placeholder
import minhadespensa.uisystem.generated.resources.home_financial_dialog_save
import minhadespensa.uisystem.generated.resources.home_financial_dialog_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@Composable
fun SetMonthlyBudgetDialog(
    currentBudgetInCents: Long?,
    onConfirm: (budgetInCents: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    var budgetText by remember(currentBudgetInCents) {
        mutableStateOf(currentBudgetInCents?.formatPrice() ?: "")
    }

    val parsedCents = remember(budgetText) {
        budgetText.trim()
            .replace(".", "")
            .replace(",", ".")
            .toDoubleOrNull()
            ?.let { (it * 100).roundToLong() }
    }

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = stringResource(Res.string.home_financial_dialog_title),
        description = stringResource(Res.string.home_financial_dialog_desc),
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.home_financial_dialog_cancel),
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    onClick = onDismiss,
                )
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.home_financial_dialog_save),
                    modifier = Modifier.weight(1.3f),
                    isLoading = isSaving,
                    enabled = !isSaving && parsedCents != null && parsedCents >= 0L,
                    onClick = {
                        parsedCents?.let(onConfirm)
                    },
                )
            }
        },
    ) {
        MinhaDespensaTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { MinhaDespensaText(stringResource(Res.string.home_financial_dialog_input_label)) },
            placeholder = { MinhaDespensaText(stringResource(Res.string.home_financial_dialog_input_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
    }
}

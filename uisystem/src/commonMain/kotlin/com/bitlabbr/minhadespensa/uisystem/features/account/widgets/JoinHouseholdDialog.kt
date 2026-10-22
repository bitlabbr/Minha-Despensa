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

package com.bitlabbr.minhadespensa.uisystem.features.account.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.account_logout_dialog_cancel
import minhadespensa.uisystem.generated.resources.household_dialog_join_confirm
import minhadespensa.uisystem.generated.resources.household_dialog_join_desc
import minhadespensa.uisystem.generated.resources.household_dialog_join_input_label
import minhadespensa.uisystem.generated.resources.household_dialog_join_input_placeholder
import minhadespensa.uisystem.generated.resources.household_dialog_join_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinHouseholdDialog(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    val dimens = MinhaDespensaTheme.dimens

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.household_dialog_join_title),
        description = stringResource(Res.string.household_dialog_join_desc),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            ProductTextField(
                value = inviteCode,
                onValueChange = onInviteCodeChange,
                label = stringResource(Res.string.household_dialog_join_input_label),
                placeholder = stringResource(Res.string.household_dialog_join_input_placeholder),
                maxCharacters = CoreConstants.Household.INVITE_CODE_LENGTH,
                isRequired = true,
                singleLine = true,
            )

            Spacer(Modifier.height(dimens.paddingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.account_logout_dialog_cancel),
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                )

                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.household_dialog_join_confirm),
                    onClick = onConfirm,
                    enabled = inviteCode.trim().length == CoreConstants.Household.INVITE_CODE_LENGTH && !isLoading,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.app_update_available_title
import minhadespensa.uisystem.generated.resources.app_update_button_check
import minhadespensa.uisystem.generated.resources.app_update_button_checking
import minhadespensa.uisystem.generated.resources.app_update_button_download_install
import minhadespensa.uisystem.generated.resources.app_update_button_downloading
import minhadespensa.uisystem.generated.resources.app_update_changelog_title
import minhadespensa.uisystem.generated.resources.app_update_current_version_label
import minhadespensa.uisystem.generated.resources.app_update_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppUpdateSection(
    uiState: HouseholdAccountUiState,
    onCheckUpdate: () -> Unit,
    onDownloadAndInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isSelfUpdateSupported) return

    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    SecondaryContainerGlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.SystemUpdate,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
                MinhaDespensaText(
                    text = stringResource(Res.string.app_update_title),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
            }

            MinhaDespensaText(
                text = stringResource(
                    Res.string.app_update_current_version_label,
                    uiState.appVersionName.ifBlank { "N/A" }
                ),
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = 0.8f),
            )

            val availableUpdate = uiState.availableUpdate
            if (availableUpdate != null && availableUpdate.isUpdateAvailable) {
                ItemContainerGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(
                                Res.string.app_update_available_title,
                                availableUpdate.versionName
                            ),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                        )

                        if (availableUpdate.releaseNotes.isNotBlank()) {
                            MinhaDespensaText(
                                text = stringResource(Res.string.app_update_changelog_title),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = colors.onPrimaryContainer,
                            )
                            MinhaDespensaText(
                                text = availableUpdate.releaseNotes,
                                fontStyle = typography.bodySmall,
                                color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                        }

                        if (uiState.isDownloadingUpdate) {
                            Spacer(Modifier.height(4.dp))
                            val progressPercent = (uiState.downloadProgress * 100).toInt()
                            LinearProgressIndicator(
                                progress = { uiState.downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        color = colors.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(3.dp),
                                    ),
                                color = colors.primary,
                            )
                            MinhaDespensaText(
                                text = stringResource(
                                    Res.string.app_update_button_downloading,
                                    progressPercent
                                ),
                                fontStyle = typography.bodySmall,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        } else {
                            MinhaDespensaPrimaryButton(
                                text = stringResource(Res.string.app_update_button_download_install),
                                onClick = onDownloadAndInstall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.isCheckingUpdate) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary,
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.app_update_button_checking),
                                fontStyle = typography.bodySmall,
                                color = colors.onSecondaryContainer,
                            )
                        }
                    } else {
                        MinhaDespensaSecondaryButton(
                            text = stringResource(Res.string.app_update_button_check),
                            onClick = onCheckUpdate,
                        )
                    }
                }
            }
        }
    }
}

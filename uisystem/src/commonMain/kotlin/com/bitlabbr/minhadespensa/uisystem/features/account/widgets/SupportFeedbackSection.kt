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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.theme.getAppStatusColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.support_button_export_logs
import minhadespensa.uisystem.generated.resources.support_button_report
import minhadespensa.uisystem.generated.resources.support_crash_banner_action
import minhadespensa.uisystem.generated.resources.support_crash_banner_desc
import minhadespensa.uisystem.generated.resources.support_crash_banner_dismiss
import minhadespensa.uisystem.generated.resources.support_crash_banner_title
import minhadespensa.uisystem.generated.resources.support_section_desc
import minhadespensa.uisystem.generated.resources.support_section_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupportFeedbackSection(
    uiState: HouseholdAccountUiState,
    onOpenReportDialog: (SupportCategory) -> Unit,
    onExportLogs: () -> Unit,
    onDismissPendingCrash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val statusColors = getAppStatusColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        // Banner de Alerta Pós-Crash (se detectado da sessão anterior)
        if (!uiState.pendingCrashStackTrace.isNullOrBlank()) {
            SecondaryContainerGlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.paddingSmall),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = statusColors.warning,
                            modifier = Modifier.size(24.dp),
                        )
                        MinhaDespensaText(
                            text = stringResource(Res.string.support_crash_banner_title),
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColors.warning,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MinhaDespensaText(
                        text = stringResource(Res.string.support_crash_banner_desc),
                        fontStyle = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MinhaDespensaPrimaryButton(
                            text = stringResource(Res.string.support_crash_banner_action),
                            onClick = { onOpenReportDialog(SupportCategory.CRASH) },
                            modifier = Modifier.weight(1f),
                        )

                        MinhaDespensaSecondaryButton(
                            text = stringResource(Res.string.support_crash_banner_dismiss),
                            onClick = onDismissPendingCrash,
                        )
                    }
                }
            }
        }

        // Card Principal de Suporte, Bugs & Feedback
        SecondaryContainerGlassCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.paddingSmall),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    MinhaDespensaText(
                        text = stringResource(Res.string.support_section_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                MinhaDespensaText(
                    text = stringResource(Res.string.support_section_desc),
                    fontStyle = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.support_button_report),
                        onClick = { onOpenReportDialog(SupportCategory.BUG) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    MinhaDespensaSecondaryButton(
                        text = stringResource(Res.string.support_button_export_logs),
                        onClick = onExportLogs,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSharingLogs,
                    )
                }
            }
        }
    }
}

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.input.MinhaDespensaTextField
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.theme.getAppStatusColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.support_dialog_cancel_button
import minhadespensa.uisystem.generated.resources.support_dialog_category_bug
import minhadespensa.uisystem.generated.resources.support_dialog_category_crash
import minhadespensa.uisystem.generated.resources.support_dialog_category_label
import minhadespensa.uisystem.generated.resources.support_dialog_category_question
import minhadespensa.uisystem.generated.resources.support_dialog_category_suggestion
import minhadespensa.uisystem.generated.resources.support_dialog_contact_label
import minhadespensa.uisystem.generated.resources.support_dialog_contact_placeholder
import minhadespensa.uisystem.generated.resources.support_dialog_desc_label
import minhadespensa.uisystem.generated.resources.support_dialog_desc_placeholder
import minhadespensa.uisystem.generated.resources.support_dialog_include_logs
import minhadespensa.uisystem.generated.resources.support_dialog_include_logs_privacy
import minhadespensa.uisystem.generated.resources.support_dialog_submit_button
import minhadespensa.uisystem.generated.resources.support_dialog_submitting
import minhadespensa.uisystem.generated.resources.support_dialog_title
import minhadespensa.uisystem.generated.resources.support_dialog_title_label
import minhadespensa.uisystem.generated.resources.support_dialog_title_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun BugReportDialog(
    uiState: HouseholdAccountUiState,
    onCategoryChange: (SupportCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onIncludeLogsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = getAppColors()
    val statusColors = getAppStatusColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.support_dialog_title),
        content = {
            val receipt = uiState.lastReportReceipt
            if (receipt != null) {
                // Visualização de Recibo / Protocolo Emitido com Sucesso
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = statusColors.success,
                        modifier = Modifier.size(48.dp),
                    )

                    MinhaDespensaText(
                        text = "Relato Registrado com Sucesso!",
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColors.success,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.1f))
                            .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            MinhaDespensaText(
                                text = "PROTOCOLO DE ATENDIMENTO",
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            MinhaDespensaText(
                                text = "#${receipt.protocol}",
                                fontStyle = typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary,
                            )
                        }
                    }

                    MinhaDespensaText(
                        text = "Guarde o número do protocolo acima para acompanhamento. Nossa equipe irá analisar os diagnósticos.",
                        fontStyle = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            } else {
                // Formulário de Submissão
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Seleção de Categoria
                    MinhaDespensaText(
                        text = stringResource(Res.string.support_dialog_category_label),
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CategoryChip(
                            label = stringResource(Res.string.support_dialog_category_bug),
                            isSelected = uiState.bugReportCategory == SupportCategory.BUG,
                            onClick = { onCategoryChange(SupportCategory.BUG) },
                            modifier = Modifier.weight(1f),
                        )
                        CategoryChip(
                            label = stringResource(Res.string.support_dialog_category_suggestion),
                            isSelected = uiState.bugReportCategory == SupportCategory.SUGGESTION,
                            onClick = { onCategoryChange(SupportCategory.SUGGESTION) },
                            modifier = Modifier.weight(1f),
                        )
                        CategoryChip(
                            label = stringResource(Res.string.support_dialog_category_question),
                            isSelected = uiState.bugReportCategory == SupportCategory.QUESTION,
                            onClick = { onCategoryChange(SupportCategory.QUESTION) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Se for CRASH vindo de alerta pós-crash
                    if (uiState.bugReportCategory == SupportCategory.CRASH) {
                        CategoryChip(
                            label = stringResource(Res.string.support_dialog_category_crash),
                            isSelected = true,
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Título
                    MinhaDespensaTextField(
                        value = uiState.bugReportTitle,
                        onValueChange = onTitleChange,
                        label = { MinhaDespensaText(stringResource(Res.string.support_dialog_title_label)) },
                        placeholder = { MinhaDespensaText(stringResource(Res.string.support_dialog_title_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Descrição
                    MinhaDespensaTextField(
                        value = uiState.bugReportDescription,
                        onValueChange = onDescriptionChange,
                        label = { MinhaDespensaText(stringResource(Res.string.support_dialog_desc_label)) },
                        placeholder = { MinhaDespensaText(stringResource(Res.string.support_dialog_desc_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                    )

                    // Contato
                    MinhaDespensaTextField(
                        value = uiState.bugReportContact,
                        onValueChange = onContactChange,
                        label = { MinhaDespensaText(stringResource(Res.string.support_dialog_contact_label)) },
                        placeholder = { MinhaDespensaText(stringResource(Res.string.support_dialog_contact_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Checkbox de Logs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onIncludeLogsChange(!uiState.bugReportIncludeLogs) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = uiState.bugReportIncludeLogs,
                            onCheckedChange = onIncludeLogsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primary,
                                uncheckedColor = colors.onSurfaceVariant,
                            ),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            MinhaDespensaText(
                                text = stringResource(Res.string.support_dialog_include_logs),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.support_dialog_include_logs_privacy),
                                fontStyle = typography.bodySmall.copy(fontSize = typography.bodySmall.fontSize * 0.85f),
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            if (uiState.lastReportReceipt != null) {
                MinhaDespensaPrimaryButton(
                    text = "Concluir",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                val canSubmit = uiState.bugReportTitle.trim().length >= 3 &&
                    uiState.bugReportDescription.trim().length >= 5 &&
                    !uiState.isSubmittingBugReport

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    MinhaDespensaSecondaryButton(
                        text = stringResource(Res.string.support_dialog_cancel_button),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSubmittingBugReport,
                    )

                    MinhaDespensaPrimaryButton(
                        text = if (uiState.isSubmittingBugReport) {
                            stringResource(Res.string.support_dialog_submitting)
                        } else {
                            stringResource(Res.string.support_dialog_submit_button)
                        },
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit,
                    )
                }
            }
        },
    )
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.5f)
    val borderColor = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        MinhaDespensaText(
            text = label,
            fontStyle = typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurface,
        )
    }
}

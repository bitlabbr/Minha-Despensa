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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.gauge.InverseAnchoredGauge
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import kotlin.math.abs
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingFinancialDashboard(
    cartValueInCents: Long,
    budgetInCents: Long?,
    remainingBudgetInCents: Long?,
    budgetProgress: Float?,
    isOverBudget: Boolean,
    checkedCount: Int,
    totalCount: Int,
    isCompleted: Boolean,
    onEditBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    SecondaryContainerGlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. No Carrinho
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.shopping_assistant_dashboard_cart),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(2.dp))
                    MinhaDespensaText(
                        text = cartValueInCents.formatPrice(includeCurrencySymbol = true),
                        fontStyle = typography.priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) colors.error else colors.onSecondaryContainer,
                    )
                    Spacer(Modifier.height(1.dp))
                    MinhaDespensaText(
                        text = stringResource(
                            Res.string.shopping_assistant_dashboard_items_count,
                            checkedCount,
                            totalCount,
                        ),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer.copy(alpha = 0.5f),
                    )
                }

                // Divisor Vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(colors.onSecondaryContainer.copy(alpha = 0.12f)),
                )

                // 2. Teto (Orçamento) - Clicável
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isCompleted, onClick = onEditBudget)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_dashboard_budget),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium,
                        )
                        if (!isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(Res.string.shopping_assistant_edit_budget_dialog_title),
                                tint = colors.primary.copy(alpha = 0.75f),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    MinhaDespensaText(
                        text = if (budgetInCents != null) {
                            budgetInCents.formatPrice(includeCurrencySymbol = true)
                        } else {
                            stringResource(Res.string.shopping_assistant_dashboard_set_budget)
                        },
                        fontStyle = typography.priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                    Spacer(Modifier.height(1.dp))
                    MinhaDespensaText(
                        text = if (budgetInCents != null && !isCompleted) {
                            stringResource(Res.string.shopping_assistant_dashboard_edit_hint)
                        } else {
                            stringResource(Res.string.shopping_assistant_dashboard_no_limit)
                        },
                        fontStyle = typography.bodySmall,
                        color = if (budgetInCents != null && !isCompleted) colors.primary.copy(alpha = 0.7f) else colors.onSecondaryContainer.copy(alpha = 0.5f),
                    )
                }

                // Divisor Vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(colors.onSecondaryContainer.copy(alpha = 0.12f)),
                )

                // 3. Saldo
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    MinhaDespensaText(
                        text = if (isOverBudget) {
                            stringResource(Res.string.shopping_assistant_dashboard_exceeded)
                        } else {
                            stringResource(Res.string.shopping_assistant_dashboard_balance)
                        },
                        fontStyle = typography.bodySmall,
                        color = if (isOverBudget) colors.error else colors.onSecondaryContainer.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(2.dp))
                    val balanceColor = when {
                        budgetInCents == null -> colors.onSecondaryContainer.copy(alpha = 0.4f)
                        isOverBudget -> colors.error
                        else -> colors.primary
                    }
                    val balanceText = when {
                        budgetInCents == null -> stringResource(Res.string.shopping_assistant_dashboard_no_budget)
                        remainingBudgetInCents != null && remainingBudgetInCents < 0 -> {
                            "- " + abs(remainingBudgetInCents).formatPrice(includeCurrencySymbol = true)
                        }
                        remainingBudgetInCents != null -> {
                            remainingBudgetInCents.formatPrice(includeCurrencySymbol = true)
                        }
                        else -> "—"
                    }
                    MinhaDespensaText(
                        text = balanceText,
                        fontStyle = typography.priceLabel,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor,
                    )
                    Spacer(Modifier.height(1.dp))
                    val balanceSubtext = when {
                        budgetInCents == null -> stringResource(Res.string.shopping_assistant_dashboard_unlimited)
                        isOverBudget -> stringResource(Res.string.shopping_assistant_dashboard_over_status)
                        else -> stringResource(Res.string.shopping_assistant_dashboard_available)
                    }
                    MinhaDespensaText(
                        text = balanceSubtext,
                        fontStyle = typography.bodySmall,
                        color = if (isOverBudget) colors.error else colors.onSecondaryContainer.copy(alpha = 0.5f),
                        fontWeight = if (isOverBudget) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }

            // Barra de Progresso ou Banner para Definir Teto
            if (budgetInCents != null) {
                val progress = budgetProgress ?: 0f
                InverseAnchoredGauge(
                    progress = progress,
                    targetLabel = budgetInCents.formatPrice(includeCurrencySymbol = true),
                    gaugeHeight = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isOverBudget) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = colors.error,
                                modifier = Modifier.size(13.dp),
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.shopping_assistant_dashboard_over_budget_alert),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.error,
                            )
                        }
                    } else {
                        val percent = (progress * 100).toInt()
                        MinhaDespensaText(
                            text = stringResource(Res.string.shopping_assistant_dashboard_budget_used, percent),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.6f),
                        )
                        if (remainingBudgetInCents != null) {
                            MinhaDespensaText(
                                text = stringResource(
                                    Res.string.shopping_assistant_dashboard_remaining,
                                    remainingBudgetInCents.formatPrice(includeCurrencySymbol = true),
                                ),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                            )
                        }
                    }
                }
            } else if (!isCompleted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary.copy(alpha = 0.08f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditBudget),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            MinhaDespensaText(
                                text = stringResource(Res.string.shopping_assistant_dashboard_set_budget_hint),
                                fontStyle = typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

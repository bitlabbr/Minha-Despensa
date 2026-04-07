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

package com.bitlabbr.minhadespensa.uisystem.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.CustomTopBar
import com.bitlabbr.minhadespensa.uisystem.components.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.*
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun HomeScreen(
    bottomPadding: Dp = 0.dp
) {
    val widgets = HomeMockData.widgets
    val appColors = MinhaDespensaTheme.color
    val appTypography = MinhaDespensaTheme.typography

    Scaffold(
        topBar = {
            CustomTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    CustomText(
                        text = "",
                        fontStyle = appTypography.displayMedium,
                        color = appColors.onBackground
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPadding + 16.dp
                )
            ) {
                item {
                    PrimaryContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = MinhaDespensaTheme.dimens.paddingSmall, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = MinhaDespensaTheme.dimens.paddingLarge,
                                    bottom = MinhaDespensaTheme.dimens.paddingLarge
                                )
                        ) {
                            PrimaryContainerHeader{}
                            widgets.forEach { widget ->
                                Column() {
                                    when (widget) {
                                        is HomeWidget.FinancialSummary -> FinancialCard(widget)
                                        is HomeWidget.ExpiringSoon -> ExpiringSoonCard(widget)
                                        is HomeWidget.ConsumptionTrend -> ConsumptionTrendCard(widget)
                                        is HomeWidget.SmartList -> SmartListCard(widget)
                                        is HomeWidget.QuickInsert -> QuickInsertCard()
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
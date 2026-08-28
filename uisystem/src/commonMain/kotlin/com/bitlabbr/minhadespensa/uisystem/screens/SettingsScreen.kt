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

package com.bitlabbr.minhadespensa.uisystem.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.gauge.BudgetGauge
import com.bitlabbr.minhadespensa.uisystem.components.core.gauge.InverseAnchoredGauge
import com.bitlabbr.minhadespensa.uisystem.components.core.header.PrimaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.settins_section_tile_bottom
import minhadespensa.uisystem.generated.resources.settins_section_tile_top
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    bottomPadding: Dp = 0.dp
) {

    val appDimens = MinhaDespensaTheme.dimens

    Scaffold(
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    MinhaDespensaText(
                        text = "",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = getAppColors().onBackground
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
                    bottom = bottomPadding
                )
            ) {
                item {
                    PrimaryContainerGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = appDimens.paddingSmall, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = appDimens.paddingLarge,
                                    bottom = appDimens.paddingLarge
                                )
                        ) {
                            PrimaryContainerHeader(
                                textTop = stringResource(Res.string.settins_section_tile_top),
                                textBottom = stringResource(Res.string.settins_section_tile_bottom)
                            ) {}
                            SecondaryContainerGlassCard(
                                modifier = Modifier
                                    .animateContentSize()
                                    .padding(
                                        horizontal = MinhaDespensaTheme.dimens.paddingSmall,
                                        vertical = MinhaDespensaTheme.dimens.paddingSmall
                                    ),
                                content = {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Spacer(modifier = Modifier.size(appDimens.paddingSmall))
                                        MinhaDespensaText(
                                            fontStyle = MinhaDespensaTheme.typography.displayMedium,
                                            color = getAppColors().onBackground,
                                            alignment = TextAlign.Center,
                                            text = "Em breve...",
                                            modifier = Modifier.padding(all = appDimens.paddingSmall),
                                        )
                                        Spacer(modifier = Modifier.size(appDimens.paddingSmall))
                                        MinhaDespensaText(
                                            fontStyle = MinhaDespensaTheme.typography.bodySmall,
                                            color = getAppColors().onBackground.copy(alpha = 0.65f),
                                            alignment = TextAlign.Center,
                                            text = "Novas funcionalidades estarão aqui!",
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier.padding(all = appDimens.paddingSmall),
                                        )
                                        Spacer(modifier = Modifier.size(appDimens.paddingSmall))
                                        BudgetGauge(
                                            modifier = Modifier.size(200.dp),
                                            progress = 1f,

                                            )
                                        Spacer(modifier = Modifier.size(appDimens.paddingSmall))
                                        InverseAnchoredGauge(
                                            progress = 1.5f,
                                            targetLabel = "100",
                                            gaugeHeight = 35.dp,
                                        )

                                        InverseAnchoredGauge(
                                            progress = .7f,
                                            targetLabel = "100",
                                            gaugeHeight = 35.dp,
                                            colorPrimary = getAppColors().secondary,
                                            colorSecondary = getAppColors().primary.copy(alpha = 0.65f),
                                        )
                                        Spacer(modifier = Modifier.size(450.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
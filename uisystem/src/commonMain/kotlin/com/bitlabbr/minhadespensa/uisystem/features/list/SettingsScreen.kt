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

package com.bitlabbr.minhadespensa.uisystem.features.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.CustomTopBar
import com.bitlabbr.minhadespensa.uisystem.components.GlassCard
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CustomTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    CustomText(
                        text = "Configurações",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = MinhaDespensaTheme.color.onBackground
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuItem()
                MenuItem()
                MenuItem()
                MenuItem()
                MenuItem()
                MenuItem()
            }
        }
    }
}

@Composable
private fun MenuItem() {
    val appDimens = MinhaDespensaTheme.dimens
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = appDimens.paddingSmall,
                end = appDimens.paddingSmall,
                top = appDimens.paddingSmall / 2,
                bottom = appDimens.paddingSmall / 2
            )
    ) {
        Column {
            CustomText(
                fontStyle = MinhaDespensaTheme.typography.displayMedium,
                color = MinhaDespensaTheme.color.onBackground,
                alignment = TextAlign.Center,
                text = "Configurações",
                modifier = Modifier.padding(all = appDimens.paddingSmall),
            )
        }
    }
}
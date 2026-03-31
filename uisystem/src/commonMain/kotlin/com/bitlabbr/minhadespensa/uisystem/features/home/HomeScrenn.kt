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
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.CustomTopBar
import com.bitlabbr.minhadespensa.uisystem.components.GlassCard
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

@Composable
fun HomeScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Scaffold(
        topBar = {
            CustomTopBar(
                backgroundColor = Color.Transparent,
                centerContent = {
                    CustomText(
                        text = "Home",
                        fontStyle = MinhaDespensaTheme.typography.displayMedium,
                        color = MinhaDespensaTheme.color.onBackground
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPadding + 16.dp
                )
            ) {
                items(10) { product ->
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
                        Spacer(modifier = Modifier.heightIn(200.dp))
                        CustomText(
                            text = "Isso eh um teste"
                        )
                        Spacer(modifier = Modifier.heightIn(200.dp))
                    }
                }
            }
        }
    }
}
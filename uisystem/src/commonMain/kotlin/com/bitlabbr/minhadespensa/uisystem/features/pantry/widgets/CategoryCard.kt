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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets

import androidx.compose.runtime.Composable
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.SecondaryContainerHeader
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.inventory_card_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryCard(
    data: PantryWidget.CategoriesSummary
) {
    val colors = MinhaDespensaTheme.color
    val appDimens = MinhaDespensaTheme.dimens

    SecondaryContainerGlassCard(
        content = {
            SecondaryContainerHeader(
                text = stringResource(Res.string.inventory_card_title)
            )
        }
    )
}
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

package com.bitlabbr.minhadespensa.uisystem.mapper

import androidx.compose.runtime.Composable
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val MeasureUnit.labelRes: StringResource
    get() = when (this) {
        MeasureUnit.KILOGRAM -> Res.string.measure_unit_kilogram
        MeasureUnit.GRAM -> Res.string.measure_unit_gram
        MeasureUnit.LITER -> Res.string.measure_unit_liter
        MeasureUnit.MILLILITER -> Res.string.measure_unit_milliliter
        MeasureUnit.UNIT -> Res.string.measure_unit_unit
        MeasureUnit.PACKAGE -> Res.string.measure_unit_package
    }


@Composable
fun MeasureUnit.toLabel(): String = stringResource(this.labelRes)
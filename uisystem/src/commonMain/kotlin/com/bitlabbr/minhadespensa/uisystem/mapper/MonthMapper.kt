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

import kotlinx.datetime.Month
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource

val Month.shortNameRes: StringResource
    get() = when (this) {
        Month.JANUARY -> Res.string.month_1_short
        Month.FEBRUARY -> Res.string.month_2_short
        Month.MARCH -> Res.string.month_3_short
        Month.APRIL -> Res.string.month_4_short
        Month.MAY -> Res.string.month_5_short
        Month.JUNE -> Res.string.month_6_short
        Month.JULY -> Res.string.month_7_short
        Month.AUGUST -> Res.string.month_8_short
        Month.SEPTEMBER -> Res.string.month_9_short
        Month.OCTOBER -> Res.string.month_10_short
        Month.NOVEMBER -> Res.string.month_11_short
        Month.DECEMBER -> Res.string.month_12_short
    }

fun Month.toShortName(): String = when (this) {
    Month.JANUARY -> "Jan"
    Month.FEBRUARY -> "Fev"
    Month.MARCH -> "Mar"
    Month.APRIL -> "Abr"
    Month.MAY -> "Mai"
    Month.JUNE -> "Jun"
    Month.JULY -> "Jul"
    Month.AUGUST -> "Ago"
    Month.SEPTEMBER -> "Set"
    Month.OCTOBER -> "Out"
    Month.NOVEMBER -> "Nov"
    Month.DECEMBER -> "Dez"
}

fun Int.toShortMonthName(): String {
    return if (this in 1..12) Month(this).toShortName() else "M$this"
}

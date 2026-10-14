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

package com.bitlabbr.minhadespensa.core.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Duration.Companion.minutes

fun getCurrentTime(): Long = Clock.System.now().toEpochMilliseconds()

fun isValidTimestamp(timestamp: Long): Boolean {
    if (timestamp <= 0) return false

    val instant = try {
        Instant.fromEpochMilliseconds(timestamp)
    } catch (e: Exception) {
        return false
    }

    val now = Clock.System.now()
    val minValid = Instant.parse("2000-01-01T00:00:00Z")
    val maxFuture = now.plus(1.minutes)

    return instant in minValid..maxFuture
}

/**
 * Returns a [Pair] of epoch milliseconds representing the start and end of the specified [month] in [year].
 * Start is inclusive (00:00:00.000) and end is inclusive (23:59:59.999).
 */
fun getMonthTimeRange(
    year: Int,
    month: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Pair<Long, Long> {
    require(year in 2000..2100) { "Ano inválido: $year" }
    require(month in 1..12) { "Mês inválido: $month. Deve estar entre 1 e 12." }

    val startOfMonth = LocalDate(year, month, 1)
        .atStartOfDayIn(timeZone)
        .toEpochMilliseconds()

    val nextMonth = if (month == 12) 1 else month + 1
    val nextYear = if (month == 12) year + 1 else year
    val startOfNextMonth = LocalDate(nextYear, nextMonth, 1)
        .atStartOfDayIn(timeZone)
        .toEpochMilliseconds()

    val endOfMonth = startOfNextMonth - 1
    return Pair(startOfMonth, endOfMonth)
}
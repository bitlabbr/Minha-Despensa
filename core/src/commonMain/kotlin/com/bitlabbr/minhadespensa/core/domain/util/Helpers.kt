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
import kotlin.time.Duration.Companion.milliseconds

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
    val maxFuture = now.plus(100.milliseconds)

    return instant in minValid..maxFuture
}
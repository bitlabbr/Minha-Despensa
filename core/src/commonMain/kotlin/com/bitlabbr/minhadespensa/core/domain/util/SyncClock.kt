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
import kotlin.concurrent.Volatile

class SyncClock(
    private val timeProvider: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    @Volatile
    var serverOffsetMillis: Long = 0L
        private set

    fun now(): Long = timeProvider() + serverOffsetMillis

    fun calibrate(serverTimestamp: Long, requestSentAt: Long, responseReceivedAt: Long) {
        val rtt = maxOf(0L, responseReceivedAt - requestSentAt)
        val estimatedServerNow = serverTimestamp + (rtt / 2)
        serverOffsetMillis = estimatedServerNow - responseReceivedAt
    }

    fun nextTimestampAfter(existingUpdatedAt: Long): Long {
        return maxOf(now(), existingUpdatedAt + 1L)
    }

    fun reset() {
        serverOffsetMillis = 0L
    }
}

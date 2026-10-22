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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncClockTest {

    @Test
    fun defaultClockShouldReturnCurrentTime() {
        var simulatedTime = 1_000_000L
        val clock = SyncClock(timeProvider = { simulatedTime })

        assertEquals(1_000_000L, clock.now())
        assertEquals(0L, clock.serverOffsetMillis)
    }

    @Test
    fun calibrateShouldAdjustOffsetWithRttHalved() {
        var localTime = 1_000_200L
        val clock = SyncClock(timeProvider = { localTime })

        // Request sent at local 1000000, server processed at 1000500, received at local 1000200
        // RTT = 1000200 - 1000000 = 200ms -> RTT/2 = 100ms
        // Estimated server time at response = 1000500 + 100 = 1000600
        // Offset = 1000600 - 1000200 = +400ms
        clock.calibrate(
            serverTimestamp = 1_000_500L,
            requestSentAt = 1_000_000L,
            responseReceivedAt = 1_000_200L
        )

        assertEquals(400L, clock.serverOffsetMillis)
        assertEquals(1_000_600L, clock.now())
    }

    @Test
    fun nextTimestampAfterShouldGuaranteeMonotonicityEvenWithLaggingClock() {
        var localTime = 1000L
        val clock = SyncClock(timeProvider = { localTime })

        // Existing entity was updated at 1500L (future relative to clock.now())
        val next = clock.nextTimestampAfter(existingUpdatedAt = 1500L)
        assertEquals(1501L, next)

        // If clock is ahead of existing entity
        localTime = 2000L
        val nextAhead = clock.nextTimestampAfter(existingUpdatedAt = 1500L)
        assertEquals(2000L, nextAhead)
    }

    @Test
    fun resetShouldRestoreOffsetToZero() {
        val clock = SyncClock(timeProvider = { 1000L })
        clock.calibrate(serverTimestamp = 5000L, requestSentAt = 900L, responseReceivedAt = 1000L)
        assertTrue(clock.serverOffsetMillis != 0L)

        clock.reset()
        assertEquals(0L, clock.serverOffsetMillis)
        assertEquals(1000L, clock.now())
    }
}

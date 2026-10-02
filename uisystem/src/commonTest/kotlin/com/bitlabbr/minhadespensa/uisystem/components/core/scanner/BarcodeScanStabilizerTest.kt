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
 *     - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.components.core.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BarcodeScanStabilizerTest {

    @Test
    fun `single frame should not confirm scan and transition to Stabilizing`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2, maxIntervalBetweenFramesMs = 400L)

        val result = stabilizer.process(barcode = "7891000100104", timestampMs = 1000L)

        assertIs<BarcodeStabilizationResult.Stabilizing>(result)
        assertEquals("7891000100104", result.barcode)
        assertEquals(1, result.currentMatches)
        assertEquals(2, result.requiredMatches)
    }

    @Test
    fun `second identical frame within max interval should confirm barcode`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2, maxIntervalBetweenFramesMs = 400L)

        stabilizer.process(barcode = "7891000100104", timestampMs = 1000L)
        val result = stabilizer.process(barcode = "7891000100104", timestampMs = 1100L)

        assertIs<BarcodeStabilizationResult.Confirmed>(result)
        assertEquals("7891000100104", result.barcode)
    }

    @Test
    fun `different barcode in second frame should reset count and start stabilizing new barcode`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2, maxIntervalBetweenFramesMs = 400L)

        val firstResult = stabilizer.process(barcode = "7891000100", timestampMs = 1000L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(firstResult)
        assertEquals(1, firstResult.currentMatches)

        val secondResult = stabilizer.process(barcode = "7891000100104", timestampMs = 1050L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(secondResult)
        assertEquals("7891000100104", secondResult.barcode)
        assertEquals(1, secondResult.currentMatches)

        val thirdResult = stabilizer.process(barcode = "7891000100104", timestampMs = 1100L)
        assertIs<BarcodeStabilizationResult.Confirmed>(thirdResult)
        assertEquals("7891000100104", thirdResult.barcode)
    }

    @Test
    fun `same barcode after interval timeout should reset count and restart stabilization`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2, maxIntervalBetweenFramesMs = 400L)

        val first = stabilizer.process(barcode = "7891000100104", timestampMs = 1000L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(first)
        assertEquals(1, first.currentMatches)

        // Elapsed time: 450ms > 400ms limit -> resets consecutive count
        val second = stabilizer.process(barcode = "7891000100104", timestampMs = 1450L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(second)
        assertEquals(1, second.currentMatches)

        // Another frame within 100ms -> confirms
        val third = stabilizer.process(barcode = "7891000100104", timestampMs = 1550L)
        assertIs<BarcodeStabilizationResult.Confirmed>(third)
        assertEquals("7891000100104", third.barcode)
    }

    @Test
    fun `blank or empty barcode should be ignored`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2)

        val nullResult = stabilizer.process(barcode = null, timestampMs = 1000L)
        assertEquals(BarcodeStabilizationResult.Ignored, nullResult)

        val emptyResult = stabilizer.process(barcode = "", timestampMs = 1050L)
        assertEquals(BarcodeStabilizationResult.Ignored, emptyResult)

        val blankResult = stabilizer.process(barcode = "   ", timestampMs = 1100L)
        assertEquals(BarcodeStabilizationResult.Ignored, blankResult)
    }

    @Test
    fun `subsequent frames after confirmation should be ignored until reset`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2)

        stabilizer.process(barcode = "7891000100104", timestampMs = 1000L)
        stabilizer.process(barcode = "7891000100104", timestampMs = 1050L)

        val extra = stabilizer.process(barcode = "7891000100104", timestampMs = 1100L)
        assertEquals(BarcodeStabilizationResult.Ignored, extra)

        val differentAfterConfirmed = stabilizer.process(barcode = "1234567890", timestampMs = 1150L)
        assertEquals(BarcodeStabilizationResult.Ignored, differentAfterConfirmed)
    }

    @Test
    fun `reset should allow stabilizer to accept new barcode sequences`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 2)

        stabilizer.process(barcode = "7891000100104", timestampMs = 1000L)
        stabilizer.process(barcode = "7891000100104", timestampMs = 1050L)

        stabilizer.reset()

        val afterReset1 = stabilizer.process(barcode = "7891000100104", timestampMs = 2000L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(afterReset1)
        assertEquals(1, afterReset1.currentMatches)

        val afterReset2 = stabilizer.process(barcode = "7891000100104", timestampMs = 2050L)
        assertIs<BarcodeStabilizationResult.Confirmed>(afterReset2)
    }

    @Test
    fun `custom requiredMatches should require N identical consecutive readings`() {
        val stabilizer = BarcodeScanStabilizer(requiredConsecutiveMatches = 3, maxIntervalBetweenFramesMs = 300L)

        val r1 = stabilizer.process(barcode = "7891000100104", timestampMs = 100L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(r1)
        assertEquals(1, r1.currentMatches)

        val r2 = stabilizer.process(barcode = "7891000100104", timestampMs = 200L)
        assertIs<BarcodeStabilizationResult.Stabilizing>(r2)
        assertEquals(2, r2.currentMatches)

        val r3 = stabilizer.process(barcode = "7891000100104", timestampMs = 300L)
        assertIs<BarcodeStabilizationResult.Confirmed>(r3)
        assertEquals("7891000100104", r3.barcode)
    }
}

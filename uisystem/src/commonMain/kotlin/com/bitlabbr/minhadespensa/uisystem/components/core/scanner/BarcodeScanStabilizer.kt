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

/**
 * Result of processing a barcode frame through [BarcodeScanStabilizer].
 */
sealed interface BarcodeStabilizationResult {
    /**
     * Barcode has achieved required consecutive matches within the allowed interval.
     */
    data class Confirmed(val barcode: String) : BarcodeStabilizationResult

    /**
     * Barcode is accumulating consecutive matches but hasn't reached threshold yet.
     */
    data class Stabilizing(
        val barcode: String,
        val currentMatches: Int,
        val requiredMatches: Int,
    ) : BarcodeStabilizationResult

    /**
     * Input was null/blank or stabilizer has already confirmed a scan.
     */
    data object Ignored : BarcodeStabilizationResult
}

/**
 * State stabilizer that requires [requiredConsecutiveMatches] identical barcode readings
 * within [maxIntervalBetweenFramesMs] before confirming a scan.
 *
 * This eliminates accidental, partial, or out-of-focus scans caused by rapid movement.
 */
class BarcodeScanStabilizer(
    val requiredConsecutiveMatches: Int = 2,
    val maxIntervalBetweenFramesMs: Long = 400L,
) {
    private var lastBarcode: String? = null
    private var consecutiveCount: Int = 0
    private var lastTimestampMs: Long = 0L
    private var isConfirmed: Boolean = false

    /**
     * Processes a detected [barcode].
     *
     * @param barcode The raw scanned barcode string.
     * @param timestampMs Timestamp of the frame in milliseconds.
     * @return [BarcodeStabilizationResult] indicating if the barcode was confirmed, stabilizing, or ignored.
     */
    fun process(barcode: String?, timestampMs: Long): BarcodeStabilizationResult {
        if (isConfirmed || barcode.isNullOrBlank()) {
            return BarcodeStabilizationResult.Ignored
        }

        val elapsed = timestampMs - lastTimestampMs
        val isConsecutive = lastBarcode == barcode && (lastTimestampMs == 0L || elapsed <= maxIntervalBetweenFramesMs)

        if (isConsecutive) {
            consecutiveCount++
        } else {
            lastBarcode = barcode
            consecutiveCount = 1
        }
        lastTimestampMs = timestampMs

        return if (consecutiveCount >= requiredConsecutiveMatches) {
            isConfirmed = true
            BarcodeStabilizationResult.Confirmed(barcode)
        } else {
            BarcodeStabilizationResult.Stabilizing(
                barcode = barcode,
                currentMatches = consecutiveCount,
                requiredMatches = requiredConsecutiveMatches,
            )
        }
    }

    /**
     * Resets the stabilizer state to allow new scans.
     */
    fun reset() {
        lastBarcode = null
        consecutiveCount = 0
        lastTimestampMs = 0L
        isConfirmed = false
    }
}

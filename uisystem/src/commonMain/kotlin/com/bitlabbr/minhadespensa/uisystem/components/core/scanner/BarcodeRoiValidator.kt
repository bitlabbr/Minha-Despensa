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
 * Geometric bounding rectangle for a detected barcode.
 */
data class BarcodeRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }
}

/**
 * Validates whether a detected barcode bounding box lies within the central Region of Interest (ROI)
 * corresponding to the scanner's visual reticle.
 *
 * @param horizontalFraction Fraction of image width allocated to the central ROI (e.g. 0.70 = 70% width).
 * @param verticalFraction Fraction of image height allocated to the central ROI (e.g. 0.45 = 45% height).
 */
class BarcodeRoiValidator(
    val horizontalFraction: Float = 0.70f,
    val verticalFraction: Float = 0.45f,
) {
    init {
        require(horizontalFraction in 0.05f..1.0f) { "horizontalFraction must be between 0.05 and 1.0" }
        require(verticalFraction in 0.05f..1.0f) { "verticalFraction must be between 0.05 and 1.0" }
    }

    /**
     * Calculates the bounding rectangle of the central Region of Interest (ROI) for given [imageWidth] and [imageHeight].
     */
    fun calculateRoi(imageWidth: Int, imageHeight: Int): BarcodeRect {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return BarcodeRect(0f, 0f, 0f, 0f)
        }

        val roiWidth = imageWidth * horizontalFraction
        val roiHeight = imageHeight * verticalFraction

        val left = (imageWidth - roiWidth) / 2f
        val top = (imageHeight - roiHeight) / 2f
        val right = left + roiWidth
        val bottom = top + roiHeight

        return BarcodeRect(left = left, top = top, right = right, bottom = bottom)
    }

    /**
     * Checks if the center of [barcodeBox] is inside the central Region of Interest (ROI).
     *
     * @param barcodeBox Bounding rectangle of the barcode in upright image coordinates.
     * @param imageWidth Width of the upright image.
     * @param imageHeight Height of the upright image.
     * @return `true` if the barcode is centered inside the reticle ROI, `false` otherwise.
     */
    fun isInsideRoi(barcodeBox: BarcodeRect?, imageWidth: Int, imageHeight: Int): Boolean {
        if (barcodeBox == null || imageWidth <= 0 || imageHeight <= 0) return false
        if (barcodeBox.width <= 0f && barcodeBox.height <= 0f) return false

        val roi = calculateRoi(imageWidth, imageHeight)
        return roi.contains(barcodeBox.centerX, barcodeBox.centerY)
    }
}

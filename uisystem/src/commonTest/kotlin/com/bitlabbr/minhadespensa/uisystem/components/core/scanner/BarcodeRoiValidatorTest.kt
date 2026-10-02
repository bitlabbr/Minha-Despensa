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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarcodeRoiValidatorTest {

    private val validator = BarcodeRoiValidator(
        horizontalFraction = 0.70f,
        verticalFraction = 0.40f,
    )

    @Test
    fun `calculateRoi should compute central box with correct dimensions and margins`() {
        val roi = validator.calculateRoi(imageWidth = 1000, imageHeight = 2000)

        // Width = 1000 * 0.7 = 700; left = (1000 - 700)/2 = 150; right = 850
        // Height = 2000 * 0.4 = 800; top = (2000 - 800)/2 = 600; bottom = 1400
        assertEquals(150f, roi.left)
        assertEquals(600f, roi.top)
        assertEquals(850f, roi.right)
        assertEquals(1400f, roi.bottom)
        assertEquals(700f, roi.width)
        assertEquals(800f, roi.height)
        assertEquals(500f, roi.centerX)
        assertEquals(1000f, roi.centerY)
    }

    @Test
    fun `barcode centered within image should be accepted`() {
        // Centered around (500, 1000)
        val barcode = BarcodeRect(left = 400f, top = 950f, right = 600f, bottom = 1050f)

        val result = validator.isInsideRoi(barcode, imageWidth = 1000, imageHeight = 2000)

        assertTrue(result, "Centered barcode must be accepted within ROI")
    }

    @Test
    fun `barcode on extreme left edge should be rejected`() {
        // Left edge: center is at x = 50, which is < 150
        val barcode = BarcodeRect(left = 0f, top = 950f, right = 100f, bottom = 1050f)

        val result = validator.isInsideRoi(barcode, imageWidth = 1000, imageHeight = 2000)

        assertFalse(result, "Barcode on extreme left edge must be rejected")
    }

    @Test
    fun `barcode on extreme right edge should be rejected`() {
        // Right edge: center is at x = 950, which is > 850
        val barcode = BarcodeRect(left = 900f, top = 950f, right = 1000f, bottom = 1050f)

        val result = validator.isInsideRoi(barcode, imageWidth = 1000, imageHeight = 2000)

        assertFalse(result, "Barcode on extreme right edge must be rejected")
    }

    @Test
    fun `barcode on extreme top edge should be rejected`() {
        // Top edge: center is at y = 100, which is < 600
        val barcode = BarcodeRect(left = 400f, top = 50f, right = 600f, bottom = 150f)

        val result = validator.isInsideRoi(barcode, imageWidth = 1000, imageHeight = 2000)

        assertFalse(result, "Barcode on extreme top edge must be rejected")
    }

    @Test
    fun `barcode on extreme bottom edge should be rejected`() {
        // Bottom edge: center is at y = 1850, which is > 1400
        val barcode = BarcodeRect(left = 400f, top = 1800f, right = 600f, bottom = 1900f)

        val result = validator.isInsideRoi(barcode, imageWidth = 1000, imageHeight = 2000)

        assertFalse(result, "Barcode on extreme bottom edge must be rejected")
    }

    @Test
    fun `barcode on exact boundary of ROI should be accepted`() {
        // Boundary: centerX = 150f, centerY = 600f
        val boundaryBarcode = BarcodeRect(left = 100f, top = 550f, right = 200f, bottom = 650f)

        val result = validator.isInsideRoi(boundaryBarcode, imageWidth = 1000, imageHeight = 2000)

        assertTrue(result, "Barcode on exact ROI boundary should be accepted")
    }

    @Test
    fun `null or zero dimension barcode should be rejected`() {
        assertFalse(validator.isInsideRoi(null, imageWidth = 1000, imageHeight = 2000))

        val zeroSizeBarcode = BarcodeRect(left = 500f, top = 1000f, right = 500f, bottom = 1000f)
        assertFalse(validator.isInsideRoi(zeroSizeBarcode, imageWidth = 1000, imageHeight = 2000))
    }

    @Test
    fun `invalid image dimensions should return false`() {
        val validBarcode = BarcodeRect(left = 400f, top = 950f, right = 600f, bottom = 1050f)

        assertFalse(validator.isInsideRoi(validBarcode, imageWidth = 0, imageHeight = 2000))
        assertFalse(validator.isInsideRoi(validBarcode, imageWidth = 1000, imageHeight = -1))
    }
}

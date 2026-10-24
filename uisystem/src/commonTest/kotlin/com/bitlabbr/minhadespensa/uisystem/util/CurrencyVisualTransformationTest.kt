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

package com.bitlabbr.minhadespensa.uisystem.util

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyVisualTransformationTest {

    private val transformation = CurrencyThousandsVisualTransformation()

    @Test
    fun filter_formatsStandardThousandsAndDecimalsCorrectly() {
        val result = transformation.filter(AnnotatedString("2549,98"))
        assertEquals("2.549,98", result.text.text)
    }

    @Test
    fun filter_formatsLargeMillionsCorrectly() {
        val result = transformation.filter(AnnotatedString("1234567,89"))
        assertEquals("1.234.567,89", result.text.text)
    }

    @Test
    fun filter_formatsIntegerWithoutDecimals() {
        val result = transformation.filter(AnnotatedString("1020"))
        assertEquals("1.020", result.text.text)
    }

    @Test
    fun filter_formatsSmallNumbersWithoutThousandsDots() {
        val result = transformation.filter(AnnotatedString("500"))
        assertEquals("500", result.text.text)

        val resultWithDec = transformation.filter(AnnotatedString("50,25"))
        assertEquals("50,25", resultWithDec.text.text)
    }

    @Test
    fun filter_emptyString_returnsEmpty() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
    }

    @Test
    fun offsetMapping_correctlyMapsCursorPositions() {
        // "2549,98" (length 7) -> "2.549,98" (length 8)
        val transformed = transformation.filter(AnnotatedString("2549,98"))
        val mapping = transformed.offsetMapping

        // Offset 0 (before '2') -> 0
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(0, mapping.transformedToOriginal(0))

        // Offset 1 (after '2', before dot) -> transformed 2 (after '2.')
        assertEquals(2, mapping.originalToTransformed(1))

        // Offset 4 (after '9', before comma) -> transformed 5
        assertEquals(5, mapping.originalToTransformed(4))

        // Offset 7 (end of original) -> transformed 8 (end of transformed)
        assertEquals(8, mapping.originalToTransformed(7))
        assertEquals(7, mapping.transformedToOriginal(8))
    }
}

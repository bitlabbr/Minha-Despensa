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

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatterTest {

    @Test
    fun formatPrice_formatsStandardAmountsCorrectly() {
        assertEquals("15,50", 1550L.formatPrice())
        assertEquals("10,00", 1000L.formatPrice())
        assertEquals("0,99", 99L.formatPrice())
    }

    @Test
    fun formatPrice_formatsSingleDigitCentavosWithLeadingZero() {
        assertEquals("0,05", 5L.formatPrice())
        assertEquals("1,05", 105L.formatPrice())
        assertEquals("0,00", 0L.formatPrice())
    }

    @Test
    fun formatPrice_formatsNegativeValuesCorrectly() {
        assertEquals("-15,50", (-1550L).formatPrice())
        assertEquals("-0,05", (-5L).formatPrice())
    }

    @Test
    fun formatPrice_formatsLargeAmountsCorrectly() {
        assertEquals("1234567,89", 123456789L.formatPrice())
    }

    @Test
    fun formatPrice_withCurrencySymbol_prependsSymbol() {
        assertEquals("R$ 15,50", 1550L.formatPrice(includeCurrencySymbol = true))
        assertEquals("R$ 0,00", 0L.formatPrice(includeCurrencySymbol = true))
        assertEquals("R$ -10,00", (-1000L).formatPrice(includeCurrencySymbol = true))
    }
}

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

package com.bitlabbr.minhadespensa.uisystem.mapper

import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MonthMapperTest {

    @Test
    fun toShortName_returnsCorrectAbbreviationForEachMonth() {
        val expected = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        Month.entries.forEachIndexed { index, month ->
            assertEquals(expected[index], month.toShortName())
        }
    }

    @Test
    fun shortNameRes_mapsEveryMonthToAStringResource() {
        Month.entries.forEach { month ->
            assertNotNull(month.shortNameRes, "Month $month should have a non-null StringResource")
        }
    }

    @Test
    fun toShortMonthName_formatsValidAndInvalidMonthNumbers() {
        assertEquals("Jan", 1.toShortMonthName())
        assertEquals("Dez", 12.toShortMonthName())
        assertEquals("M0", 0.toShortMonthName())
        assertEquals("M13", 13.toShortMonthName())
    }
}

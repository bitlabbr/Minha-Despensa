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

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MeasureUnitMapperTest {

    @Test
    fun toAbbreviation_returnsCorrectAbbreviationForEachUnit() {
        assertEquals("kg", MeasureUnit.KILOGRAM.toAbbreviation())
        assertEquals("g", MeasureUnit.GRAM.toAbbreviation())
        assertEquals("L", MeasureUnit.LITER.toAbbreviation())
        assertEquals("ml", MeasureUnit.MILLILITER.toAbbreviation())
        assertEquals("un", MeasureUnit.UNIT.toAbbreviation())
        assertEquals("pct", MeasureUnit.PACKAGE.toAbbreviation())
    }

    @Test
    fun labelRes_mapsEveryUnitToAStringResource() {
        MeasureUnit.entries.forEach { unit ->
            assertNotNull(unit.labelRes, "MeasureUnit $unit should have a non-null StringResource label")
        }
    }
}

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

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class DeterministicIdGeneratorTest {

    @Test
    fun sameEanShouldProduceExactSameUuid() {
        val id1 = DeterministicIdGenerator.forProduct("7891000100103", "Arroz Camil")
        val id2 = DeterministicIdGenerator.forProduct("  7891000100103  ", "Arroz Diferente")
        assertEquals(id1, id2)
    }

    @Test
    fun sameNormalizedNameAndUnitWithoutEanShouldProduceExactSameUuid() {
        val id1 = DeterministicIdGenerator.forProduct(null, "Pão Francês", MeasureUnit.UNIT)
        val id2 = DeterministicIdGenerator.forProduct("", "pao  frances", MeasureUnit.UNIT)
        val id3 = DeterministicIdGenerator.forProduct("   ", "PAO FRANCÊS", MeasureUnit.UNIT)
        assertEquals(id1, id2)
        assertEquals(id1, id3)
    }

    @Test
    fun differentUnitOrNameShouldProduceDifferentUuid() {
        val idKg = DeterministicIdGenerator.forProduct(null, "Banana Prata", MeasureUnit.KILOGRAM)
        val idUnit = DeterministicIdGenerator.forProduct(null, "Banana Prata", MeasureUnit.UNIT)
        assertNotEquals(idKg, idUnit)

        val idMaca = DeterministicIdGenerator.forProduct(null, "Maçã Gala", MeasureUnit.KILOGRAM)
        assertNotEquals(idKg, idMaca)
    }

    @Test
    fun categoryNameShouldProduceConsistentUuid() {
        val id1 = DeterministicIdGenerator.forCategory("Laticínios & Frios")
        val id2 = DeterministicIdGenerator.forCategory("laticinios & frios")
        assertEquals(id1, id2)
    }

    @Test
    fun monthlyBudgetShouldFormatProperly() {
        val id = DeterministicIdGenerator.forMonthlyBudget(2026, 10)
        assertEquals("2026-10", id)
    }

    @Test
    fun blankInputsShouldThrowIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DeterministicIdGenerator.productIdForEan("   ")
        }
        assertFailsWith<IllegalArgumentException> {
            DeterministicIdGenerator.productIdForNameAndUnit("   ", "UNIT")
        }
        assertFailsWith<IllegalArgumentException> {
            DeterministicIdGenerator.forCategory("   ")
        }
    }
}

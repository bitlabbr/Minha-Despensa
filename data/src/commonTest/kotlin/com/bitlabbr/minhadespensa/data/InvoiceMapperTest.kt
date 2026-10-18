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

package com.bitlabbr.minhadespensa.data

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.data.local.mapper.parseMeasureUnit
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceItemDto
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InvoiceMapperTest {

    @Test
    fun `toDomain should map InvoiceResponseDto and InvoiceItemDto correctly`() {
        val dto = InvoiceResponseDto(
            accessKey = " 26241012345678000190650010001234561001234567 ",
            storeName = " Supermercado Pão & Mel ",
            emissionDate = " 2026-10-18T12:00:00 ",
            items = listOf(
                InvoiceItemDto(
                    ean = " 7891000111222 ",
                    name = " Biscoito Cream Cracker 400g ",
                    quantity = 2.0,
                    unit = "PCT",
                    unitPriceInCents = 450L,
                    totalPriceInCents = 900L,
                ),
                InvoiceItemDto(
                    ean = null,
                    name = "Tomate Salada",
                    quantity = 1.35,
                    unit = "KG",
                    unitPriceInCents = 800L,
                    totalPriceInCents = 1080L,
                ),
            ),
        )

        val domain = dto.toDomain()

        assertEquals("26241012345678000190650010001234561001234567", domain.accessKey)
        assertEquals("Supermercado Pão & Mel", domain.storeName)
        assertEquals("2026-10-18T12:00:00", domain.emissionDate)
        assertEquals(2, domain.items.size)

        val item1 = domain.items[0]
        assertNotNull(item1.id)
        assertEquals("7891000111222", item1.ean)
        assertEquals("Biscoito Cream Cracker 400g", item1.name)
        assertEquals(2.0, item1.quantity)
        assertEquals(MeasureUnit.PACKAGE, item1.measureUnit)
        assertEquals(450L, item1.unitPriceInCents)
        assertEquals(900L, item1.totalPriceInCents)
        assertTrue(item1.isSelected)

        val item2 = domain.items[1]
        assertNull(item2.ean)
        assertEquals("Tomate Salada", item2.name)
        assertEquals(1.35, item2.quantity)
        assertEquals(MeasureUnit.KILOGRAM, item2.measureUnit)
    }

    @Test
    fun `parseMeasureUnit should map common Brazilian abbreviation strings accurately`() {
        assertEquals(MeasureUnit.KILOGRAM, parseMeasureUnit("kg"))
        assertEquals(MeasureUnit.KILOGRAM, parseMeasureUnit("KG"))
        assertEquals(MeasureUnit.KILOGRAM, parseMeasureUnit("kilo"))
        assertEquals(MeasureUnit.GRAM, parseMeasureUnit("g"))
        assertEquals(MeasureUnit.GRAM, parseMeasureUnit("gr"))
        assertEquals(MeasureUnit.LITER, parseMeasureUnit("l"))
        assertEquals(MeasureUnit.LITER, parseMeasureUnit("LT"))
        assertEquals(MeasureUnit.MILLILITER, parseMeasureUnit("ml"))
        assertEquals(MeasureUnit.PACKAGE, parseMeasureUnit("cx"))
        assertEquals(MeasureUnit.PACKAGE, parseMeasureUnit("pct"))
        assertEquals(MeasureUnit.PACKAGE, parseMeasureUnit("pacote"))
        assertEquals(MeasureUnit.UNIT, parseMeasureUnit("un"))
        assertEquals(MeasureUnit.UNIT, parseMeasureUnit("UND"))
        assertEquals(MeasureUnit.UNIT, parseMeasureUnit(null))
        assertEquals(MeasureUnit.UNIT, parseMeasureUnit("UNKNOWN"))
    }
}

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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.model

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogProductUiModelTest {

    @Test
    fun toUiModel_formatsIntegerWeightWithAbbreviation() {
        val domainProduct = CatalogProduct(
            id = "prod-1",
            name = "Arroz",
            brand = "Tio João",
            category = "Grãos",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 1.0,
            updatedAt = 0L,
            ean = "7891234567890",
        )

        val uiModel = domainProduct.toUiModel()

        assertEquals("prod-1", uiModel.id)
        assertEquals("Arroz", uiModel.name)
        assertEquals("Tio João", uiModel.brand)
        assertEquals("Grãos", uiModel.category)
        assertEquals(MeasureUnit.KILOGRAM, uiModel.measureUnit)
        assertEquals(1.0, uiModel.netWeight)
        assertEquals("7891234567890", uiModel.ean)
        assertEquals("1 kg", uiModel.formattedWeight)
    }

    @Test
    fun toUiModel_formatsDecimalWeightWithCommaAndAbbreviation() {
        val domainProduct = CatalogProduct(
            id = "prod-2",
            name = "Café",
            brand = "Pilão",
            category = "Bebidas",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 0.5,
            updatedAt = 0L,
            ean = null,
        )

        val uiModel = domainProduct.toUiModel()

        assertEquals("0,5 kg", uiModel.formattedWeight)
    }

    @Test
    fun toUiModel_handlesVariousMeasureUnits() {
        val gramProduct = CatalogProduct(
            id = "prod-3",
            name = "Fermento",
            category = "Padaria",
            measureUnit = MeasureUnit.GRAM,
            netWeight = 100.0,
            updatedAt = 0L,
        )
        val literProduct = CatalogProduct(
            id = "prod-4",
            name = "Leite",
            category = "Laticínios",
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0,
            updatedAt = 0L,
        )
        val unitProduct = CatalogProduct(
            id = "prod-5",
            name = "Sabonete",
            category = "Higiene",
            measureUnit = MeasureUnit.UNIT,
            netWeight = 1.0,
            updatedAt = 0L,
        )

        assertEquals("100 g", gramProduct.toUiModel().formattedWeight)
        assertEquals("1 L", literProduct.toUiModel().formattedWeight)
        assertEquals("1 un", unitProduct.toUiModel().formattedWeight)
    }
}

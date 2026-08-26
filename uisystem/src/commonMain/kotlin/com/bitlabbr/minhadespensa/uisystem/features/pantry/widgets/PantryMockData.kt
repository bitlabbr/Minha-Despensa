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

package com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.IconKeys
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PantryMockData {
    val categories = listOf("Todos", "Grãos", "Proteínas", "Bebidas", "Limpeza", "Lanches")


    val pantryCategories: Map<String, List<CatalogProduct>> = mapOf(
        "Grãos" to listOf(
            CatalogProduct(
                id = "1",
                name = "Arroz Branco",
                brand = "Tio João",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 5.0,
                updatedAt = 1753468800000L,
            ),
            CatalogProduct(
                id = "2",
                name = "Arroz Integral",
                brand = "Camil",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 1.0,
                updatedAt = 1753555200000L
            ),
            CatalogProduct(
                id = "3",
                name = "Feijão Preto",
                brand = "Kicaldo",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 1.0,
                updatedAt = 1753641600000L
            ),
            CatalogProduct(
                id = "4",
                name = "Feijão Carioca",
                brand = "Camil",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 1.0,
                updatedAt = 1753728000000L
            ),
            CatalogProduct(
                id = "5",
                name = "Aveia",
                brand = "Quaker",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 0.5,
                updatedAt = 1753814400000L
            )
        ),

        "Proteínas" to listOf(
            CatalogProduct(
                id = "6",
                name = "Peito de Frango",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 1.0,
                updatedAt = 1753900800000L
            ),
            CatalogProduct(
                id = "7",
                name = "Carne Bovina",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 1.0,
                updatedAt = 1753987200000L
            ),
            CatalogProduct(
                id = "8",
                name = "Carne Moída",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 0.5,
                updatedAt = 1754073600000L
            ),
            CatalogProduct(
                id = "9",
                name = "Filé de Peixe",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 0.8,
                updatedAt = 1754160000000L
            )
        ),

        "Bebidas" to listOf(
            CatalogProduct(
                id = "10",
                name = "Água Mineral",
                brand = "Indaiá",
                measureUnit = MeasureUnit.LITER,
                netWeight = 1.5,
                updatedAt = 1754246400000L
            ),
            CatalogProduct(
                id = "11",
                name = "Café",
                brand = "Pilão",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 0.5,
                updatedAt = 1754332800000L
            ),
            CatalogProduct(
                id = "12",
                name = "Suco de Laranja",
                brand = "Del Valle",
                measureUnit = MeasureUnit.LITER,
                netWeight = 1.0,
                updatedAt = 1754419200000L
            ),
            CatalogProduct(
                id = "13",
                name = "Refrigerante Cola",
                brand = "Coca-Cola",
                measureUnit = MeasureUnit.LITER,
                netWeight = 2.0,
                updatedAt = 1754505600000L
            )
        ),

        "Limpeza" to listOf(
            CatalogProduct(
                id = "14",
                name = "Detergente",
                brand = "Ypê",
                measureUnit = MeasureUnit.LITER,
                netWeight = 0.5,
                updatedAt = 1754592000000L
            ),
            CatalogProduct(
                id = "15",
                name = "Água Sanitária",
                brand = "Qboa",
                measureUnit = MeasureUnit.LITER,
                netWeight = 2.0,
                updatedAt = 1754678400000L
            ),
            CatalogProduct(
                id = "16",
                name = "Desinfetante",
                brand = "Pinho Sol",
                measureUnit = MeasureUnit.LITER,
                netWeight = 1.0,
                updatedAt = 1754764800000L
            ),
            CatalogProduct(
                id = "17",
                name = "Sabão em Pó",
                brand = "OMO",
                measureUnit = MeasureUnit.KILOGRAM,
                netWeight = 2.2,
                updatedAt = 1754851200000L
            )
        ),

        "Lanches" to listOf(
            CatalogProduct(
                id = "18",
                name = "Batata Chips",
                brand = "Ruffles",
                measureUnit = MeasureUnit.UNITY,
                netWeight = 120.0,
                updatedAt = 1754937600000L
            ),
            CatalogProduct(
                id = "19",
                name = "Biscoito Cream Cracker",
                brand = "Marilan",
                measureUnit = MeasureUnit.UNITY,
                netWeight = 200.0,
                updatedAt = 1755024000000L
            ),
            CatalogProduct(
                id = "20",
                name = "Chocolate",
                brand = "Lacta",
                measureUnit = MeasureUnit.UNITY,
                netWeight = 90.0,
                updatedAt = 1755110400000L
            ),
            CatalogProduct(
                id = "21",
                name = "Barra de Cereal",
                brand = "Nestlé",
                measureUnit = MeasureUnit.UNITY,
                netWeight = 25.0,
                updatedAt = 1755196800000L
            ),
            CatalogProduct(
                id = "22",
                name = "Pipoca de Micro-ondas",
                brand = "Yoki",
                measureUnit = MeasureUnit.UNITY,
                netWeight = 100.0,
                updatedAt = 1755283200000L
            )
        )
    )


    val widgets = listOf{}
}
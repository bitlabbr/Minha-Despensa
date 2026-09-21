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

package com.bitlabbr.minhadespensa.core

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ShoppingItem subtotalInCents should calculate rounded cents correctly`() {
        val itemFractional = ShoppingItem(
            id = "1",
            listId = "list-1",
            quantity = 1.5,
            priceAtTime = 1000L, // R$ 10,00 -> total esperado: 1500L (R$ 15,00)
            updatedAt = 1000L
        )
        assertEquals(1500L, itemFractional.subtotalInCents)

        val itemWeighted = ShoppingItem(
            id = "2",
            listId = "list-1",
            quantity = 0.345, // 345g
            priceAtTime = 4990L, // R$ 49,90/kg -> 0.345 * 4990 = 1721.55 -> 1722L
            updatedAt = 1000L
        )
        assertEquals(1722L, itemWeighted.subtotalInCents)

        val itemNoPrice = ShoppingItem(
            id = "3",
            listId = "list-1",
            quantity = 2.0,
            priceAtTime = null,
            updatedAt = 1000L
        )
        assertNull(itemNoPrice.subtotalInCents)
    }

    @Test
    fun `ShoppingList metrics and budget calculations should behave accurately`() {
        val item1 = ShoppingItem(
            id = "1",
            listId = "list-1",
            quantity = 2.0,
            priceAtTime = 500L,
            isChecked = true,
            updatedAt = 1000L
        )
        val item2 = ShoppingItem(
            id = "2",
            listId = "list-1",
            quantity = 1.0,
            priceAtTime = 1500L,
            isChecked = true,
            updatedAt = 1000L
        )
        val itemUnchecked = ShoppingItem(
            id = "3",
            listId = "list-1",
            quantity = 1.0,
            priceAtTime = 2000L,
            isChecked = false,
            updatedAt = 1000L
        )
        val itemDeleted = ShoppingItem(
            id = "4",
            listId = "list-1",
            quantity = 5.0,
            priceAtTime = 1000L,
            isChecked = true,
            isDeleted = true,
            updatedAt = 1000L
        )

        val list = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.PLANNED,
            budgetInCents = 2000L, // R$ 20,00 de orçamento
            items = listOf(item1, item2, itemUnchecked, itemDeleted),
            updatedAt = 1000L
        )

        assertEquals(2, list.totalCheckedItems)
        assertEquals(3, list.totalActiveItems)
        assertEquals(2500L, list.totalCartInCents) // 1000 + 1500
        assertTrue(list.isOverBudget) // 2500L > 2000L
    }

    @Test
    fun `CatalogProduct default values and serialization should be consistent`() {
        val product = CatalogProduct(
            id = "prod-1",
            name = "Feijão Preto",
            updatedAt = 123456L
        )

        assertEquals(MeasureUnit.UNIT, product.measureUnit)
        assertEquals(1.0, product.netWeight)
        assertFalse(product.isDeleted)
        assertTrue(product.manuallyAdded)

        val serialized = json.encodeToString(product)
        val deserialized = json.decodeFromString<CatalogProduct>(serialized)

        assertEquals(product, deserialized)
    }

    @Test
    fun `PriceEntry serialization roundtrip maintains Long cent precision`() {
        val entry = PriceEntry(
            id = "price-1",
            productId = "prod-1",
            priceInCents = 2599L,
            storeName = "Supermercado Central",
            updatedAt = 123456L
        )

        val serialized = json.encodeToString(entry)
        val deserialized = json.decodeFromString<PriceEntry>(serialized)

        assertEquals(entry, deserialized)
        assertEquals(2599L, deserialized.priceInCents)
    }
}
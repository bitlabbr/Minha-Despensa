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

package com.bitlabbr.minhadespensa.core.domain

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemConsumption
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
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
            priceAtTime = 1000L, // 1.5 * 1000 = 1500L
            updatedAt = 1000L
        )
        assertEquals(1500L, itemFractional.subtotalInCents)

        val itemWeighted = ShoppingItem(
            id = "2",
            listId = "list-1",
            quantity = 0.345, // 345g
            priceAtTime = 4990L, // 0.345 * 4990 = 1721.55 -> rounds to 1722L
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

        val itemZeroPrice = ShoppingItem(
            id = "4",
            listId = "list-1",
            quantity = 2.0,
            priceAtTime = 0L,
            updatedAt = 1000L
        )
        assertEquals(0L, itemZeroPrice.subtotalInCents)
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
            budgetInCents = 2000L, // 2000 cents budget
            items = listOf(item1, item2, itemUnchecked, itemDeleted),
            updatedAt = 1000L
        )

        assertEquals(2, list.totalCheckedItems)
        assertEquals(3, list.totalActiveItems)
        assertEquals(2500L, list.totalCartInCents) // (2.0 * 500) + (1.0 * 1500) = 2500L
        assertTrue(list.isOverBudget) // 2500L > 2000L
    }

    @Test
    fun `ShoppingList isOverBudget should be false when budget is null or total equals budget`() {
        val item = ShoppingItem(
            id = "1",
            listId = "list-1",
            quantity = 1.0,
            priceAtTime = 2000L,
            isChecked = true,
            updatedAt = 1000L
        )

        val listNoBudget = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.PLANNED,
            budgetInCents = null,
            items = listOf(item),
            updatedAt = 1000L
        )
        assertFalse(listNoBudget.isOverBudget)

        val listExactBudget = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.PLANNED,
            budgetInCents = 2000L,
            items = listOf(item),
            updatedAt = 1000L
        )
        assertFalse(listExactBudget.isOverBudget)

        val emptyList = ShoppingList(
            id = "list-empty",
            name = "Vazia",
            type = ShoppingListType.PLANNED,
            budgetInCents = 1000L,
            items = emptyList(),
            updatedAt = 1000L
        )
        assertEquals(0, emptyList.totalCheckedItems)
        assertEquals(0, emptyList.totalActiveItems)
        assertEquals(0L, emptyList.totalCartInCents)
        assertFalse(emptyList.isOverBudget)
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

    @Test
    fun `PantryItem serialization and properties should be consistent`() {
        val pantryItem = PantryItem(
            id = "pantry-1",
            productId = "prod-1",
            quantity = 4.0,
            expirationDate = 1700000000L,
            batchNumber = "LOTE-A",
            updatedAt = 123456L,
            isDeleted = false,
        )

        val serialized = json.encodeToString(pantryItem)
        val deserialized = json.decodeFromString<PantryItem>(serialized)

        assertEquals(pantryItem, deserialized)
        assertEquals("LOTE-A", deserialized.batchNumber)
        assertEquals(4.0, deserialized.quantity)
    }

    @Test
    fun `Pantry auxiliary models should instantiate correctly`() {
        val item = PantryItem(
            id = "pantry-1",
            productId = "prod-1",
            quantity = 2.0,
            expirationDate = null,
            updatedAt = 1000L,
        )

        val joined = PantryItemWithCategory(
            pantryItem = item,
            category = "Grãos",
            name = "Feijão Carioca",
        )
        assertEquals("Grãos", joined.category)
        assertEquals("Feijão Carioca", joined.name)

        val consumption = PantryItemConsumption(
            pantryItemId = "pantry-1",
            quantityToConsume = 0.5,
        )
        assertEquals("pantry-1", consumption.pantryItemId)
        assertEquals(0.5, consumption.quantityToConsume)
    }

    @Test
    fun `ShoppingList totalCartInCents should gracefully ignore checked items with null price`() {
        val itemPriced = ShoppingItem(
            id = "1",
            listId = "list-1",
            quantity = 2.0,
            priceAtTime = 300L,
            isChecked = true,
            updatedAt = 1000L,
        )
        val itemNullPrice = ShoppingItem(
            id = "2",
            listId = "list-1",
            quantity = 1.0,
            priceAtTime = null,
            isChecked = true,
            updatedAt = 1000L,
        )

        val list = ShoppingList(
            id = "list-1",
            name = "Compras",
            type = ShoppingListType.PLANNED,
            items = listOf(itemPriced, itemNullPrice),
            updatedAt = 1000L,
        )

        assertEquals(2, list.totalCheckedItems)
        assertEquals(600L, list.totalCartInCents) // ignores itemNullPrice without crashing
    }

    @Test
    fun `isValidTimestamp should validate realistic timestamps correctly`() {
        assertFalse(isValidTimestamp(0L))
        assertFalse(isValidTimestamp(-100L))
        assertFalse(isValidTimestamp(946684799000L)) // 1999-12-31 23:59:59 (before 2000-01-01)
        assertTrue(isValidTimestamp(getCurrentTime()))
        assertFalse(isValidTimestamp(getCurrentTime() + 10_000_000_000L)) // far future
    }
}
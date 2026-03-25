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

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomShoppingRepository
import com.bitlabbr.minhadespensa.data.repository.toEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RoomShoppingRepositoryTest : BaseTest() {
    private lateinit var db: AppDatabase
    private lateinit var shoppingRepository: RoomShoppingRepository
    private lateinit var catalogRepository: RoomCatalogRepository
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        shoppingRepository = RoomShoppingRepository(db, logger)
        catalogRepository = RoomCatalogRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    @Ignore
    fun `finalizePurchase should move checked items to pantry and create price entries`() = runTest {
        val productId = Uuid.random().toString()
        val now = getCurrentTime()
        val productEan = "7891234567890"

        db.catalogDao().insert(
            CatalogProduct(
                id = productId,
                name = "Leite integral",
                brand = "Betânia",
                measureUnit = MeasureUnit.LITER,
                netWeight = 1.0,
                updatedAt = now,
                isDeleted = false,
                manuallyAdded = true,
                ean = productEan,
                thumbnailUrl = null
            ).toEntity()
        )

        val shoppingItemId = Uuid.random().toString()
        val shoppingItem = ShoppingItem(
            id = shoppingItemId, productId = productId, quantity = 8.0,
            priceAtTime = 548, isChecked = false, updatedAt = now
        )
        shoppingRepository.insertShoppingItem(shoppingItem)
        shoppingRepository.toggleCheck(shoppingItemId, true)
        shoppingRepository.finalizePurchase()

        shoppingRepository.getActiveShoppingList().test {
            val list = awaitItem()
            assertTrue(list.isEmpty(), "O carrinho deveria estar vazio após o checkout")
        }

        db.pantryDao().getAllActive().test {
            val pantryItems = awaitItem()
            assertEquals(1, pantryItems.size)
            assertEquals(productId, pantryItems[0].productId)
            assertEquals(8.0, pantryItems[0].quantity)
        }

        db.priceDao().getHistoryByProduct(productId).test {
            val prices = awaitItem()
            assertEquals(1, prices.size)
            assertEquals(548, prices[0].priceInCents)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `SAVE a shopping item `() = runTest {
        val product = createDummyCatalogProduct(
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0
        )

        val shoppingItem = createDummyShoppingItem(
            productId = product.id,
            quantity = 8.0
        )

        catalogRepository.insertProduct(product, null)
        shoppingRepository.insertShoppingItem(shoppingItem)
        shoppingRepository.getActiveShoppingList().test {
            val list = awaitItem()
            assertEquals(list.size, 1, "Should have only one item")
            assertEquals(list[0].productId, product.id, "Product ID should be tha same")
            assertEquals(list[0].quantity, shoppingItem.quantity, "Quantity should be tha same")
            assertEquals(list[0].priceAtTime, shoppingItem.priceAtTime, "Price at time should be the same")
            assertEquals(list[0].isChecked, shoppingItem.isChecked, "Is checked should be tha same")
            assertEquals(list[0].isDeleted, shoppingItem.isDeleted, "Is deleted should be the same")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyShoppingItem(
        id: String = Uuid.random().toString(),
        productId: String = Uuid.random().toString(),
        quantity: Double,
        priceAtTime: Long? = null,
        isChecked: Boolean = false,
        updatedAt: Long = getCurrentTime(),
        isDeleted: Boolean = false
    ): ShoppingItem {
        return ShoppingItem(
            id = id,
            productId = productId,
            quantity = quantity,
            updatedAt = updatedAt,
            isChecked = isChecked,
            priceAtTime = priceAtTime,
            isDeleted = isDeleted,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDummyCatalogProduct(
        id: String = Uuid.random().toString(),
        name: String = "Name",
        ean: String? = null,
        brand: String? = null,
        measureUnit: MeasureUnit,
        netWeight: Double,
        thumbnailUrl: String? = null,
        updatedAt: Long = getCurrentTime(),
        manuallyAdded: Boolean = true,
        isDelete: Boolean = false

    ): CatalogProduct {
        return CatalogProduct(
            id = id,
            name = name,
            measureUnit = measureUnit,
            netWeight = netWeight,
            updatedAt = updatedAt,
            ean = ean,
            brand = brand,
            isDeleted = isDelete,
            manuallyAdded = manuallyAdded,
            thumbnailUrl = thumbnailUrl,
        )
    }

    @Test
    fun `should UPDATE quantity of an existing shopping item`() = runTest {
        val product = createDummyCatalogProduct(
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0
        )

        val initialItem = createDummyShoppingItem(productId = product.id, quantity = 1.0)

        catalogRepository.insertProduct(product, null)
        shoppingRepository.insertShoppingItem(initialItem)

        val updatedItem = initialItem.copy(quantity = 5.5, updatedAt = getCurrentTime())
        shoppingRepository.updateItem(updatedItem)

        shoppingRepository.getActiveShoppingList().test {
            val list = awaitItem()
            assertEquals(1, list.size, "Should have only 1 item")
            assertEquals(list[0].productId, product.id, "Product ID should be tha same")
            assertEquals(list[0].quantity, updatedItem.quantity, "Quantity should be tha same")
            assertEquals(list[0].priceAtTime, initialItem.priceAtTime, "Price at time should be the same")
            assertEquals(list[0].isChecked, initialItem.isChecked, "Is checked should be tha same")
            assertEquals(list[0].isDeleted, initialItem.isDeleted, "Is deleted should be the same")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should TOGGLE isChecked status successfully`() = runTest {
        val product = createDummyCatalogProduct(
            measureUnit = MeasureUnit.LITER,
            netWeight = 1.0
        )
        val item = createDummyShoppingItem(productId = product.id, isChecked = false, quantity = 10.0)

        catalogRepository.insertProduct(product, null)
        shoppingRepository.insertShoppingItem(item)

        shoppingRepository.toggleCheck(item.id, true)

        shoppingRepository.getActiveShoppingList().test {
            val list = awaitItem()
            assertTrue(list[0].isChecked, "O item deveria estar marcado")
            cancelAndIgnoreRemainingEvents()
        }
    }

}
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RoomShoppingRepositoryTest: BaseTest() {
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
        // the user add the shoppItem to the shopping list
        shoppingRepository.saveItem(shoppingItem)
        // the user effectively buy the products
        shoppingRepository.toggleCheck(shoppingItemId, true)
        // update the bought item into pantry
        shoppingRepository.finalizePurchase()

        // THEN: O carrinho deve estar vazio (via Turbine)
        shoppingRepository.getActiveShoppingList().test {
            val list = awaitItem()
            assertTrue(list.isEmpty(), "O carrinho deveria estar vazio após o checkout")
        }

        // THEN: O item deve estar na despensa
        db.pantryDao().getAllActive().test {
            val pantryItems = awaitItem()
            assertEquals(1, pantryItems.size)
            assertEquals(productId, pantryItems[0].productId)
            assertEquals(8.0, pantryItems[0].quantity)
        }

        // THEN: O histórico de preço deve ter sido criado
        db.priceDao().getHistoryByProduct(productId).test {
            val prices = awaitItem()
            assertEquals(1, prices.size)
            assertEquals(548, prices[0].priceInCents)
        }
    }

}
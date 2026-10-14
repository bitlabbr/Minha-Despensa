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
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PantryMovementDaoTest : BaseTest() {

    private lateinit var db: AppDatabase

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun insertCatalogProduct(
        id: String,
        ean: String = "7891234567890",
        name: String = "Arroz Tio João",
        brand: String = "Tio João",
        measureUnit: String = "KG",
        netWeight: Double = 1.0,
    ) {
        db.catalogDao().insert(
            CatalogProductEntity(
                id = id,
                ean = ean,
                name = name,
                category = "Grãos",
                brand = brand,
                measureUnit = measureUnit,
                netWeight = netWeight,
                thumbnailUrl = null,
                updatedAt = 1000L,
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
            )
        )
    }

    @Test
    fun `insertMovement and getMovementsForPeriod should filter correctly by timestamp`() = runTest {
        val prodId = Uuid.random().toString()
        insertCatalogProduct(prodId)

        val mov1 = PantryMovementEntity(
            id = Uuid.random().toString(),
            productId = prodId,
            type = "ENTRY_MANUAL",
            quantity = 2.0,
            unitPriceInCents = 1000L,
            totalPriceInCents = 2000L,
            timestamp = 1000L,
        )
        val mov2 = PantryMovementEntity(
            id = Uuid.random().toString(),
            productId = prodId,
            type = "CONSUMPTION",
            quantity = 1.0,
            timestamp = 2000L,
        )
        val mov3 = PantryMovementEntity(
            id = Uuid.random().toString(),
            productId = prodId,
            type = "ENTRY_PURCHASE",
            quantity = 5.0,
            unitPriceInCents = 800L,
            totalPriceInCents = 4000L,
            timestamp = 3000L,
        )

        db.movementDao().insertMovements(listOf(mov1, mov2, mov3))

        val periodResult = db.movementDao().getMovementsForPeriod(1500L, 2500L).first()
        assertEquals(1, periodResult.size)
        assertEquals(mov2.id, periodResult[0].id)

        val allPeriodResult = db.movementDao().getMovementsForPeriod(500L, 3500L).first()
        assertEquals(3, allPeriodResult.size)
        // Ordered by timestamp DESC
        assertEquals(mov3.id, allPeriodResult[0].id)
        assertEquals(mov2.id, allPeriodResult[1].id)
        assertEquals(mov1.id, allPeriodResult[2].id)
    }

    @Test
    fun `getMostConsumedProducts should aggregate only CONSUMPTION movements within period`() = runTest {
        val prod1 = Uuid.random().toString()
        val prod2 = Uuid.random().toString()
        insertCatalogProduct(prod1, ean = "1111111111111", name = "Feijão")
        insertCatalogProduct(prod2, ean = "2222222222222", name = "Arroz")

        // Prod 1 consumptions
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prod1,
                type = "CONSUMPTION",
                quantity = 3.0,
                timestamp = 1000L,
            )
        )
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prod1,
                type = "CONSUMPTION",
                quantity = 2.0,
                timestamp = 2000L,
            )
        )

        // Prod 2 consumption
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prod2,
                type = "CONSUMPTION",
                quantity = 10.0,
                timestamp = 1500L,
            )
        )

        // Non-consumption entries should be ignored
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prod1,
                type = "ENTRY_MANUAL",
                quantity = 50.0,
                totalPriceInCents = 10000L,
                timestamp = 1200L,
            )
        )

        val mostConsumed = db.movementDao().getMostConsumedProducts(500L, 3000L, limit = 5).first()
        assertEquals(2, mostConsumed.size)

        // Prod 2 had 10.0 consumed, so it ranks 1st
        assertEquals(prod2, mostConsumed[0].productId)
        assertEquals(10.0, mostConsumed[0].totalConsumed)

        // Prod 1 had 3.0 + 2.0 = 5.0 consumed, so it ranks 2nd
        assertEquals(prod1, mostConsumed[1].productId)
        assertEquals(5.0, mostConsumed[1].totalConsumed)
    }

    @Test
    fun `getMonthlyExpenses should sum only ENTRY_MANUAL and ENTRY_PURCHASE and emit reactively`() = runTest {
        val prodId = Uuid.random().toString()
        insertCatalogProduct(prodId)

        db.movementDao().getMonthlyExpenses(1000L, 5000L).test {
            // Initial state: 0 expenses
            assertEquals(0L, awaitItem())

            // Add manual pantry entry of R$ 35,00
            db.movementDao().insertMovement(
                PantryMovementEntity(
                    id = Uuid.random().toString(),
                    productId = prodId,
                    type = "ENTRY_MANUAL",
                    quantity = 2.0,
                    unitPriceInCents = 1750L,
                    totalPriceInCents = 3500L,
                    timestamp = 2000L,
                )
            )
            assertEquals(3500L, awaitItem())

            // Add purchase entry from list of R$ 50,00
            db.movementDao().insertMovement(
                PantryMovementEntity(
                    id = Uuid.random().toString(),
                    productId = prodId,
                    type = "ENTRY_PURCHASE",
                    quantity = 1.0,
                    unitPriceInCents = 5000L,
                    totalPriceInCents = 5000L,
                    timestamp = 3000L,
                )
            )
            assertEquals(8500L, awaitItem())

            // Add consumption: must NOT reduce expenses!
            db.movementDao().insertMovement(
                PantryMovementEntity(
                    id = Uuid.random().toString(),
                    productId = prodId,
                    type = "CONSUMPTION",
                    quantity = 2.0,
                    timestamp = 3500L,
                )
            )
            // No new emission of different expense value, query still evaluates to 8500
            assertEquals(8500L, db.movementDao().findMonthlyExpenses(1000L, 5000L))

            // Add soft-deleted entry: must be ignored
            db.movementDao().insertMovement(
                PantryMovementEntity(
                    id = Uuid.random().toString(),
                    productId = prodId,
                    type = "ENTRY_MANUAL",
                    quantity = 1.0,
                    unitPriceInCents = 2000L,
                    totalPriceInCents = 2000L,
                    timestamp = 4000L,
                    isDeleted = true,
                )
            )
            assertEquals(8500L, db.movementDao().findMonthlyExpenses(1000L, 5000L))

            cancelAndIgnoreRemainingEvents()
        }
    }
}

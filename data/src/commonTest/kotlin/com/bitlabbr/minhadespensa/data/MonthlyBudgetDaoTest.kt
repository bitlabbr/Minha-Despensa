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
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.util.getMonthTimeRange
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MonthlyBudgetDaoTest : BaseTest() {

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

    @Test
    fun insertOrUpdate_and_getBudget_should_insert_and_update_budget() = runTest {
        val dao = db.budgetDao()

        // 1. Initially null
        assertNull(dao.findBudget(2026, 10))

        // 2. Insert budget
        val budget1 = MonthlyBudgetEntity(
            year = 2026,
            month = 10,
            budgetInCents = 150000L,
            updatedAt = 1000L
        )
        dao.insertOrUpdate(budget1)

        val retrieved1 = dao.getBudget(2026, 10).first()
        assertEquals(budget1, retrieved1)

        // 3. Update budget for same year and month
        val budget2 = MonthlyBudgetEntity(
            year = 2026,
            month = 10,
            budgetInCents = 200000L,
            updatedAt = 2000L
        )
        dao.insertOrUpdate(budget2)

        val retrieved2 = dao.getBudget(2026, 10).first()
        assertEquals(budget2, retrieved2)
        assertEquals(200000L, retrieved2?.budgetInCents)
    }

    @Test
    fun getBudgetsForYear_should_return_all_budgets_for_year_ordered_by_month_asc() = runTest {
        val dao = db.budgetDao()

        dao.insertOrUpdate(MonthlyBudgetEntity(2026, 10, 150000L, 1000L))
        dao.insertOrUpdate(MonthlyBudgetEntity(2026, 1, 120000L, 1000L))
        dao.insertOrUpdate(MonthlyBudgetEntity(2026, 5, 130000L, 1000L))
        dao.insertOrUpdate(MonthlyBudgetEntity(2025, 12, 90000L, 1000L))

        val budgets2026 = dao.getBudgetsForYear(2026).first()
        assertEquals(3, budgets2026.size)
        assertEquals(1, budgets2026[0].month)
        assertEquals(5, budgets2026[1].month)
        assertEquals(10, budgets2026[2].month)

        val budgets2025 = dao.getBudgetsForYear(2025).first()
        assertEquals(1, budgets2025.size)
        assertEquals(12, budgets2025[0].month)
    }

    @Test
    fun getMonthlyExpenses_should_return_zero_when_database_is_empty() = runTest {
        val (start, end) = getMonthTimeRange(2026, 10)
        val expenses = db.budgetDao().findMonthlyExpenses(start, end)
        assertEquals(0L, expenses)
    }

    @Test
    fun getMonthlyExpenses_should_aggregate_completed_shopping_lists_and_direct_pantry_entries() = runTest {
        val (startOfOct, endOfOct) = getMonthTimeRange(2026, 10)
        val midOct = startOfOct + 86400000L * 10 // 10 days in Oct

        // 1. Create a catalog product
        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Café Especial",
                category = "Bebidas",
                brand = "Melitta",
                measureUnit = "UNIT",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = midOct,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        // 2. Insert purchase movement from shopping list checkout: 3000 + 1500 = 4500 cents
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 2.0,
                unitPriceInCents = 1500L,
                totalPriceInCents = 3000L,
                updatedAt = midOct,
                storeOrReason = "Compra: Compras do Mês",
                isDeleted = false,
            )
        )
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 1.5,
                unitPriceInCents = 1000L,
                totalPriceInCents = 1500L,
                updatedAt = midOct,
                storeOrReason = "Compra: Compras do Mês",
                isDeleted = false,
            )
        )

        // 3. Insert direct pantry entry in Oct (storeName = 'Entrada na Despensa'): 2400 cents
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_MANUAL",
                quantity = 3.0,
                unitPriceInCents = 800L,
                totalPriceInCents = 2400L,
                updatedAt = midOct,
                storeOrReason = "Entrada na Despensa",
                isDeleted = false,
            )
        )

        // 4. Expected: Shopping list (3000 + 1500) + Pantry direct (2400) = 6900 cents
        val expenses = db.budgetDao().findMonthlyExpenses(startOfOct, endOfOct)
        assertEquals(6900L, expenses)
    }

    @Test
    fun getMonthlyExpenses_should_ignore_deleted_movements_non_purchase_types_and_other_months() = runTest {
        val (startOfOct, endOfOct) = getMonthTimeRange(2026, 10)
        val midOct = startOfOct + 86400000L * 5
        val (startOfNov, _) = getMonthTimeRange(2026, 11)
        val midNov = startOfNov + 86400000L * 2

        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Azeite",
                category = "Óleos",
                brand = "Galo",
                measureUnit = "UNIT",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = midOct,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        // 1. Consumption movement in Oct -> ignored for expenses
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prodId,
                type = "CONSUMPTION",
                quantity = 1.0,
                unitPriceInCents = 3000L,
                totalPriceInCents = null,
                updatedAt = midOct,
            )
        )

        // 2. Purchase movement in Nov -> ignored for Oct
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 1.0,
                unitPriceInCents = 4000L,
                totalPriceInCents = 4000L,
                updatedAt = midNov,
            )
        )

        // 3. Purchase movement in Oct with isDeleted = true -> ignored
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 1.0,
                unitPriceInCents = 2000L,
                totalPriceInCents = 2000L,
                updatedAt = midOct,
                isDeleted = true,
            )
        )

        // Verify expenses for October are 0
        val octExpenses = db.budgetDao().findMonthlyExpenses(startOfOct, endOfOct)
        assertEquals(0L, octExpenses)

        // Verify November has the 4000L
        val (startNovRange, endNovRange) = getMonthTimeRange(2026, 11)
        val novExpenses = db.budgetDao().findMonthlyExpenses(startNovRange, endNovRange)
        assertEquals(4000L, novExpenses)
    }

    @Test
    fun getMonthlyExpenses_flow_should_emit_updates_reactively() = runTest {
        val (start, end) = getMonthTimeRange(2026, 10)
        val timestamp = start + 1000L

        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Leite",
                category = "Laticínios",
                brand = "Italac",
                measureUnit = "LITER",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = timestamp,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        db.budgetDao().getMonthlyExpenses(start, end).test {
            // Initial emission
            assertEquals(0L, awaitItem())

            // Add pantry movement
            db.movementDao().insertMovement(
                PantryMovementEntity(
                    id = Uuid.random().toString(),
                    productId = prodId,
                    type = "ENTRY_MANUAL",
                    quantity = 2.0,
                    unitPriceInCents = 500L,
                    totalPriceInCents = 1000L,
                    updatedAt = timestamp,
                )
            )
            assertEquals(1000L, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}

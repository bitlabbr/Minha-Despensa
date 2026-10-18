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

import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getMonthTimeRange
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomBudgetRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class RoomBudgetRepositoryTest : BaseTest() {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomBudgetRepository
    private val logger = ConsoleLogger("RoomBudgetRepositoryTest")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
        repository = RoomBudgetRepository(db, logger)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // 1. Budget CRUD & Validation Tests
    // -------------------------------------------------------------------------

    @Test
    fun setBudgetForMonth_and_getBudgetForMonth_should_persist_and_retrieve_budget() = runTest {
        val initial = repository.getBudgetForMonth(2026, 10).first()
        assertNull(initial)

        repository.setBudgetForMonth(year = 2026, month = 10, budgetInCents = 150000L)

        val budget = repository.getBudgetForMonth(2026, 10).first()
        assertNotNull(budget)
        assertEquals(2026, budget.year)
        assertEquals(10, budget.month)
        assertEquals(150000L, budget.budgetInCents)

        // Update
        repository.setBudgetForMonth(year = 2026, month = 10, budgetInCents = 250000L)
        val updated = repository.getBudgetForMonth(2026, 10).first()
        assertNotNull(updated)
        assertEquals(250000L, updated.budgetInCents)
    }

    @Test
    fun setBudgetForMonth_should_fail_for_negative_budget() = runTest {
        assertFailsWith<IllegalArgumentException> {
            repository.setBudgetForMonth(year = 2026, month = 10, budgetInCents = -1L)
        }
    }

    @Test
    fun setBudgetForMonth_should_fail_for_invalid_year_or_month() = runTest {
        assertFailsWith<IllegalArgumentException> {
            repository.setBudgetForMonth(year = 1999, month = 10, budgetInCents = 1000L)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.setBudgetForMonth(year = 2026, month = 0, budgetInCents = 1000L)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.setBudgetForMonth(year = 2026, month = 13, budgetInCents = 1000L)
        }
    }

    @Test
    fun getBudgetForMonth_should_fail_for_invalid_year_or_month() {
        assertFailsWith<IllegalArgumentException> {
            repository.getBudgetForMonth(year = 1999, month = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getBudgetForMonth(year = 2026, month = 0)
        }
    }

    // -------------------------------------------------------------------------
    // 2. Monthly Expenses Aggregation Tests
    // -------------------------------------------------------------------------

    @Test
    fun getMonthlyExpenses_should_aggregate_expenses_from_completed_lists_and_pantry() = runTest {
        val (startOfOct, _) = getMonthTimeRange(2026, 10)
        val midOct = startOfOct + 86400000L * 5

        // Create catalog product
        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Sabão Líquido",
                category = "Limpeza",
                brand = "Omo",
                measureUnit = "LITER",
                netWeight = 3.0,
                thumbnailUrl = null,
                updatedAt = midOct,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        // 1. Completed Shopping List: 2 * 3500 = 7000 cents
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 2.0,
                unitPriceInCents = 3500L,
                totalPriceInCents = 7000L,
                updatedAt = midOct,
                storeOrReason = "Compra: Compras Limpeza",
                isDeleted = false,
            )
        )

        // 2. Direct Pantry Entry: 1 * 4000 = 4000 cents
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_MANUAL",
                quantity = 1.0,
                unitPriceInCents = 4000L,
                totalPriceInCents = 4000L,
                updatedAt = midOct,
                storeOrReason = "Entrada na Despensa",
                isDeleted = false,
            )
        )

        val totalExpenses = repository.getMonthlyExpenses(2026, 10).first()
        assertEquals(11000L, totalExpenses) // 7000 + 4000 = 11000
    }

    // -------------------------------------------------------------------------
    // 3. Yearly Summary Tests (12 Months Array)
    // -------------------------------------------------------------------------

    @Test
    fun getYearlySummary_should_return_all_12_months_with_budgets_and_expenses() = runTest {
        val (startOfFeb, _) = getMonthTimeRange(2026, 2)
        val (startOfOct, _) = getMonthTimeRange(2026, 10)

        // Setup product
        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Azeite de Oliva",
                category = "Mercearia",
                brand = "Borges",
                measureUnit = "MILLILITER",
                netWeight = 500.0,
                thumbnailUrl = null,
                updatedAt = startOfFeb,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        // 1. Budget for February and October
        repository.setBudgetForMonth(year = 2026, month = 2, budgetInCents = 100000L) // R$ 1000
        repository.setBudgetForMonth(year = 2026, month = 10, budgetInCents = 200000L) // R$ 2000

        // 2. Expense for February: 120000L (Over budget!)
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_PURCHASE",
                quantity = 3.0,
                unitPriceInCents = 40000L,
                totalPriceInCents = 120000L,
                updatedAt = startOfFeb + 1000L,
                storeOrReason = "Compra: Compras Fev",
                isDeleted = false,
            )
        )

        // 3. Expense for October: 50000L (Under budget)
        db.movementDao().insertMovement(
            PantryMovementEntity(
                id = Uuid.random().toString(),
                pantryItemId = Uuid.random().toString(),
                productId = prodId,
                type = "ENTRY_MANUAL",
                quantity = 1.0,
                unitPriceInCents = 50000L,
                totalPriceInCents = 50000L,
                updatedAt = startOfOct + 1000L,
                storeOrReason = "Entrada na Despensa",
                isDeleted = false,
            )
        )

        val yearlySummary = repository.getYearlySummary(2026).first()
        assertEquals(12, yearlySummary.size)

        // Month 1 (January): No budget, no expenses
        val jan = yearlySummary[0]
        assertEquals(1, jan.month)
        assertEquals(2026, jan.year)
        assertNull(jan.budgetInCents)
        assertEquals(0L, jan.totalSpentInCents)
        assertNull(jan.remainingInCents)
        assertEquals(0f, jan.consumptionProgress)
        assertFalse(jan.isOverBudget)

        // Month 2 (February): Budget 100000L, Spent 120000L -> Over budget!
        val feb = yearlySummary[1]
        assertEquals(2, feb.month)
        assertEquals(100000L, feb.budgetInCents)
        assertEquals(120000L, feb.totalSpentInCents)
        assertEquals(-20000L, feb.remainingInCents)
        assertEquals(1.2f, feb.consumptionProgress, 0.001f)
        assertTrue(feb.isOverBudget)

        // Month 10 (October): Budget 200000L, Spent 50000L -> Under budget
        val oct = yearlySummary[9]
        assertEquals(10, oct.month)
        assertEquals(200000L, oct.budgetInCents)
        assertEquals(50000L, oct.totalSpentInCents)
        assertEquals(150000L, oct.remainingInCents)
        assertEquals(0.25f, oct.consumptionProgress, 0.001f)
        assertFalse(oct.isOverBudget)
    }

    @Test
    fun getYearlySummary_should_fail_for_invalid_year() {
        assertFailsWith<IllegalArgumentException> {
            repository.getYearlySummary(1999)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getYearlySummary(2101)
        }
    }

    @Test
    fun `getMonthlyExpenses should remain immutable when pantry inventory is consumed`() = runTest {
        val (startOfOct, _) = getMonthTimeRange(2026, 10)
        val midOct = startOfOct + 86400000L * 5

        val prodId = Uuid.random().toString()
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = null,
                name = "Arroz Integral",
                category = "Grãos",
                brand = "Camil",
                measureUnit = "KILOGRAM",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = midOct,
                isDeleted = false,
                manuallyAdded = true,
                notes = null
            )
        )

        val pantryRepo = RoomPantryRepository(db, logger)
        val pantryItemId = Uuid.random().toString()

        // 1. User buys 10 kg of Rice for R$ 5,00 / kg = R$ 50,00 total spent (5000 cents)
        pantryRepo.insertPantryItem(
            PantryItem(
                id = pantryItemId,
                productId = prodId,
                quantity = 10.0,
                expirationDate = null,
                batchNumber = "LOTE-IMMUTABLE",
                unitPriceInCents = 500L,
                updatedAt = midOct,
                isDeleted = false,
            )
        )

        // Expense should be R$ 50,00
        assertEquals(5000L, repository.getMonthlyExpenses(2026, 10).first())

        // 2. Family consumes 6 kg of Rice (remaining stock = 4 kg)
        pantryRepo.consumePantryItem(pantryItemId, 6.0)

        // Expense must REMAIN R$ 50,00! (Stock consumption must NOT decrease historical expenses)
        assertEquals(5000L, repository.getMonthlyExpenses(2026, 10).first())

        // 3. Family consumes the remaining 4 kg of Rice (stock = 0 kg)
        pantryRepo.consumePantryItem(pantryItemId, 4.0)

        // Expense must STILL remain R$ 50,00!
        assertEquals(5000L, repository.getMonthlyExpenses(2026, 10).first())
    }
}

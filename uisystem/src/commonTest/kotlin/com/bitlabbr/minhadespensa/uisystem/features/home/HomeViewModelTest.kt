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

package com.bitlabbr.minhadespensa.uisystem.features.home

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.MonthlyBudget
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMonthlyFinancialSummaryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMostConsumedProductsUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SetMonthlyBudgetUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeBudgetRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakePantryMovementRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakePantryRepository
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.home_expiring_in_days
import minhadespensa.uisystem.generated.resources.home_expiring_no_date
import minhadespensa.uisystem.generated.resources.home_expiring_overdue_1
import minhadespensa.uisystem.generated.resources.home_expiring_overdue_days
import minhadespensa.uisystem.generated.resources.home_expiring_today
import minhadespensa.uisystem.generated.resources.home_expiring_tomorrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedInstant = Instant.parse("2026-10-14T12:00:00Z")
    private val fixedClock = object : Clock {
        override fun now(): Instant = fixedInstant
    }
    private val fixedTimeZone = TimeZone.UTC

    private lateinit var budgetRepository: FakeBudgetRepository
    private lateinit var pantryMovementRepository: FakePantryMovementRepository
    private lateinit var pantryRepository: FakePantryRepository
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var logger: FakeAppLogger

    private lateinit var getMonthlyFinancialSummaryUseCase: GetMonthlyFinancialSummaryUseCase
    private lateinit var setMonthlyBudgetUseCase: SetMonthlyBudgetUseCase
    private lateinit var getMostConsumedProductsUseCase: GetMostConsumedProductsUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        budgetRepository = FakeBudgetRepository()
        pantryMovementRepository = FakePantryMovementRepository()
        catalogRepository = FakeCatalogRepository()
        pantryRepository = FakePantryRepository(catalogRepository)
        notificationManager = AppNotificationManager()
        logger = FakeAppLogger()

        getMonthlyFinancialSummaryUseCase = GetMonthlyFinancialSummaryUseCase(budgetRepository)
        setMonthlyBudgetUseCase = SetMonthlyBudgetUseCase(budgetRepository)
        getMostConsumedProductsUseCase = GetMostConsumedProductsUseCase(pantryMovementRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getMonthlyFinancialSummaryUseCase = getMonthlyFinancialSummaryUseCase,
            setMonthlyBudgetUseCase = setMonthlyBudgetUseCase,
            getMostConsumedProductsUseCase = getMostConsumedProductsUseCase,
            pantryRepository = pantryRepository,
            catalogRepository = catalogRepository,
            logger = logger,
            notificationManager = notificationManager,
            clock = fixedClock,
            timeZone = fixedTimeZone,
        )
    }

    @Test
    fun `initial state emits with empty or default values and loading false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.financialSummary.hasBudget)
        assertEquals("R$ 0,00", state.financialSummary.currentConsumption)
        assertTrue(state.expiringItems.isEmpty())
        assertTrue(state.consumptionTrends.isEmpty())
        assertFalse(state.isSetBudgetDialogOpen)
    }

    @Test
    fun `financial summary reflects budget and expenses correctly`() = runTest(testDispatcher) {
        // Set budget: R$ 1.000,00 (100000 cents) for 2026/10
        budgetRepository.budgets.value = mapOf(
            Pair(2026, 10) to MonthlyBudget(
                year = 2026,
                month = 10,
                budgetInCents = 100000L,
                updatedAt = 0L,
            )
        )
        // Set expenses: R$ 420,50 (42050 cents)
        budgetRepository.expenses.value = mapOf(
            Pair(2026, 10) to 42050L
        )

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.financialSummary.hasBudget)
        assertEquals(100000L, state.financialSummary.budgetInCents)
        assertEquals(42050L, state.financialSummary.totalSpentInCents)
        assertEquals("R$ 420,50", state.financialSummary.currentConsumption)
        assertEquals("R$ 1000,00", state.financialSummary.budget)
        assertEquals(0.4205f, state.financialSummary.consumptionProgress, 0.001f)
    }

    @Test
    fun `financial summary handles overbudget scenario`() = runTest(testDispatcher) {
        // Set budget: R$ 200,00 (20000 cents)
        budgetRepository.budgets.value = mapOf(
            Pair(2026, 10) to MonthlyBudget(
                year = 2026,
                month = 10,
                budgetInCents = 20000L,
                updatedAt = 0L,
            )
        )
        // Expenses: R$ 300,00 (30000 cents) -> 1.5x overbudget
        budgetRepository.expenses.value = mapOf(
            Pair(2026, 10) to 30000L
        )

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.financialSummary.hasBudget)
        assertEquals(1.5f, state.financialSummary.consumptionProgress, 0.001f)
    }

    @Test
    fun `expiring items dynamic label calculations for today, tomorrow, future, and past`() = runTest(testDispatcher) {
        // Base date: 2026-10-14
        val todayMillis = LocalDate(2026, 10, 14).atStartOfDayIn(fixedTimeZone).toEpochMilliseconds()
        val tomorrowMillis = LocalDate(2026, 10, 15).atStartOfDayIn(fixedTimeZone).toEpochMilliseconds()
        val in3DaysMillis = LocalDate(2026, 10, 17).atStartOfDayIn(fixedTimeZone).toEpochMilliseconds()
        val expired1DayMillis = LocalDate(2026, 10, 13).atStartOfDayIn(fixedTimeZone).toEpochMilliseconds()
        val expired3DaysMillis = LocalDate(2026, 10, 11).atStartOfDayIn(fixedTimeZone).toEpochMilliseconds()

        val expiringList = listOf(
            PantryItemWithCategory(
                pantryItem = PantryItem(id = "item-1", productId = "prod-1", quantity = 1.0, expirationDate = todayMillis, updatedAt = 0L),
                name = "Leite Integral",
                category = "Laticínios",
            ),
            PantryItemWithCategory(
                pantryItem = PantryItem(id = "item-2", productId = "prod-2", quantity = 2.0, expirationDate = tomorrowMillis, updatedAt = 0L),
                name = "Iogurte Natural",
                category = "Laticínios",
            ),
            PantryItemWithCategory(
                pantryItem = PantryItem(id = "item-3", productId = "prod-3", quantity = 1.0, expirationDate = in3DaysMillis, updatedAt = 0L),
                name = "Pão de Forma",
                category = "Padaria",
            ),
            PantryItemWithCategory(
                pantryItem = PantryItem(id = "item-4", productId = "prod-4", quantity = 1.0, expirationDate = expired1DayMillis, updatedAt = 0L),
                name = "Queijo Minas",
                category = "Laticínios",
            ),
            PantryItemWithCategory(
                pantryItem = PantryItem(id = "item-5", productId = "prod-5", quantity = 1.0, expirationDate = expired3DaysMillis, updatedAt = 0L),
                name = "Presunto Cozido",
                category = "Frios",
            ),
        )

        pantryRepository.customExpiringItems.value = expiringList

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.expiringItems.size)

        val item1 = state.expiringItems[0]
        assertEquals(UiText.Resource(Res.string.home_expiring_today), item1.expirationLabel)
        assertFalse(item1.isExpired)
        assertEquals("14/10", item1.anchoredTargetLabel)
        assertEquals(1.0f, item1.anchoredGaugeProgress, 0.001f)

        val item2 = state.expiringItems[1]
        assertEquals(UiText.Resource(Res.string.home_expiring_tomorrow), item2.expirationLabel)
        assertFalse(item2.isExpired)
        assertEquals("15/10", item2.anchoredTargetLabel)
        assertEquals(6f / 7f, item2.anchoredGaugeProgress, 0.001f)

        val item3 = state.expiringItems[2]
        assertEquals(UiText.Resource(Res.string.home_expiring_in_days, listOf(3)), item3.expirationLabel)
        assertFalse(item3.isExpired)
        assertEquals("17/10", item3.anchoredTargetLabel)
        assertEquals(4f / 7f, item3.anchoredGaugeProgress, 0.001f)

        val item4 = state.expiringItems[3]
        assertEquals(UiText.Resource(Res.string.home_expiring_overdue_1), item4.expirationLabel)
        assertTrue(item4.isExpired)
        assertEquals("13/10", item4.anchoredTargetLabel)
        assertEquals(8f / 7f, item4.anchoredGaugeProgress, 0.001f)

        val item5 = state.expiringItems[4]
        assertEquals(UiText.Resource(Res.string.home_expiring_overdue_days, listOf(3)), item5.expirationLabel)
        assertTrue(item5.isExpired)
        assertEquals("11/10", item5.anchoredTargetLabel)
        assertEquals(10f / 7f, item5.anchoredGaugeProgress, 0.001f)
    }

    @Test
    fun `consumption trends resolves metadata from catalog repository`() = runTest(testDispatcher) {
        // Register products in catalog
        val product1 = CatalogProduct(
            id = "prod-coffee",
            name = "Café Torrado",
            category = "Bebidas",
            measureUnit = MeasureUnit.PACKAGE,
            updatedAt = 0L,
            thumbnailUrl = "icon_coffee",
        )
        val product2 = CatalogProduct(
            id = "prod-rice",
            name = "Arroz Branco",
            category = "Grãos",
            measureUnit = MeasureUnit.KILOGRAM,
            updatedAt = 0L,
        )
        catalogRepository.products.value = mapOf(
            product1.id to product1,
            product2.id to product2,
        )

        // Set most consumed: 4 packages of coffee, 2.5 kg of rice
        pantryMovementRepository.customMostConsumed.value = listOf(
            Pair("prod-coffee", 4.0),
            Pair("prod-rice", 2.5),
        )

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.consumptionTrends.size)

        val trend1 = state.consumptionTrends[0]
        assertEquals("Café Torrado", trend1.productName)
        assertEquals("Bebidas", trend1.productCategory)
        assertEquals("pct", trend1.productMeasureUnity)
        assertEquals("4", trend1.consumptionAmount)
        assertEquals("icon_coffee", trend1.iconPainterURI)

        val trend2 = state.consumptionTrends[1]
        assertEquals("Arroz Branco", trend2.productName)
        assertEquals("Grãos", trend2.productCategory)
        assertEquals("kg", trend2.productMeasureUnity)
        assertEquals("2,5", trend2.consumptionAmount)
    }

    @Test
    fun `dialog open and dismiss works correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSetBudgetDialogOpen)

        viewModel.openSetBudgetDialog()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSetBudgetDialogOpen)

        viewModel.dismissSetBudgetDialog()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSetBudgetDialogOpen)
    }

    @Test
    fun `saveMonthlyBudget updates repository, closes dialog, and updates uiState`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.financialSummary.hasBudget)

        viewModel.openSetBudgetDialog()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSetBudgetDialogOpen)

        viewModel.saveMonthlyBudget(150000L) // R$ 1.500,00
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSetBudgetDialogOpen)
        assertTrue(state.financialSummary.hasBudget)
        assertEquals(150000L, state.financialSummary.budgetInCents)
        assertEquals("R$ 1500,00", state.financialSummary.budget)
    }

    @Test
    fun `saveMonthlyBudgetText parses decimal string correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.financialSummary.hasBudget)

        viewModel.saveMonthlyBudgetText("1.250,50")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.financialSummary.hasBudget)
        assertEquals(125050L, state.financialSummary.budgetInCents)
        assertEquals("R$ 1250,50", state.financialSummary.budget)
    }

    @Test
    fun `saveMonthlyBudgetText with invalid string shows error notification and does not update budget`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        notificationManager.notifications.test {
            viewModel.saveMonthlyBudgetText("invalid-text")
            testScheduler.advanceUntilIdle()

            val notification = awaitItem()
            assertEquals(com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType.ERROR, notification.type)
        }

        assertFalse(viewModel.uiState.value.financialSummary.hasBudget)
    }

    @Test
    fun `expiring item with null expiration date maps to fallback label`() = runTest(testDispatcher) {
        val item = PantryItemWithCategory(
            pantryItem = PantryItem(id = "p-null", productId = "prod-null", quantity = 1.0, expirationDate = null, updatedAt = 0L),
            name = "Sal",
            category = "Condimentos",
        )
        pantryRepository.customExpiringItems.value = listOf(item)

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.expiringItems.size)
        assertEquals(UiText.Resource(Res.string.home_expiring_no_date), state.expiringItems.first().expirationLabel)
        assertFalse(state.expiringItems.first().isExpired)
        assertEquals("--/--", state.expiringItems.first().anchoredTargetLabel)
        assertEquals(0f, state.expiringItems.first().anchoredGaugeProgress)
    }

    @Test
    fun `consumption trends with unregistered product falls back gracefully to default values`() = runTest(testDispatcher) {
        pantryMovementRepository.customMostConsumed.value = listOf(
            Pair("unregistered-id", 3.0)
        )

        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.consumptionTrends.size)
        assertEquals("Produto", state.consumptionTrends.first().productName)
        assertEquals("Geral", state.consumptionTrends.first().productCategory)
        assertEquals("un", state.consumptionTrends.first().productMeasureUnity)
        assertEquals("3", state.consumptionTrends.first().consumptionAmount)
    }
}

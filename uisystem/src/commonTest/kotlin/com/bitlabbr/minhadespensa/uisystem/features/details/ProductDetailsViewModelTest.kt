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

package com.bitlabbr.minhadespensa.uisystem.features.details

import com.bitlabbr.minhadespensa.core.domain.model.*
import com.bitlabbr.minhadespensa.core.domain.usecase.AddCatalogItemToShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.AddPantryItemUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.*
import com.bitlabbr.minhadespensa.uisystem.features.details.model.ProductDetailsSubFlow
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var pantryRepository: FakePantryRepository
    private lateinit var priceRepository: FakePriceRepository
    private lateinit var shoppingListRepository: FakeShoppingListRepository
    private lateinit var addCatalogItemToShoppingListUseCase: AddCatalogItemToShoppingListUseCase
    private lateinit var addPantryItemUseCase: AddPantryItemUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var viewModel: ProductDetailsViewModel

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1000000L)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        catalogRepository = FakeCatalogRepository()
        pantryRepository = FakePantryRepository(catalogRepository)
        priceRepository = FakePriceRepository()
        shoppingListRepository = FakeShoppingListRepository()
        addCatalogItemToShoppingListUseCase = AddCatalogItemToShoppingListUseCase(shoppingListRepository)
        addPantryItemUseCase = AddPantryItemUseCase(pantryRepository)
        logger = FakeAppLogger()
        notificationManager = AppNotificationManager()

        viewModel = ProductDetailsViewModel(
            catalogRepository = catalogRepository,
            pantryRepository = pantryRepository,
            priceRepository = priceRepository,
            shoppingListRepository = shoppingListRepository,
            addCatalogItemToShoppingListUseCase = addCatalogItemToShoppingListUseCase,
            addPantryItemUseCase = addPantryItemUseCase,
            logger = logger,
            notificationManager = notificationManager,
            clock = fixedClock,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading product should combine catalog, pantry stock, and price history correctly`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(
            id = "prod-1",
            name = "Café Especial",
            brand = "Gourmet",
            category = "Bebidas",
            ean = "7891234567890",
            measureUnit = MeasureUnit.GRAM,
            netWeight = 500.0,
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(product, byteArrayOf(1, 2, 3))

        val pantryBatch1 = PantryItem(
            id = "batch-1",
            productId = "prod-1",
            quantity = 2.0,
            expirationDate = 2000000L,
            batchNumber = "LOTE-A",
            updatedAt = 1000L,
        )
        val pantryBatch2 = PantryItem(
            id = "batch-2",
            productId = "prod-1",
            quantity = 3.0,
            expirationDate = 1500000L,
            batchNumber = "LOTE-B",
            updatedAt = 1100L,
        )
        pantryRepository.insertPantryItem(pantryBatch1)
        pantryRepository.insertPantryItem(pantryBatch2)

        val price1 = PriceEntry(
            id = "price-1",
            productId = "prod-1",
            priceInCents = 1500L,
            storeName = "Mercado Central",
            updatedAt = 1000L,
        )
        val price2 = PriceEntry(
            id = "price-2",
            productId = "prod-1",
            priceInCents = 1800L,
            storeName = "Supermercado Sul",
            updatedAt = 1200L,
        )
        priceRepository.insertPriceEntry(price1)
        priceRepository.insertPriceEntry(price2)

        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-1")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.product)
        assertEquals("Café Especial", state.product?.name)
        assertEquals("Gourmet", state.product?.brand)
        assertEquals("7891234567890", state.product?.ean)

        // Pantry stock assertions
        val stock = state.pantryStock
        assertNotNull(stock)
        assertTrue(stock.hasStock)
        assertEquals(5.0, stock.totalQuantity)
        assertEquals(1500000L, stock.closestExpirationDate)
        assertFalse(stock.isExpired)
        assertEquals(2, stock.batches.size)
        // First batch should be the closest expiration (FEFO order)
        assertEquals("batch-2", stock.batches.first().id)

        // Price history assertions
        assertEquals(1800L, state.latestPrice)
        assertEquals(1650L, state.averagePrice)
        assertEquals(1500L, state.lowestPrice)
        assertEquals(1800L, state.highestPrice)
        assertEquals(2, state.priceHistory.size)
    }

    @Test
    fun `product without pantry items should have hasStock false`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(
            id = "prod-no-stock",
            name = "Azeite",
            category = "Condimentos",
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(product, null)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-no-stock")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.product)
        val stock = state.pantryStock
        assertNotNull(stock)
        assertFalse(stock.hasStock)
        assertEquals(0.0, stock.totalQuantity)
        assertTrue(stock.batches.isEmpty())
    }

    @Test
    fun `onConsumeStock should consume from earliest expiring batch by default`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(id = "prod-2", name = "Leite", category = "Laticínios", updatedAt = 1000L)
        catalogRepository.insertProduct(product, null)

        val earlyBatch = PantryItem(
            id = "batch-early",
            productId = "prod-2",
            quantity = 2.0,
            expirationDate = 1200000L,
            updatedAt = 1000L,
        )
        val lateBatch = PantryItem(
            id = "batch-late",
            productId = "prod-2",
            quantity = 3.0,
            expirationDate = 2000000L,
            updatedAt = 1000L,
        )
        pantryRepository.insertPantryItem(earlyBatch)
        pantryRepository.insertPantryItem(lateBatch)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-2")
        testScheduler.advanceUntilIdle()

        // Consume 1 unit with FEFO
        viewModel.onConsumeStock(quantity = 1.0)
        testScheduler.advanceUntilIdle()

        val updatedEarlyBatch = pantryRepository.getPantryItemById("batch-early").first()
        assertNotNull(updatedEarlyBatch)
        assertEquals(1.0, updatedEarlyBatch.quantity)

        val updatedLateBatch = pantryRepository.getPantryItemById("batch-late").first()
        assertNotNull(updatedLateBatch)
        assertEquals(3.0, updatedLateBatch.quantity)
    }

    @Test
    fun `onConsumeStock when quantity exceeds first batch should cascade consumption across multiple batches in FEFO order`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(id = "prod-cascade", name = "Arroz", category = "Grãos", updatedAt = 1000L)
        catalogRepository.insertProduct(product, null)

        val batch1 = PantryItem(
            id = "batch-1",
            productId = "prod-cascade",
            quantity = 2.0,
            expirationDate = 1100000L,
            updatedAt = 1000L,
        )
        val batch2 = PantryItem(
            id = "batch-2",
            productId = "prod-cascade",
            quantity = 3.0,
            expirationDate = 1500000L,
            updatedAt = 1000L,
        )
        pantryRepository.insertPantryItem(batch1)
        pantryRepository.insertPantryItem(batch2)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-cascade")
        testScheduler.advanceUntilIdle()

        // Consume 3.5 units: 2.0 from batch-1 (draining it) and 1.5 from batch-2
        viewModel.onConsumeStock(quantity = 3.5)
        testScheduler.advanceUntilIdle()

        val updatedBatch1 = pantryRepository.getPantryItemById("batch-1").first()
        assertNotNull(updatedBatch1)
        assertEquals(0.0, updatedBatch1.quantity)

        val updatedBatch2 = pantryRepository.getPantryItemById("batch-2").first()
        assertNotNull(updatedBatch2)
        assertEquals(1.5, updatedBatch2.quantity)
    }

    @Test
    fun `onAddPantryStock should persist stock and dismiss subflow`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(id = "prod-3", name = "Farinha", category = "Grãos", updatedAt = 1000L)
        catalogRepository.insertProduct(product, null)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-3")
        testScheduler.advanceUntilIdle()

        viewModel.onOpenAddStockSheet()
        testScheduler.advanceUntilIdle()
        assertIs<ProductDetailsSubFlow.AddPantryStock>(viewModel.uiState.value.activeSubFlow)

        viewModel.onAddPantryStock(
            quantity = 4.0,
            expirationDate = 3000000L,
            batchNumber = "LOTE-FARINHA",
        )
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeSubFlow)
        val pantryItems = pantryRepository.getPantryItemsByProductId("prod-3").first()
        assertEquals(1, pantryItems.size)
        assertEquals(4.0, pantryItems.first().quantity)
        assertEquals("LOTE-FARINHA", pantryItems.first().batchNumber)
    }

    @Test
    fun `onAddToShoppingList should append item to target list and dismiss subflow`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(id = "prod-4", name = "Biscoito", category = "Snacks", updatedAt = 1000L)
        catalogRepository.insertProduct(product, null)

        val list = ShoppingList(id = "list-1", name = "Compras da Semana", updatedAt = 1000L)
        shoppingListRepository.insertShoppingList(list)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-4")
        testScheduler.advanceUntilIdle()

        viewModel.onOpenAddToListDialog()
        testScheduler.advanceUntilIdle()
        assertEquals(ProductDetailsSubFlow.SelectShoppingList, viewModel.uiState.value.activeSubFlow)

        viewModel.onAddToShoppingList("list-1", 2.0)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeSubFlow)
        val updatedList = shoppingListRepository.getShoppingListById("list-1").first()
        assertNotNull(updatedList)
        val item = updatedList.items.firstOrNull { it.productId == "prod-4" }
        assertNotNull(item)
        assertEquals(2.0, item.quantity)
    }

    @Test
    fun `onConsumeStock consuming full stock should update hasStock to false and batches to empty`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val product = CatalogProduct(id = "prod-full", name = "Azeite", category = "Condimentos", updatedAt = 1000L)
        catalogRepository.insertProduct(product, null)

        val item = PantryItem(
            id = "batch-full",
            productId = "prod-full",
            quantity = 2.0,
            expirationDate = 1500000L,
            updatedAt = 1000L,
        )
        pantryRepository.insertPantryItem(item)
        testScheduler.advanceUntilIdle()

        viewModel.loadProduct("prod-full")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.pantryStock?.hasStock == true)

        viewModel.onConsumeStock(quantity = 2.0)
        testScheduler.advanceUntilIdle()

        val stock = viewModel.uiState.value.pantryStock
        assertNotNull(stock)
        assertFalse(stock.hasStock)
        assertEquals(0.0, stock.totalQuantity)
        assertTrue(stock.batches.isEmpty())

        // Ensure clicking consume dialog when hasStock is false does nothing
        viewModel.onOpenConsumeDialog()
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.activeSubFlow)
    }
}

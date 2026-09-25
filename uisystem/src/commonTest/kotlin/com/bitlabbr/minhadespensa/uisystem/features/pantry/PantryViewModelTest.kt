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

package com.bitlabbr.minhadespensa.uisystem.features.pantry

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.usecase.AddPantryItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakePantryRepository
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantrySubFlow
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PantryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var pantryRepository: FakePantryRepository
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var checkEanStatusUseCase: CheckEanStatusUseCase
    private lateinit var addPantryItemUseCase: AddPantryItemUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var viewModel: PantryViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        catalogRepository = FakeCatalogRepository()
        pantryRepository = FakePantryRepository(catalogRepository)
        checkEanStatusUseCase = CheckEanStatusUseCase(catalogRepository)
        addPantryItemUseCase = AddPantryItemUseCase(pantryRepository)
        logger = FakeAppLogger()
        notificationManager = AppNotificationManager()

        viewModel = PantryViewModel(
            pantryRepository = pantryRepository,
            catalogRepository = catalogRepository,
            checkEanStatusUseCase = checkEanStatusUseCase,
            addPantryItemUseCase = addPantryItemUseCase,
            logger = logger,
            notificationManager = notificationManager,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search and category filtering should filter active pantry items`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val item1 = PantryItemWithCategory(
            pantryItem = PantryItem(id = "p-1", productId = "prod-1", quantity = 2.0, expirationDate = null, updatedAt = 1000L),
            name = "Leite Integral",
            category = "Laticínios",
        )
        val item2 = PantryItemWithCategory(
            pantryItem = PantryItem(id = "p-2", productId = "prod-2", quantity = 1.0, expirationDate = null, updatedAt = 1000L),
            name = "Queijo Prato",
            category = "Laticínios",
        )
        val item3 = PantryItemWithCategory(
            pantryItem = PantryItem(id = "p-3", productId = "prod-3", quantity = 5.0, expirationDate = null, updatedAt = 1000L),
            name = "Arroz Branco",
            category = "Grãos",
        )

        pantryRepository.customItemsWithCategory.value = listOf(item1, item2, item3)
        testScheduler.advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.listState.products.size)

        // Filter by category "Laticínios"
        viewModel.onCategorySelected("Laticínios")
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.listState.products.size)

        // Further filter by query "Queijo"
        viewModel.onSearchQueryChanged("Queijo")
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.listState.products.size)
        assertEquals("p-2", viewModel.uiState.value.listState.products.first().id)

        // Clear filter
        viewModel.onCategorySelected(null)
        viewModel.onSearchQueryChanged("")
        testScheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.listState.products.size)
    }

    @Test
    fun `subflow state transitions should handle scanner manual register and barcode recognition`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val catalogProd = CatalogProduct(id = "c-1", name = "Macarrão", ean = "7891112223334", updatedAt = 1000L)
        catalogRepository.insertProduct(catalogProd, null)
        testScheduler.advanceUntilIdle()

        // 1. Start scanner
        viewModel.onStartScanFlow()
        testScheduler.advanceUntilIdle()
        assertEquals(PantrySubFlow.BarcodeScanner, viewModel.uiState.value.activeSubFlow)

        // 2. Scan registered product -> AddItemDetails
        viewModel.onBarcodeScanned("7891112223334")
        testScheduler.advanceUntilIdle()

        val subflow1 = viewModel.uiState.value.activeSubFlow
        assertIs<PantrySubFlow.AddItemDetails>(subflow1)
        assertEquals("c-1", subflow1.product.id)

        // 3. Scan unregistered barcode -> CreateCatalogProduct
        viewModel.onBarcodeScanned("7899999999999")
        testScheduler.advanceUntilIdle()

        val subflow2 = viewModel.uiState.value.activeSubFlow
        assertIs<PantrySubFlow.CreateCatalogProduct>(subflow2)
        assertEquals("7899999999999", subflow2.initialEan)

        // 4. Product created from catalog -> AddItemDetails
        val newlyCreated = CatalogProduct(id = "c-new", name = "Molho de Tomate", updatedAt = 1000L)
        viewModel.onProductCreatedFromCatalog(newlyCreated)
        testScheduler.advanceUntilIdle()

        val subflow3 = viewModel.uiState.value.activeSubFlow
        assertIs<PantrySubFlow.AddItemDetails>(subflow3)
        assertEquals("c-new", subflow3.product.id)

        // 5. Dismiss subflow
        viewModel.onDismissSubFlow()
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.activeSubFlow)
    }

    @Test
    fun `onConfirmAddPantryItem should persist pantry item and emit success notification`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        notificationManager.notifications.test {
            viewModel.onStartManualRegisterFlow("7891234567890")
            testScheduler.advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.activeSubFlow)

            viewModel.onConfirmAddPantryItem(
                productId = "prod-pantry-1",
                quantity = 3.0,
                expirationDate = 1800000000000L,
                batchNumber = "LOTE-123",
            )
            testScheduler.advanceUntilIdle()

            // Subflow dismissed
            assertNull(viewModel.uiState.value.activeSubFlow)

            // Item persisted in repository
            val items = pantryRepository.getAllActivePantryItems().first()
            val savedItem = items.firstOrNull { it.productId == "prod-pantry-1" }
            assertNotNull(savedItem)
            assertEquals(3.0, savedItem.quantity)
            assertEquals("LOTE-123", savedItem.batchNumber)

            // Notification received
            val notification = awaitItem()
            assertEquals(MinhaDespensaSnackbarType.SUCCESS, notification.type)
        }
    }
}

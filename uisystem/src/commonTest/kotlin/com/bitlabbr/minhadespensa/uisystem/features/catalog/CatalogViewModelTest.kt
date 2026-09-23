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

package com.bitlabbr.minhadespensa.uisystem.features.catalog

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.usecase.SaveCatalogProductUseCase
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.ProductFormState
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var catalogRepository: FakeCatalogRepository
    private lateinit var saveProductUseCase: SaveCatalogProductUseCase
    private lateinit var logger: FakeAppLogger
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var viewModel: CatalogViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        catalogRepository = FakeCatalogRepository()
        saveProductUseCase = SaveCatalogProductUseCase(catalogRepository)
        logger = FakeAppLogger()
        notificationManager = AppNotificationManager()
        viewModel = CatalogViewModel(
            catalogRepository = catalogRepository,
            saveProductUseCase = saveProductUseCase,
            logger = logger,
            notificationManager = notificationManager,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search and category filtering should narrow product list`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val prod1 = CatalogProduct(id = "p1", name = "Café Melitta", brand = "Melitta", category = "Bebidas", updatedAt = 1000L)
        val prod2 = CatalogProduct(id = "p2", name = "Café Pilão", brand = "Pilão", category = "Bebidas", updatedAt = 1000L)
        val prod3 = CatalogProduct(id = "p3", name = "Arroz Tio João", brand = "Tio João", category = "Grãos", updatedAt = 1000L)
        catalogRepository.insertProduct(prod1, null)
        catalogRepository.insertProduct(prod2, null)
        catalogRepository.insertProduct(prod3, null)

        testScheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.listState.products.size)

        // Filter by category "Bebidas"
        viewModel.onCategorySelected("Bebidas")
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.listState.products.size)

        // Further filter by query "Melitta"
        viewModel.onSearchQueryChanged("Melitta")
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.listState.products.size)
        assertEquals("p1", viewModel.uiState.value.listState.products.first().id)

        // Clear query
        viewModel.onSearchQueryChanged("")
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.listState.products.size)
    }

    @Test
    fun `form opening and closing should toggle isFormOpen and reset formState`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.openAddProductSheet()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isFormOpen)

        viewModel.onFormChange(ProductFormState(name = "Novo Produto"))
        testScheduler.advanceUntilIdle()
        assertEquals("Novo Produto", viewModel.uiState.value.formState.name)

        viewModel.closeAddProductSheet()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFormOpen)
        assertEquals("", viewModel.uiState.value.formState.name)
    }

    @Test
    fun `form validation should detect invalid number and duplicate ean`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        val existing = CatalogProduct(id = "p-ean", name = "Azeite Extra Virgem", ean = "7891234567890", updatedAt = 1000L)
        catalogRepository.insertProduct(existing, null)
        testScheduler.advanceUntilIdle()

        viewModel.openAddProductSheet()
        testScheduler.advanceUntilIdle()

        // 1. Invalid netWeight number
        viewModel.onFormChange(ProductFormState(name = "Azeite", netWeight = "abc"))
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.formState.netWeightError)

        // 2. Duplicate EAN debounced validation
        viewModel.onFormChange(ProductFormState(name = "Azeite", ean = "7891234567890"))
        // Advance debounce delay (350ms)
        testScheduler.advanceTimeBy(400)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formState.eanError)
    }

    @Test
    fun `saveProduct should persist product via SaveCatalogProductUseCase and close sheet`() = runTest(testDispatcher) {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.openAddProductSheet()
        testScheduler.advanceUntilIdle()

        viewModel.onFormChange(
            ProductFormState(
                name = "Açúcar Demerara",
                brand = "União",
                category = "Mercearia",
                netWeight = "1",
            )
        )
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.formState.isFormValid)

        viewModel.saveProduct()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFormOpen)

        val allProducts = catalogRepository.getAllActiveProducts().first()
        val created = allProducts.firstOrNull { it.name == "Açúcar Demerara" }
        assertNotNull(created)
        assertEquals("União", created.brand)
        assertEquals("Mercearia", created.category)
    }
}

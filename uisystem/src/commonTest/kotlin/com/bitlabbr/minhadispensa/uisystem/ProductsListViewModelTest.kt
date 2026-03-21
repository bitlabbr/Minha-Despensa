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
package com.bitlabbr.minhadispensa.uisystem

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductsListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductsListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeProductRepository
    private lateinit var fakeLogger: FakeLogger
    private lateinit var viewModel: ProductsListViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeProductRepository()
        fakeLogger = FakeLogger()
        viewModel = ProductsListViewModel(fakeRepository, fakeLogger)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `form state updates when user types name`() = runTest {
        viewModel.onNameChange("Arroz")
        assertEquals("Arroz", viewModel.formState.value.name)
    }

    @Test
    fun `saveProduct should not save if name is empty`() = runTest {
        viewModel.onNameChange("")
        viewModel.onQuantityChange("2")
        viewModel.saveProduct()
        assertEquals(0, fakeRepository.items.size)
    }

    @Test
    fun `saveProduct should save correctly when data is valid`() = runTest {
        viewModel.onNameChange("Feijão")
        viewModel.onQuantityChange("5.5")
        viewModel.onUnitChange(MeasureUnit.KILOGRAM)

        viewModel.saveProduct()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, fakeRepository.items.size)
        assertEquals("Feijão", fakeRepository.items.first().name)
        assertEquals("", viewModel.formState.value.name)
    }
}

class FakeProductRepository : ProductRepository {
    val items = mutableListOf<Product>()

    override fun getAllProducts() = flowOf(items)

    override suspend fun insertProduct(product: Product) {
        items.add(product)
    }

    override suspend fun deleteProductById(id: String) {
        items.removeAll { it.id == id }
    }
    override suspend fun getProductById(id: String): Product? {
        return items.find { it.id == id }
    }
}

class FakeLogger : AppLogger {
    override fun d(tag: String, message: String) {
        println(buildLogLine("D", tag, message))
    }
    override fun e(tag: String, message: String, error: Throwable?) {
        val fullMessage = message + (error?.let { "\n${it.stackTraceToString()}" } ?: "")
        println(buildLogLine("E", tag, fullMessage))
    }

    private fun buildLogLine(type: String, tag: String, message: String): String {
        return "[$tag][$type]: $message"
    }
}

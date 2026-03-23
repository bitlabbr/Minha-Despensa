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
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RoomCatalogRepositoryTest : BaseTest() {
    private lateinit var catalogRepository: RoomCatalogRepository
    private lateinit var db: AppDatabase
    private val logger = ConsoleLogger("Test")

    @BeforeTest
    fun setup() {
        val builder = getTestDatabaseBuilder()
        db = createInMemoryDatabase(builder)
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
    fun `should save product successfully`() = runTest {
        val productId = Uuid.random().toString()
        val now = getCurrentTime()
        val productEan = "7891234567890"
        val productRef = CatalogProduct(
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
        )
        catalogRepository.saveProduct(productRef, null)
        catalogRepository.getProductById(productId).test {
            val product = awaitItem()
            assertProduct(product, productRef)
            cancelAndIgnoreRemainingEvents()
        }
        catalogRepository.getProductByEan(productEan).test {
            val product = awaitItem()
            assertProduct(product, productRef)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `should save product and its image blob atomically`() = runTest {
        val productId = Uuid.random().toString()
        val productEan = "0000000000000"
        val productRef = CatalogProduct(
            id = productId,
            name = "Café",
            brand = "Pilão",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 0.5,
            updatedAt = getCurrentTime(),
            isDeleted = false,
            manuallyAdded = true,
            ean = productEan,
            thumbnailUrl = null
        )
        val fakeImageBlob = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

        catalogRepository.saveProduct(productRef, fakeImageBlob)

        val savedProduct = catalogRepository.getProductById(productId).first()
        assertNotNull(savedProduct)
        val savedMedia = db.productMediaDao().getByProductId(productId)
        assertNotNull(savedMedia)
        assertTrue(fakeImageBlob.contentEquals(savedMedia.blob), "O BLOB salvo deve ser igual ao enviado")
    }

    private fun assertProduct(value: CatalogProduct?, ref: CatalogProduct) {
        assertTrue { value != null }
        val netWeight = value?.netWeight?: 0.0
        assertEquals(ref.id, value?.id)
        assertEquals(ref.name, value?.name)
        assertEquals(ref.brand, value?.brand)
        assertEquals(ref.measureUnit, value?.measureUnit)
        assertEquals(ref.netWeight, netWeight, 0.001)
        assertEquals(ref.updatedAt, value?.updatedAt)
        assertEquals(ref.isDeleted, value?.isDeleted)
        assertEquals(ref.manuallyAdded, value?.manuallyAdded)
        assertEquals(ref.ean, value?.ean)
        assertEquals(ref.thumbnailUrl, value?.thumbnailUrl)
    }
}
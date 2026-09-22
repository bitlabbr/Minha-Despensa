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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.core.domain.fakes.FakePantryRepository
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalogAndPantryUseCasesTest {

    private val catalogRepository = FakeCatalogRepository()
    private val pantryRepository = FakePantryRepository()

    private val checkEanStatusUseCase = CheckEanStatusUseCase(catalogRepository)
    private val saveCatalogProductUseCase = SaveCatalogProductUseCase(catalogRepository)
    private val addPantryItemUseCase = AddPantryItemUseCase(pantryRepository)

    @Test
    fun `CheckEanStatusUseCase should validate formats and return Found NotFound or Invalid`() = runTest {
        val existing = CatalogProduct(
            id = "prod-ean",
            name = "Café Torrado",
            ean = "7891234567890",
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(existing, null)

        // 1. EAN cadastrado
        val found = checkEanStatusUseCase("7891234567890")
        assertIs<EanStatus.Found>(found)
        assertEquals("Café Torrado", found.product.name)

        // 2. EAN válido não cadastrado
        val notFound = checkEanStatusUseCase("12345670")
        assertIs<EanStatus.NotFound>(notFound)

        // 3. EAN com tamanho incorreto
        val invalidFormat = checkEanStatusUseCase("12345")
        assertIs<EanStatus.InvalidFormat>(invalidFormat)

        // 4. EAN em branco
        val empty = checkEanStatusUseCase("   ")
        assertIs<EanStatus.Empty>(empty)
    }

    @Test
    fun `SaveCatalogProductUseCase should sanitize and persist product with default category`() = runTest {
        val result = saveCatalogProductUseCase(
            name = "  Arroz Branco Tipo 1  ",
            brand = "  Tio João  ",
            category = "   ", // Deve receber a categoria padrão
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 5.0,
            ean = "7891234567890",
        )

        assertTrue(result.isSuccess)
        val saved = result.getOrThrow()
        assertEquals("Arroz Branco Tipo 1", saved.name)
        assertEquals("Tio João", saved.brand)
        assertEquals(CoreConstants.Product.DEFAULT_CATEGORY, saved.category)
        assertEquals(5.0, saved.netWeight)

        val inRepo = catalogRepository.getProductById(saved.id).first()
        assertNotNull(inRepo)
    }

    @Test
    fun `AddPantryItemUseCase should persist stock item with positive quantity`() = runTest {
        val result = addPantryItemUseCase(
            productId = "prod-1",
            quantity = 3.5,
            expirationDate = 200000L,
            batchNumber = " LOTE-2026 ",
        )

        assertTrue(result.isSuccess)
        val item = result.getOrThrow()
        assertEquals("prod-1", item.productId)
        assertEquals(3.5, item.quantity)
        assertEquals("LOTE-2026", item.batchNumber)

        val inRepo = pantryRepository.getPantryItemById(item.id).first()
        assertNotNull(inRepo)
        assertEquals(3.5, inRepo.quantity)
    }

    @Test
    fun `SaveCatalogProductUseCase should update product when isEditing is true`() = runTest {
        val initial = saveCatalogProductUseCase(
            name = "Sabão em Pó",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 1.0,
        ).getOrThrow()

        val updated = saveCatalogProductUseCase(
            id = initial.id,
            name = "Sabão em Pó Concentrado",
            measureUnit = MeasureUnit.KILOGRAM,
            netWeight = 2.0,
            isEditing = true,
        ).getOrThrow()

        assertEquals(initial.id, updated.id)
        assertEquals("Sabão em Pó Concentrado", updated.name)
        assertEquals(2.0, updated.netWeight)

        val inRepo = catalogRepository.getProductById(initial.id).first()
        assertEquals("Sabão em Pó Concentrado", inRepo?.name)
    }

    @Test
    fun `AddPantryItemUseCase should reject invalid inputs`() = runTest {
        // Quantidade zero ou negativa
        val zeroQty = addPantryItemUseCase(productId = "prod-1", quantity = 0.0)
        assertTrue(zeroQty.isFailure)

        val negativeQty = addPantryItemUseCase(productId = "prod-1", quantity = -1.5)
        assertTrue(negativeQty.isFailure)

        // Product ID em branco
        val blankProduct = addPantryItemUseCase(productId = "   ", quantity = 1.0)
        assertTrue(blankProduct.isFailure)
    }
}
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
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductCategoryUseCasesTest {

    private val catalogRepository = FakeCatalogRepository()
    private val saveCategoryUseCase = SaveProductCategoryUseCase(catalogRepository)
    private val deleteCategoryUseCase = DeleteProductCategoryUseCase(catalogRepository)

    @Test
    fun `SaveProductCategoryUseCase should create a valid custom category and trim whitespace`() = runTest {
        val result = saveCategoryUseCase(name = "  Pet Shop  ")

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()
        assertEquals("Pet Shop", created.name)
        assertFalse(created.isDefault)
        assertFalse(created.isDeleted)

        val categories = catalogRepository.getCategories().first()
        assertTrue(categories.contains("Pet Shop"))
    }

    @Test
    fun `SaveProductCategoryUseCase should fail when category name is blank or exceeds max length`() = runTest {
        val blankResult = saveCategoryUseCase(name = "   ")
        assertTrue(blankResult.isFailure)
        assertEquals(
            CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK,
            blankResult.exceptionOrNull()?.message,
        )

        val longName = "A".repeat(CoreConstants.Product.CATEGORY_MAX_LENGTH + 1)
        val tooLongResult = saveCategoryUseCase(name = longName)
        assertTrue(tooLongResult.isFailure)
        assertTrue(
            tooLongResult.exceptionOrNull()?.message?.contains(
                CoreConstants.Validation.ERROR_PRODUCT_CATEGORY_TOO_LONG
            ) == true
        )
    }

    @Test
    fun `SaveProductCategoryUseCase should reject duplicate category names case and accent insensitively`() = runTest {
        // Default category "Cereais e Grãos" already exists
        val duplicateDefault = saveCategoryUseCase(name = "cereais e graos")
        assertTrue(duplicateDefault.isFailure)
        assertEquals(
            CoreConstants.Validation.ERROR_CATEGORY_ALREADY_EXISTS,
            duplicateDefault.exceptionOrNull()?.message,
        )

        // Create custom "Padaria & Matinais"
        assertTrue(saveCategoryUseCase(name = "Padaria & Matinais").isSuccess)

        val duplicateCustom = saveCategoryUseCase(name = "  padaria & matinais ")
        assertTrue(duplicateCustom.isFailure)
        assertEquals(
            CoreConstants.Validation.ERROR_CATEGORY_ALREADY_EXISTS,
            duplicateCustom.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `SaveProductCategoryUseCase should rename category and cascade update to linked products`() = runTest {
        val created = saveCategoryUseCase(name = "Pet Shop").getOrThrow()
        val product = CatalogProduct(
            id = "prod-pet-1",
            name = "Ração Golden 15kg",
            category = "Pet Shop",
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(product, null)

        val renameResult = saveCategoryUseCase(
            id = created.id,
            name = "Pets e Animais",
            oldName = "Pet Shop",
        )
        assertTrue(renameResult.isSuccess)
        assertEquals("Pets e Animais", renameResult.getOrThrow().name)

        val updatedProduct = catalogRepository.getProductById("prod-pet-1").first()!!
        assertEquals("Pets e Animais", updatedProduct.category)
        assertTrue(updatedProduct.updatedAt > 1000L)
    }

    @Test
    fun `SaveProductCategoryUseCase should forbid renaming the default Outros category`() = runTest {
        val activeCategories = catalogRepository.getAllActiveCategories().first()
        val outrosCategory = activeCategories.first { it.name == CoreConstants.Product.DEFAULT_CATEGORY }

        val result = saveCategoryUseCase(
            id = outrosCategory.id,
            name = "Diversos",
            oldName = CoreConstants.Product.DEFAULT_CATEGORY,
        )
        assertTrue(result.isFailure)
        assertEquals(
            CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE,
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `DeleteProductCategoryUseCase should soft delete category and move products to Outros`() = runTest {
        val created = saveCategoryUseCase(name = "Congelados").getOrThrow()
        val product = CatalogProduct(
            id = "prod-cong-1",
            name = "Lasanha Bolonhesa",
            category = "Congelados",
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(product, null)

        val deleteResult = deleteCategoryUseCase(
            id = created.id,
            categoryName = "Congelados",
        )
        assertTrue(deleteResult.isSuccess)

        val activeCategories = catalogRepository.getAllActiveCategories().first()
        assertFalse(activeCategories.any { it.id == created.id })

        val updatedProduct = catalogRepository.getProductById("prod-cong-1").first()!!
        assertEquals(CoreConstants.Product.DEFAULT_CATEGORY, updatedProduct.category)
        assertTrue(updatedProduct.updatedAt > 1000L)
    }

    @Test
    fun `DeleteProductCategoryUseCase should forbid deleting the default Outros category`() = runTest {
        val result = deleteCategoryUseCase(
            id = "default-outros",
            categoryName = "outros",
        )
        assertTrue(result.isFailure)
        assertEquals(
            CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE,
            result.exceptionOrNull()?.message,
        )
    }
}

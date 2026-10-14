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

import com.bitlabbr.minhadespensa.core.domain.fakes.FakePantryRepository
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PantryTotalValueUseCaseTest {

    private val pantryRepository = FakePantryRepository()
    private val useCase = GetPantryTotalValueUseCase(pantryRepository)

    @Test
    fun `when pantry is empty should return zero total value and zero counts`() = runTest {
        val result = useCase().first()

        assertEquals(0L, result.totalValueInCents)
        assertEquals(0, result.itemsWithPriceCount)
        assertEquals(0, result.totalActiveItemsCount)
        assertNull(result.averagePriceInCents)
    }

    @Test
    fun `when pantry has items with unit price should calculate sum correctly`() = runTest {
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-1",
                productId = "prod-1",
                quantity = 2.0,
                unitPriceInCents = 500L, // 2 * 500 = 1000
                expirationDate = null,
                updatedAt = 0L,
            )
        )
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-2",
                productId = "prod-2",
                quantity = 1.5,
                unitPriceInCents = 1000L, // 1.5 * 1000 = 1500
                expirationDate = null,
                updatedAt = 0L,
            )
        )

        val result = useCase().first()

        assertEquals(2500L, result.totalValueInCents)
        assertEquals(2, result.itemsWithPriceCount)
        assertEquals(2, result.totalActiveItemsCount)
        assertEquals(1250L, result.averagePriceInCents)
    }

    @Test
    fun `items without price or zero price should not count towards priced items or total value`() = runTest {
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-priced",
                productId = "prod-1",
                quantity = 1.0,
                unitPriceInCents = 2000L,
                expirationDate = null,
                updatedAt = 0L,
            )
        )
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-no-price",
                productId = "prod-2",
                quantity = 3.0,
                unitPriceInCents = null,
                expirationDate = null,
                updatedAt = 0L,
            )
        )
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-zero-price",
                productId = "prod-3",
                quantity = 1.0,
                unitPriceInCents = 0L,
                expirationDate = null,
                updatedAt = 0L,
            )
        )

        val result = useCase().first()

        assertEquals(2000L, result.totalValueInCents)
        assertEquals(1, result.itemsWithPriceCount)
        assertEquals(3, result.totalActiveItemsCount)
        assertEquals(2000L, result.averagePriceInCents)
    }

    @Test
    fun `deleted items should be ignored completely`() = runTest {
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-active",
                productId = "prod-1",
                quantity = 1.0,
                unitPriceInCents = 1500L,
                expirationDate = null,
                updatedAt = 0L,
                isDeleted = false,
            )
        )
        pantryRepository.insertPantryItem(
            PantryItem(
                id = "item-deleted",
                productId = "prod-2",
                quantity = 5.0,
                unitPriceInCents = 1000L,
                expirationDate = null,
                updatedAt = 0L,
                isDeleted = true,
            )
        )

        val result = useCase().first()

        assertEquals(1500L, result.totalValueInCents)
        assertEquals(1, result.itemsWithPriceCount)
        assertEquals(1, result.totalActiveItemsCount)
    }
}

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

package com.bitlabbr.minhadespensa.core.domain.model

import com.bitlabbr.minhadespensa.core.domain.repository.PantryMovementRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMostConsumedProductsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PantryMovementTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `PantryMovement serialization and deserialization should preserve all fields`() {
        val movement = PantryMovement(
            id = "mov-uuid-1",
            pantryItemId = "pantry-uuid-1",
            productId = "prod-uuid-1",
            type = MovementType.ENTRY_MANUAL,
            quantity = 5.0,
            unitPriceInCents = 1000L,
            totalPriceInCents = 5000L,
            timestamp = 1728900000000L,
            storeOrReason = "Entrada na Despensa",
            isDeleted = false,
        )

        val serialized = json.encodeToString(movement)
        val deserialized = json.decodeFromString<PantryMovement>(serialized)

        assertEquals(movement, deserialized)
    }

    @Test
    fun `computedTotalPriceInCents should use explicit totalPriceInCents when present`() {
        val movement = PantryMovement(
            id = "mov-1",
            productId = "prod-1",
            type = MovementType.ENTRY_PURCHASE,
            quantity = 2.0,
            unitPriceInCents = 500L,
            totalPriceInCents = 1200L, // Explicit total takes precedence
            timestamp = 1000L,
        )

        assertEquals(1200L, movement.computedTotalPriceInCents)
    }

    @Test
    fun `computedTotalPriceInCents should fallback to quantity times unitPriceInCents rounded`() {
        val movement = PantryMovement(
            id = "mov-2",
            productId = "prod-2",
            type = MovementType.ENTRY_MANUAL,
            quantity = 2.5,
            unitPriceInCents = 1000L,
            totalPriceInCents = null,
            timestamp = 1000L,
        )

        assertEquals(2500L, movement.computedTotalPriceInCents)
    }

    @Test
    fun `computedTotalPriceInCents should return null if both totalPriceInCents and unitPriceInCents are null`() {
        val movement = PantryMovement(
            id = "mov-3",
            productId = "prod-3",
            type = MovementType.CONSUMPTION,
            quantity = 1.0,
            unitPriceInCents = null,
            totalPriceInCents = null,
            timestamp = 1000L,
        )

        assertNull(movement.computedTotalPriceInCents)
    }

    @Test
    fun `GetMostConsumedProductsUseCase should validate arguments strictly`() {
        val fakeRepo = object : PantryMovementRepository {
            override suspend fun recordMovement(movement: PantryMovement) = Unit
            override suspend fun recordMovements(movements: List<PantryMovement>) = Unit
            override fun getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovement>> =
                flowOf(emptyList())

            override fun getMostConsumedProducts(
                startTimestamp: Long,
                endTimestamp: Long,
                limit: Int
            ): Flow<List<Pair<String, Double>>> = flowOf(emptyList())
        }

        val useCase = GetMostConsumedProductsUseCase(fakeRepo)

        assertFailsWith<IllegalArgumentException> {
            useCase(startTimestamp = 2000L, endTimestamp = 1000L, limit = 5)
        }

        assertFailsWith<IllegalArgumentException> {
            useCase(startTimestamp = 1000L, endTimestamp = 2000L, limit = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            useCase(startTimestamp = 1000L, endTimestamp = 2000L, limit = -1)
        }
    }

    @Test
    fun `GetMostConsumedProductsUseCase should delegate to repository and emit result`() = runTest {
        val expected = listOf("prod-1" to 10.0, "prod-2" to 4.5)
        val fakeRepo = object : PantryMovementRepository {
            override suspend fun recordMovement(movement: PantryMovement) = Unit
            override suspend fun recordMovements(movements: List<PantryMovement>) = Unit
            override fun getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovement>> =
                flowOf(emptyList())

            override fun getMostConsumedProducts(
                startTimestamp: Long,
                endTimestamp: Long,
                limit: Int
            ): Flow<List<Pair<String, Double>>> = flowOf(expected)
        }

        val useCase = GetMostConsumedProductsUseCase(fakeRepo)
        val result = useCase(startTimestamp = 1000L, endTimestamp = 2000L, limit = 5).first()

        assertEquals(expected, result)
    }
}

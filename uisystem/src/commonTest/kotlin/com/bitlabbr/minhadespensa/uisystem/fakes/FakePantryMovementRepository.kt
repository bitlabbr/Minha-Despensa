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

package com.bitlabbr.minhadespensa.uisystem.fakes

import com.bitlabbr.minhadespensa.core.domain.model.MovementType
import com.bitlabbr.minhadespensa.core.domain.model.PantryMovement
import com.bitlabbr.minhadespensa.core.domain.repository.PantryMovementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePantryMovementRepository : PantryMovementRepository {
    val movements = MutableStateFlow<List<PantryMovement>>(emptyList())
    val customMostConsumed = MutableStateFlow<List<Pair<String, Double>>?>(null)

    override suspend fun recordMovement(movement: PantryMovement) {
        movements.value = movements.value + movement
    }

    override suspend fun recordMovements(movements: List<PantryMovement>) {
        this.movements.value = this.movements.value + movements
    }

    override fun getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovement>> {
        return movements.map { list ->
            list.filter { it.timestamp in startTimestamp..endTimestamp }
        }
    }

    override fun getMostConsumedProducts(
        startTimestamp: Long,
        endTimestamp: Long,
        limit: Int
    ): Flow<List<Pair<String, Double>>> {
        return customMostConsumed.map { custom ->
            if (custom != null) {
                custom.take(limit)
            } else {
                movements.value
                    .filter { it.timestamp in startTimestamp..endTimestamp && it.type == MovementType.CONSUMPTION }
                    .groupBy { it.productId }
                    .mapValues { entry -> entry.value.sumOf { it.quantity } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(limit)
            }
        }
    }
}

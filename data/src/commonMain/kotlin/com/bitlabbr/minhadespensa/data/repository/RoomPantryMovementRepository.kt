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

package com.bitlabbr.minhadespensa.data.repository

import com.bitlabbr.minhadespensa.core.domain.model.PantryMovement
import com.bitlabbr.minhadespensa.core.domain.repository.PantryMovementRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPantryMovementRepository(
    private val db: AppDatabase,
    private val logger: AppLogger,
    private val outboxEnqueuer: OutboxEnqueuer = OutboxEnqueuer(db.pendingSyncDao()),
) : PantryMovementRepository {

    private val TAG = "RoomPantryMovementRepo"
    private val dao = db.movementDao()

    override suspend fun recordMovement(movement: PantryMovement) {
        logger.d(TAG, "recordMovement: id=${movement.id}, type=${movement.type}, quantity=${movement.quantity}")
        val entity = movement.toEntity()
        dao.insertMovement(entity)
        outboxEnqueuer.enqueueMovement(entity)
    }

    override suspend fun recordMovements(movements: List<PantryMovement>) {
        logger.d(TAG, "recordMovements: count=${movements.size}")
        val entities = movements.map { it.toEntity() }
        dao.insertMovements(entities)
        entities.forEach { outboxEnqueuer.enqueueMovement(it) }
    }

    override fun getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovement>> {
        require(startTimestamp <= endTimestamp) { "startTimestamp cannot be greater than endTimestamp" }
        logger.d(TAG, "getMovementsForPeriod: start=$startTimestamp, end=$endTimestamp")
        return dao.getMovementsForPeriod(startTimestamp, endTimestamp).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMostConsumedProducts(
        startTimestamp: Long,
        endTimestamp: Long,
        limit: Int
    ): Flow<List<Pair<String, Double>>> {
        require(startTimestamp <= endTimestamp) { "startTimestamp cannot be greater than endTimestamp" }
        require(limit > 0) { "limit must be strictly greater than zero" }
        logger.d(TAG, "getMostConsumedProducts: start=$startTimestamp, end=$endTimestamp, limit=$limit")
        return dao.getMostConsumedProducts(startTimestamp, endTimestamp, limit).map { list ->
            list.map { it.productId to it.totalConsumed }
        }
    }
}

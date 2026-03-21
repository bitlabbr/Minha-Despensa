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

import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.dao.PriceEntryDao
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class RoomPriceRepository(
    private val dao: PriceEntryDao,
    private val logger: AppLogger
) : PriceRepository {
    private val TAG = "RoomPriceRepository"

    override fun getPriceHistory(productId: String): Flow<List<PriceEntry>> {
        logger.d(TAG, "getPriceHistory: productId: $productId")
        return dao.getHistoryByProduct(productId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getLatestPrice(productId: String): Flow<PriceEntry?> {
        logger.d(TAG, "getLatestPrice: productId: $productId")
        return  dao.getLatestPrice(productId).map { it?.toDomain() }
    }

    override suspend fun addPriceEntry(entry: PriceEntry) {
        logger.d(TAG, "addPriceEntry: entry: $entry")
        dao.insertOrUpdate(entry.toEntity())
    }

    override suspend fun deletePriceEntry(id: String) {
        logger.d(TAG, "deletePriceEntry: id: $id")
        dao.markAsDeleted(id, getCurrentTime())
    }

    fun PriceEntryEntity.toDomain() = PriceEntry(
        id = this.id,
        productId = this.productId,
        priceInCents = this.priceInCents,
        storeName = this.storeName,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )

    fun PriceEntry.toEntity() = PriceEntryEntity(
        id = this.id,
        productId = this.productId,
        priceInCents = this.priceInCents,
        storeName = this.storeName,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )
}
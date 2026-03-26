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
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.dao.PriceEntryDao
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomPriceRepository(
    private val db: AppDatabase,
    private val logger: AppLogger
) : PriceRepository {
    private val TAG = "RoomPriceRepository"

    val dao = db.priceDao()

    override fun getPriceHistoryByProductId(productId: String): Flow<List<PriceEntry>> {
        logger.d(TAG, "getPriceHistoryByProductId: productId: $productId")
        return dao.getPriceHistoryByProductId(productId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getLatestPriceForProductID(productId: String): Flow<PriceEntry?> {
        logger.d(TAG, "getLatestPriceForProductID: productId: $productId")
        return dao.getLatestPriceForProductID(productId).map { it?.toDomain() }
    }

    override suspend fun insertPriceEntry(priceEntry: PriceEntry) {
        logger.d(TAG, "insertPriceEntry: priceEntry: $priceEntry")
        validatePriceEntry(priceEntry)
        dao.insertPriceEntry(priceEntry.toEntity())
    }

    override suspend fun forceUpdatePriceEntry(priceEntry: PriceEntry) {
        logger.d(TAG, "forceUpdatePriceEntry: priceEntry: $priceEntry")
        validatePriceEntry(priceEntry)
        dao.forceUpdatePriceEntry(priceEntry.toEntity())
    }

    override suspend fun updatePriceEntryIfNewer(priceEntry: PriceEntry) {
        logger.d(TAG, "updatePriceEntryIfNewer: priceEntry: $priceEntry")
        validatePriceEntry(priceEntry)

        val rowsAffected = dao.updatePriceEntryIfNewer(
            id = priceEntry.id,
            productId = priceEntry.productId,
            priceInCents = priceEntry.priceInCents,
            storeName = priceEntry.storeName,
            updatedAt = priceEntry.updatedAt,
            isDeleted = priceEntry.isDeleted
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for price entry ${priceEntry.id} ignored: local data is newer or identical.")
        } else {
            logger.d(TAG, "Price entry ${priceEntry.id} successfully updated using LWW strategy.")
        }
    }

    override suspend fun markPriceEntryAsDeletedById(priceEntryId: String) {
        logger.d(TAG, "markPriceEntryAsDeletedById: priceEntryId: $priceEntryId")
        dao.markPriceEntryAsDeletedById(priceEntryId, getCurrentTime())
    }

    override suspend fun deletePriceEntryById(priceEntryId: String) {
        logger.d(TAG, "deletePriceEntryById: priceEntryId: $priceEntryId")
        dao.deletePriceEntryById(priceEntryId)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validatePriceEntry(priceEntry: PriceEntry) {
        require(runCatching { Uuid.parse(priceEntry.id) }.isSuccess) {
            "Invalid UUID"
        }
        require(runCatching { Uuid.parse(priceEntry.productId) }.isSuccess) {
            "Invalid UUID"
        }
        require(isValidTimestamp(priceEntry.updatedAt)) { "Invalid epoch time millis" }
        require(priceEntry.priceInCents > 0) { "Product price In Cents should be more than zero" }
        priceEntry.storeName?.let {
            require(it.length <= 50) { "The store name should have at most 50 characters" }
        }
        priceEntry.storeName?.let {
            require(it.isNotBlank()) { "The store name should not be empty" }
        }
    }

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
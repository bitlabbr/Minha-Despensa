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

import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.dao.PantryRepositoryDao
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomPantryRepository(
    private val db: AppDatabase,
    private val logger: AppLogger
) : PantryRepository {
    private val TAG = "RoomPantryRepository"

    val dao = db.pantryDao()

    override fun getAllActivePantryItems(): Flow<List<PantryItem>> {
        logger.d(TAG, "getAllActivePantryItems")
        return dao.getAllActivePantryItems().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertPantryItem(item: PantryItem) {
        logger.d(TAG, "insertPantryItem item: $item")
        validatePantryItem(item)
        dao.insertPantryItem(item.toEntity())
    }

    override suspend fun forceUpdatePantryItem(item: PantryItem) {
        logger.d(TAG, "forceUpdatePantryItem item: $item")
        validatePantryItem(item)
        dao.forceUpdatePantryItem(item.toEntity())
    }

    override suspend fun updatePantryItemIfNewer(item: PantryItem) {
        logger.d(TAG, "updatePantryItemIfNewer item:$item")
        validatePantryItem(item)
        val rowsAffected = dao.updatePantryItemIfNewer(
            id = item.id,
            productId = item.productId,
            quantity = item.quantity,
            updatedAt = item.updatedAt,
            isDeleted = item.isDeleted,
            expirationDate = item.expirationDate,
            batchNumber = item.batchNumber
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for pantry item ${item.id} ignored: local data is newer.")
        }
    }

    override suspend fun markPantryItemAsDeleted(id: String, updatedAt: Long) {
        logger.d(TAG, "markPantryItemAsDeleted: id: $id updatedAt: $updatedAt")
        dao.markPantryItemAsDeleted(id, updatedAt)
    }

    override suspend fun deletePantryItemById(id: String) {
        logger.d(TAG, "deletePantryItemById: id: $id")
        dao.deletePantryItemById(id)
    }

    override fun getPantryItemsByID(pantryItemId: String): Flow<List<PantryItem>> {
        logger.d(TAG, "getPantryItemsByID: pantryItemId: $pantryItemId")
        return dao.getPantryItemsByID(pantryItemId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPantryItemsByProductID(productId: String): Flow<List<PantryItem>> {
        logger.d(TAG, "getPantryItemsByProductID: productId: $productId")
        return dao.getPantryItemsByProductID(productId).map { entities -> entities.map { it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validatePantryItem(item: PantryItem) {
        require(runCatching { Uuid.parse(item.id) }.isSuccess) { "Invalid Pantry Item UUID" }
        require(runCatching { Uuid.parse(item.productId) }.isSuccess) { "Invalid Product UUID" }
        require(item.quantity >= 0) { "Pantry quantity cannot be negative" }
        require(isValidTimestamp(item.updatedAt)) { "Invalid updatedAt timestamp" }
    }
}

fun PantryItemEntity.toDomain(): PantryItem {
    return PantryItem(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        expirationDate = this.expirationDate,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        batchNumber = this.batchNumber,
    )
}

fun PantryItem.toEntity(): PantryItemEntity {
    return PantryItemEntity(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        expirationDate = this.expirationDate,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        batchNumber = this.batchNumber
    )
}
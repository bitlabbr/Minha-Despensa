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

import androidx.room.Transactor
import androidx.room.useWriterConnection
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemConsumption
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.dto.PantryItemWithCategoryDaoResult
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomPantryRepository(
    val db: AppDatabase,
    private val logger: AppLogger
) : PantryRepository {
    private val TAG = "RoomPantryRepository"

    val dao = db.pantryDao()

    override fun getAllActivePantryItems(): Flow<List<PantryItem>> {
        logger.d(TAG, "getAllActivePantryItems")
        return dao.getAllActivePantryItems().map { pantryEntities -> pantryEntities.map { it.toDomain() } }
    }

    override fun getAllActivePantryItemsWithCategory(): Flow<List<PantryItemWithCategory>> {
        logger.d(TAG, "getAllActivePantryItemsWithCategory")
        return dao.getAllActivePantryItemsWithCategory().map { pantryItemsWithCategoryDaoResult ->
            pantryItemsWithCategoryDaoResult.map { it.toDomain() }
        }
    }

    override fun getPantryItemWithCategoryByID(pantryItemId: String): Flow<PantryItemWithCategory?> {
        logger.d(TAG, "getPantryItemWithCategoryByID: pantryItemId: $pantryItemId")
        return dao.getPantryItemWithCategoryByID(pantryItemId).map { it?.toDomain() }
    }

    override fun getExpiringPantryItems(thresholdDays: Int): Flow<List<PantryItemWithCategory>> {
        logger.d(TAG, "getExpiringPantryItems: thresholdDays: $thresholdDays")
        require(thresholdDays >= 0) { "Expiration threshold cannot be negative" }

        val now = getCurrentTime()
        val expirationThreshold = now + thresholdDays.toLong() * MILLIS_PER_DAY

        return dao.getExpiringPantryItemsDao(
            now = now,
            expirationThreshold = expirationThreshold
        ).map { pantryItemsWithCategoryDaoResult ->
            pantryItemsWithCategoryDaoResult.map { it.toDomain() }
        }
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

    override suspend fun consumePantryItem(pantryItemId: String, quantityToConsume: Double) {
        logger.d(TAG, "consumePantryItem id: $pantryItemId, quantity: $quantityToConsume")
        require(quantityToConsume > 0) { "Consumption quantity must be strictly greater than zero" }

        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val item = checkNotNull(dao.getPantryItemByID(pantryItemId).first()) {
                    "Pantry item not found with ID: $pantryItemId"
                }
                require(!item.isDeleted) { "Cannot consume a deleted pantry item: $pantryItemId" }
                require(item.quantity >= quantityToConsume) {
                    "Insufficient stock: requested $quantityToConsume, but only ${item.quantity} available"
                }

                val newQuantity = item.quantity - quantityToConsume
                val now = getCurrentTime()

                val updatedItem = item.copy(
                    quantity = newQuantity,
                    updatedAt = now
                )
                dao.forceUpdatePantryItem(updatedItem)
            }
        }
    }

    override suspend fun consumeBatch(consumptions: List<PantryItemConsumption>) {
        logger.d(TAG, "consumeBatch: ${consumptions.size} items")
        require(consumptions.isNotEmpty()) { "Consumption list cannot be empty" }

        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = getCurrentTime()

                consumptions.forEach { consumption ->
                    require(consumption.quantityToConsume > 0) {
                        "Quantity to consume must be greater than zero for item ${consumption.pantryItemId}"
                    }

                    val item = checkNotNull(dao.getPantryItemByID(consumption.pantryItemId).first()) {
                        "Pantry item not found with ID: ${consumption.pantryItemId}"
                    }
                    require(!item.isDeleted) {
                        "Cannot consume a deleted pantry item: ${consumption.pantryItemId}"
                    }
                    require(item.quantity >= consumption.quantityToConsume) {
                        "Insufficient stock for item ${consumption.pantryItemId}: " +
                                "available ${item.quantity}, required ${consumption.quantityToConsume}"
                    }

                    val updatedItem = item.copy(
                        quantity = item.quantity - consumption.quantityToConsume,
                        updatedAt = now
                    )
                    dao.forceUpdatePantryItem(updatedItem)
                }
            }
        }
    }

    override fun getPantryItemsByID(pantryItemId: String): Flow<PantryItem?> {
        logger.d(TAG, "getPantryItemsByID: pantryItemId: $pantryItemId")
        return dao.getPantryItemByID(pantryItemId).map { it?.toDomain() }
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

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
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
        batchNumber = this.batchNumber,
    )
}

fun PantryItemWithCategoryDaoResult.toDomain(): PantryItemWithCategory {
    return PantryItemWithCategory(
        pantryItem = this.pantryItem.toDomain(),
        category = this.category,
        name = this.name
    )
}
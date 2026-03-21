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
import com.bitlabbr.minhadespensa.data.local.dao.PantryRepositoryDao
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPantryRepository(
    private val dao: PantryRepositoryDao,
    private val logger: AppLogger
) : PantryRepository {
    private val TAG = "RoomPantryRepository"
    override fun getAllActiveInventory(): Flow<List<PantryItem>> {
        logger.d(TAG, "getAllActiveInventory")
        return dao.getAllActive().map { entities -> entities.map { it.toDomain() } }
    }


    override fun getItemsByProduct(productId: String): Flow<List<PantryItem>> {
        logger.d(TAG, "getItemsByProduct: productId: $productId")
       return dao.getItemsByProduct(productId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveItem(item: PantryItem) {
        logger.d(TAG, "saveItem: item: $item")
         dao.insertOrUpdate(item.toEntity())
    }

    override suspend fun deleteItem(id: String, updatedAt: Long) {
        logger.d(TAG, "deleteItem: id: $id updatedAt: $updatedAt")
        dao.markAsDeleted(id, updatedAt)
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
}
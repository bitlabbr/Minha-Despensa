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
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class RoomShoppingRepository(
    private val db: AppDatabase,
    private val logger: AppLogger
) : ShoppingRepository {

    private val shoppingDao = db.shoppingItemDao()
    private val pantryDao = db.pantryDao()
    private val priceDao = db.priceDao()
    private val TAG = "RoomShoppingRepository"


    override fun getActiveShoppingList(): Flow<List<ShoppingItem>> {
        logger.d(TAG, "getActiveShoppingList")
        return shoppingDao.getActiveItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveItem(item: ShoppingItem) {
        logger.d(TAG, "saveItem: item: $item")
        shoppingDao.insertOrUpdate(item.toEntity())
    }

    override suspend fun toggleCheck(id: String, isChecked: Boolean) {
        logger.d(TAG, "toggleCheck: id: $id isChecked: $isChecked")
        shoppingDao.updateCheckStatus(id, isChecked, getCurrentTime())
    }

    override suspend fun deleteItem(id: String) {
        logger.d(TAG, "deleteItem: id: $id")
        shoppingDao.markAsDeleted(id, getCurrentTime())
    }

    override suspend fun clearSession() {
        logger.d(TAG, "clearSession")
        shoppingDao.deleteAllLogical(getCurrentTime())
    }

    override suspend fun finalizePurchase() {
        logger.d(TAG, "finalizePurchase")
        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                try {
                    val now = getCurrentTime()
                    val checkedItems = db.shoppingItemDao().getActiveItems()
                        .map { list -> list.filter { it.isChecked } }
                        .first()

                    checkedItems.forEach { item ->
                        db.pantryDao().insertOrUpdate(
                            PantryItemEntity(
                                id = Uuid.random().toString(),
                                productId = item.productId,
                                quantity = item.quantity,
                                updatedAt = now,
                                isDeleted = false,
                                expirationDate = now, // TODO need to change this
                                batchNumber = null // TODO need to change this
                            )
                        )

                        item.priceAtTime?.let { price ->
                            db.priceDao().insertOrUpdate(
                                PriceEntryEntity(
                                    id = Uuid.random().toString(),
                                    productId = item.productId,
                                    priceInCents = price,
                                    updatedAt = now,
                                    isDeleted = false,
                                    storeName = "No name" // TODO need to change this
                                )
                            )
                        }
                    }

                    db.shoppingItemDao().deleteAllLogical(now)
                    logger.d("RoomShoppingRepository", "Checkout atômico concluído via SQLite Connection.")
                } catch (e: Exception) {
                    logger.e("RoomShoppingRepository", "Erro no checkout: ${e.message}")
                    throw e
                }
            }
        }
    }

    fun ShoppingItemEntity.toDomain() = ShoppingItem(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        priceAtTime = this.priceAtTime,
        isChecked = this.isChecked,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )

    fun ShoppingItem.toEntity() = ShoppingItemEntity(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        priceAtTime = this.priceAtTime,
        isChecked = this.isChecked,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted
    )
}
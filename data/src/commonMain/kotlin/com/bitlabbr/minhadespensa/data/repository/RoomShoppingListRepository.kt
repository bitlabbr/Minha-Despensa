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

import androidx.room.useWriterConnection
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class RoomShoppingListRepository(
    val db: AppDatabase,
    private val logger: AppLogger
) : ShoppingListRepository {

    private val listDao = db.shoppingListDao()
    private val itemDao = db.shoppingItemDao()
    private val TAG = "RoomShoppingListRepository"

    override fun getAllActiveShoppingLists(): Flow<List<ShoppingList>> {
        logger.d(TAG, "getAllActiveShoppingLists")
        return listDao.getAllActiveShoppingLists().map { list ->
            list.mapNotNull { it.toDomain() }
        }
    }

    override fun getShoppingListById(listId: String): Flow<ShoppingList?> {
        logger.d(TAG, "getShoppingListById listId: $listId")
        return listDao.getShoppingListById(listId).map { it?.toDomain() }
    }

    override suspend fun insertShoppingList(shoppingList: ShoppingList) {
        logger.d(TAG, "insertShoppingList: shoppingList: ${shoppingList.name}")
        validateShoppingList(shoppingList)
        db.useWriterConnection {
            listDao.insertShoppingList(shoppingList.toEntity())
            val itemEntities = shoppingList.items.map { it.toEntity() }
            listDao.insertItems(itemEntities)
        }
    }

    override suspend fun forceUpdateForShoppingList(shoppingList: ShoppingList) {
        logger.d(TAG, "forceUpdateForShoppingList shoppingList: ${shoppingList.name}")
        validateShoppingList(shoppingList)
        listDao.forceUpdateForShoppingList(shoppingList.toEntity())
    }

    override suspend fun updateShoppingListIfNewer(list: ShoppingList) {
        logger.d(TAG, "updateShoppingListIfNewer list: ${list.name}")
        validateShoppingList(list)
        val rowsAffected = listDao.updateShoppingListIfNewer(
            id = list.id,
            name = list.name,
            budgetInCents = list.budgetInCents,
            updatedAt = list.updatedAt,
            isDeleted = list.isDeleted
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for list ${list.id} ignored: local data is newer or identical.")
        } else {
            logger.d(TAG, "List ${list.id} successfully updated using LWW strategy.")
        }
    }

    override suspend fun markShoppingListAsDeleted(listID: String, updatedAt: Long) {
        logger.d(TAG, "markShoppingListAsDeleted listID:$listID")
        listDao.markShoppingListAsDeleted(listID, updatedAt)
    }

    override suspend fun deleteShoppingListById(listID: String) {
        logger.d(TAG, "deleteShoppingListById listID:$listID")
        listDao.deleteShoppingListById(listID)
    }

    override suspend fun insertShoppingItem(item: ShoppingItem) {
        logger.d(TAG, "insertShoppingItem itemId: ${item.id}")
        validateShoppingItem(item)
        itemDao.insertShoppingItem(item.toEntity())
    }

    override suspend fun forceUpdateForShoppingItem(item: ShoppingItem) {
        logger.d(TAG, "forceUpdateForShoppingItem itemId: ${item.id}")
        validateShoppingItem(item)
        itemDao.forceUpdateItem(item.toEntity())
    }

    override suspend fun updateShoppingItemIfNewer(item: ShoppingItem) {
        logger.d(TAG, "updateShoppingItemIfNewer itemId: ${item.id}")
        validateShoppingItem(item)
        val rowsAffected = itemDao.updateItemIfNewer(
            id = item.id,
            productId = item.productId,
            quantity = item.quantity,
            priceAtTime = item.priceAtTime,
            isChecked = item.isChecked,
            updatedAt = item.updatedAt,
            isDeleted = item.isDeleted
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for item ${item.id} ignored: local version is newer.")
        }
    }

    override suspend fun toggleItemCheck(id: String, isChecked: Boolean) {
        logger.d(TAG, "toggleItemCheck id:$id isChecked:$isChecked")
        itemDao.updateCheckStatus(id, isChecked, getCurrentTime())
    }

    override suspend fun markAsDeleted(id: String) {
        logger.d(TAG, "markAsDeleted id:$id")
        itemDao.markAsDeleted(id, getCurrentTime())
    }

    override suspend fun finalizePurchase(listId: String) {
        logger.d(TAG, "finalizePurchase listId: $listId")
        db.useWriterConnection {
            val now = getCurrentTime()
            val listWithItems = listDao.getShoppingListById(listId).first()
            val checkedItems = listWithItems?.items?.filter { it.isChecked && !it.isDeleted }

            checkedItems?.forEach { item ->
                db.pantryDao().insertOrUpdate(
                    PantryItem(
                        id = Uuid.random().toString(),
                        productId = item.productId,
                        quantity = item.quantity,
                        updatedAt = now,
                        isDeleted = false,
                        expirationDate = null,
                        batchNumber = null
                    ).toEntity()
                )

                item.priceAtTime?.let { price ->
                    db.priceDao().insertOrUpdate(
                        PriceEntry(
                            id = Uuid.random().toString(),
                            productId = item.productId,
                            priceInCents = price,
                            updatedAt = now,
                            isDeleted = false,
                            storeName = "Compra: ${listWithItems?.list?.name}"
                        ).toEntity()
                    )
                }
            }

            checkedItems?.forEach { item ->
                itemDao.updateCheckStatus(item.id, false, now)
            }

            listDao.updateTimestamp(listId, now)
            logger.d(TAG, "Checkout done. [${checkedItems?.size}] items added to pantry")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateShoppingList(list: ShoppingList) {
        require(runCatching { Uuid.parse(list.id) }.isSuccess) { "Invalid Shopping List UUID: ${list.id}" }

        require(list.name.isNotBlank()) { "Shopping List name cannot be empty" }
        require(list.name.length <= 50) { "Shopping List name is too long (max 50 chars)" }

        list.budgetInCents?.let {
            require(it >= 0) { "Budget cannot be negative" }
        }

        require(isValidTimestamp(list.updatedAt)) {
            "Invalid updatedAt timestamp for list"
        }

        list.items.forEach { validateShoppingItem(it) }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateShoppingItem(item: ShoppingItem) {
        require(runCatching { Uuid.parse(item.id) }.isSuccess) { "Invalid Shopping Item UUID: ${item.id}" }
        require(runCatching { Uuid.parse(item.productId) }.isSuccess) { "Invalid Product UUID: ${item.productId}" }
        require(runCatching { Uuid.parse(item.listID) }.isSuccess) { "Invalid List ID in item: ${item.listID}" }

        require(item.quantity > 0) { "Quantity must be greater than zero" }
        item.priceAtTime?.let {
            require(it >= 0) { "Price at time cannot be negative" }
        }

        require(isValidTimestamp(item.updatedAt)) {
            "Invalid updatedAt timestamp for item"
        }
    }
}

fun ShoppingItemEntity.toDomain() = ShoppingItem(
    id = this.id,
    productId = this.productId,
    quantity = this.quantity,
    listID = this.listId,
    priceAtTime = this.priceAtTime,
    isChecked = this.isChecked,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted
)

fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = this.id,
    productId = this.productId,
    listId = this.listID,
    quantity = this.quantity,
    priceAtTime = this.priceAtTime,
    isChecked = this.isChecked,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted
)

fun ShoppingListWithItems?.toDomain(): ShoppingList? {
    val data = this ?: return null
    return ShoppingList(
        id = data.list.id,
        name = data.list.name,
        budgetInCents = data.list.budgetInCents,
        updatedAt = data.list.updatedAt,
        isDeleted = data.list.isDeleted,
        items = data.items.map { it.toDomain() }
    )
}

fun ShoppingList.toEntity() = ShoppingListEntity(
    id = this.id,
    name = this.name,
    budgetInCents = this.budgetInCents,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted
)
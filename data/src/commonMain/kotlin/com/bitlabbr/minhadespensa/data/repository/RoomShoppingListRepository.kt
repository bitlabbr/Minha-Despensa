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
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class RoomShoppingListRepository(
    private val db: AppDatabase,
    private val logger: AppLogger,
) : ShoppingListRepository {

    private val listDao = db.shoppingListDao()
    private val itemDao = db.shoppingItemDao()

    override fun getAllActiveShoppingLists(): Flow<List<ShoppingList>> {
        logger.d(TAG, "getAllActiveShoppingLists")
        return listDao.getAllActiveShoppingLists().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getShoppingListById(listId: String): Flow<ShoppingList?> {
        logger.d(TAG, "getShoppingListById listId: $listId")
        return listDao.getShoppingListById(listId).map { it?.toDomain() }
    }

    override suspend fun insertShoppingList(shoppingList: ShoppingList) {
        logger.d(TAG, "insertShoppingList: ${shoppingList.name}")
        validateShoppingList(shoppingList)
        val itemEntities = shoppingList.items.map { it.toEntity() }
        listDao.insertShoppingListWithItems(shoppingList.toEntity(), itemEntities)
    }

    override suspend fun forceUpdateShoppingList(shoppingList: ShoppingList) {
        logger.d(TAG, "forceUpdateForShoppingList: ${shoppingList.name}")
        validateShoppingList(shoppingList)
        listDao.forceUpdateForShoppingList(shoppingList.toEntity())
    }

    override suspend fun updateShoppingListIfNewer(list: ShoppingList) {
        logger.d(TAG, "updateShoppingListIfNewer: ${list.name}")
        validateShoppingList(list)
        val rowsAffected = listDao.updateShoppingListIfNewer(
            id = list.id,
            name = list.name,
            type = list.type.name,
            status = list.status.name,
            budgetInCents = list.budgetInCents,
            updatedAt = list.updatedAt,
            isDeleted = list.isDeleted,
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for list ${list.id} ignored: local data is newer or identical.")
        } else {
            logger.d(TAG, "List ${list.id} successfully updated using LWW strategy.")
        }
    }

    override suspend fun markShoppingListAsDeleted(listId: String, updatedAt: Long) {
        logger.d(TAG, "markShoppingListAsDeleted listId:$listId")
        listDao.markShoppingListAsDeleted(listId, updatedAt)
    }

    override suspend fun deleteShoppingListById(listId: String) {
        logger.d(TAG, "deleteShoppingListById listId:$listId")
        listDao.deleteShoppingListById(listId)
    }

    override suspend fun insertShoppingItem(item: ShoppingItem) {
        logger.d(TAG, "insertShoppingItem itemId: ${item.id}")
        validateShoppingItem(item)
        itemDao.insertShoppingItem(item.toEntity())
    }

    override suspend fun forceUpdateShoppingItem(item: ShoppingItem) {
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
            rawText = item.rawText,
            quantity = item.quantity,
            priceAtTime = item.priceAtTime,
            isChecked = item.isChecked,
            updatedAt = item.updatedAt,
            isDeleted = item.isDeleted,
        )
        if (rowsAffected == 0) {
            logger.d(TAG, "Update for item ${item.id} ignored: local version is newer.")
        }
    }

    override suspend fun toggleItemCheck(itemId: String, isChecked: Boolean) {
        logger.d(TAG, "toggleItemCheck itemId:$itemId isChecked:$isChecked")
        itemDao.updateCheckStatus(itemId, isChecked, getCurrentTime())
    }

    override suspend fun markShoppingItemAsDeleted(itemId: String, updatedAt: Long) {
        logger.d(TAG, "markShoppingItemAsDeleted itemId:$itemId, updatedAt:$updatedAt")
        itemDao.markAsDeleted(itemId, updatedAt)
    }

    override suspend fun deleteShoppingItemById(itemId: String) {
        logger.d(TAG, "deleteShoppingItemById itemId:$itemId")
        itemDao.deleteById(itemId)
    }

    override suspend fun finalizePurchase(listId: String) {
        logger.d(TAG, "finalizePurchase listId: $listId")
        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = getCurrentTime()

                val listWithItems = checkNotNull(listDao.getShoppingListById(listId).first()) {
                    "Shopping list not found with ID: $listId"
                }

                require(!listWithItems.list.isDeleted) {
                    "Cannot finalize purchase for a deleted shopping list: $listId"
                }

                val checkedItems = listWithItems.items.filter { it.isChecked && !it.isDeleted }

                checkedItems.forEach { item ->
                    val targetProductId: String? = if (item.productId != null) {
                        item.productId
                    } else if (!item.rawText.isNullOrBlank()) {
                        val trimmedName = item.rawText.trim()
                        val sanitizedName = trimmedName.take(CoreConstants.Product.NAME_MAX_LENGTH).trim()
                        val existingProduct = db.catalogDao().findByName(sanitizedName)
                            ?: if (sanitizedName != trimmedName) db.catalogDao().findByName(trimmedName) else null
                        if (existingProduct != null) {
                            itemDao.updateProductId(item.id, existingProduct.id, now)
                            existingProduct.id
                        } else {
                            val newProductId = Uuid.random().toString()
                            val notes = if (trimmedName != sanitizedName) {
                                "Nome original: $trimmedName".take(CoreConstants.Product.NOTES_MAX_LENGTH)
                            } else null
                            val newProduct = CatalogProductEntity(
                                id = newProductId,
                                ean = null,
                                name = sanitizedName,
                                category = CoreConstants.Product.DEFAULT_CATEGORY,
                                brand = null,
                                measureUnit = MeasureUnit.UNIT.name,
                                netWeight = CoreConstants.Product.DEFAULT_NET_WEIGHT,
                                thumbnailUrl = null,
                                updatedAt = now,
                                isDeleted = false,
                                manuallyAdded = true,
                                notes = notes,
                            )
                            db.catalogDao().insert(newProduct)
                            itemDao.updateProductId(item.id, newProductId, now)
                            newProductId
                        }
                    } else {
                        null
                    }

                    if (targetProductId != null) {
                        db.pantryDao().insertPantryItem(
                            PantryItem(
                                id = Uuid.random().toString(),
                                productId = targetProductId,
                                quantity = item.quantity,
                                updatedAt = now,
                                isDeleted = false,
                                expirationDate = null,
                                batchNumber = null,
                            ).toEntity()
                        )

                        item.priceAtTime?.let { price ->
                            db.priceDao().insertPriceEntry(
                                PriceEntry(
                                    id = Uuid.random().toString(),
                                    productId = targetProductId,
                                    priceInCents = price,
                                    updatedAt = now,
                                    isDeleted = false,
                                    storeName = "Compra: ${listWithItems.list.name}",
                                ).toEntity()
                            )
                        }
                    }
                }

                // Uncheck all items processed during checkout
                checkedItems.forEach { item ->
                    itemDao.updateCheckStatus(item.id, false, now)
                }

                listDao.updateStatus(listId, ShoppingListStatus.COMPLETED.name, now)
                logger.d(TAG, "Checkout finalized. [${checkedItems.size}] items processed.")
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateShoppingList(list: ShoppingList) {
        require(runCatching { Uuid.parse(list.id) }.isSuccess) { "Invalid Shopping List UUID: ${list.id}" }
        require(list.name.isNotBlank()) { "Shopping List name cannot be empty" }
        require(list.name.length <= MAX_NAME_LENGTH) { "Shopping List name is too long (max $MAX_NAME_LENGTH chars)" }

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
        require(runCatching { Uuid.parse(item.listId) }.isSuccess) { "Invalid List ID in item: ${item.listId}" }

        // Flexible validation: can have productId (catalog) or rawText (scratchpad)
        item.productId?.let { prodId ->
            require(runCatching { Uuid.parse(prodId) }.isSuccess) { "Invalid Product UUID: $prodId" }
        }
        require(!item.productId.isNullOrBlank() || !item.rawText.isNullOrBlank()) {
            "Item must have either a valid productId or rawText"
        }

        require(item.quantity > 0) { "Quantity must be greater than zero" }
        item.priceAtTime?.let {
            require(it >= 0) { "Price at time cannot be negative" }
        }

        require(isValidTimestamp(item.updatedAt)) {
            "Invalid updatedAt timestamp for item"
        }
    }

    private companion object {
        const val TAG = "RoomShoppingListRepository"
        const val MAX_NAME_LENGTH = 50
    }
}
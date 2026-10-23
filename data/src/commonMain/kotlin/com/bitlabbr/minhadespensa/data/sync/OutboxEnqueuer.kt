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

package com.bitlabbr.minhadespensa.data.sync

import com.bitlabbr.minhadespensa.core.domain.util.SyncClock
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.dao.PendingSyncDao
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import com.bitlabbr.minhadespensa.data.local.entity.PendingSyncEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductCategoryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.remote.dto.CatalogProductSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.CategorySyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.MonthlyBudgetSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryMovementSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PriceEntrySyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingListSyncDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OutboxEnqueuer(
    private val pendingSyncDao: PendingSyncDao,
    private val syncClock: SyncClock? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private fun now(): Long = syncClock?.now() ?: getCurrentTime()

    suspend fun enqueueProduct(product: CatalogProductEntity, isDeleted: Boolean = product.isDeleted) {
        val dto = CatalogProductSyncDto(
            id = product.id,
            ean = product.ean,
            name = product.name,
            category = product.category,
            brand = product.brand,
            measureUnit = product.measureUnit,
            netWeight = product.netWeight,
            thumbnailUrl = product.thumbnailUrl,
            updatedAt = product.updatedAt,
            isDeleted = isDeleted,
            manuallyAdded = product.manuallyAdded,
            notes = product.notes,
            imageHash = product.imageHash,
        )
        enqueue(
            entityType = "PRODUCT",
            entityId = product.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = product.updatedAt,
        )
    }

    suspend fun enqueueCategory(category: ProductCategoryEntity, isDeleted: Boolean = category.isDeleted) {
        val dto = CategorySyncDto(
            id = category.id,
            name = category.name,
            isDefault = category.isDefault,
            updatedAt = category.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "CATEGORY",
            entityId = category.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = category.updatedAt,
        )
    }

    suspend fun enqueuePantryItem(item: PantryItemEntity, isDeleted: Boolean = item.isDeleted) {
        val dto = PantryItemSyncDto(
            id = item.id,
            productId = item.productId,
            quantity = item.quantity,
            expirationDate = item.expirationDate,
            batchNumber = item.batchNumber,
            unitPriceInCents = item.unitPriceInCents,
            updatedAt = item.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "PANTRY_ITEM",
            entityId = item.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = item.updatedAt,
        )
    }

    suspend fun enqueueShoppingList(list: ShoppingListEntity, isDeleted: Boolean = list.isDeleted) {
        val dto = ShoppingListSyncDto(
            id = list.id,
            name = list.name,
            listType = list.type,
            listStatus = list.status,
            budgetInCents = list.budgedInCents,
            updatedAt = list.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "SHOPPING_LIST",
            entityId = list.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = list.updatedAt,
        )
    }

    suspend fun enqueueShoppingItem(item: ShoppingItemEntity, isDeleted: Boolean = item.isDeleted) {
        val dto = ShoppingItemSyncDto(
            id = item.id,
            listId = item.listId,
            productId = item.productId,
            rawText = item.rawText,
            quantity = item.quantity,
            priceAtTime = item.priceAtTime,
            isChecked = item.isChecked,
            updatedAt = item.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "SHOPPING_ITEM",
            entityId = item.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = item.updatedAt,
        )
    }

    suspend fun enqueuePriceEntry(price: PriceEntryEntity, isDeleted: Boolean = price.isDeleted) {
        val dto = PriceEntrySyncDto(
            id = price.id,
            productId = price.productId,
            priceInCents = price.priceInCents,
            storeName = price.storeName,
            updatedAt = price.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "PRICE_ENTRY",
            entityId = price.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = price.updatedAt,
        )
    }

    suspend fun enqueueMovement(movement: PantryMovementEntity, isDeleted: Boolean = movement.isDeleted) {
        val dto = PantryMovementSyncDto(
            id = movement.id,
            pantryItemId = movement.pantryItemId,
            productId = movement.productId,
            type = movement.type,
            quantity = movement.quantity,
            unitPriceInCents = movement.unitPriceInCents,
            totalPriceInCents = movement.totalPriceInCents,
            storeOrReason = movement.storeOrReason,
            updatedAt = movement.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "MOVEMENT",
            entityId = movement.id,
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = movement.updatedAt,
        )
    }

    suspend fun enqueueBudget(budget: MonthlyBudgetEntity, isDeleted: Boolean = budget.isDeleted) {
        val dto = MonthlyBudgetSyncDto(
            year = budget.year,
            month = budget.month,
            budgetInCents = budget.budgetInCents,
            updatedAt = budget.updatedAt,
            isDeleted = isDeleted,
        )
        enqueue(
            entityType = "MONTHLY_BUDGET",
            entityId = "${budget.year}-${budget.month}",
            operation = if (isDeleted) "DELETE" else "UPSERT",
            payload = json.encodeToString(dto),
            updatedAt = budget.updatedAt,
        )
    }

    private suspend fun enqueue(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String?,
        updatedAt: Long,
    ) {
        pendingSyncDao.deleteByEntity(entityType, entityId)
        pendingSyncDao.insert(
            PendingSyncEntryEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = payload,
                updatedAt = updatedAt,
                createdAt = now(),
            ),
        )
    }

    suspend fun enqueueAllLocalEntities(database: AppDatabase): Int {
        var count = 0
        for (p in database.catalogDao().getAllSnapshot()) {
            enqueueProduct(p)
            count++
        }
        for (c in database.categoryDao().getAllSnapshot()) {
            enqueueCategory(c)
            count++
        }
        for (pi in database.pantryDao().getAllSnapshot()) {
            enqueuePantryItem(pi)
            count++
        }
        for (l in database.shoppingListDao().getAllSnapshot()) {
            enqueueShoppingList(l)
            count++
        }
        for (i in database.shoppingItemDao().getAllSnapshot()) {
            enqueueShoppingItem(i)
            count++
        }
        for (pe in database.priceDao().getAllSnapshot()) {
            enqueuePriceEntry(pe)
            count++
        }
        for (m in database.movementDao().getAllSnapshot()) {
            enqueueMovement(m)
            count++
        }
        for (b in database.budgetDao().getAllSnapshot()) {
            enqueueBudget(b)
            count++
        }
        return count
    }
}

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
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.WorkspaceSwitchCheckResult
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.util.SyncClock
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.AppKeyValueEntity
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryMovementEntity
import com.bitlabbr.minhadespensa.data.local.entity.PendingSyncEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductCategoryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.remote.SyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.CatalogProductSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.CategorySyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.MonthlyBudgetSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryMovementSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PriceEntrySyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingListSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncMutationRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomSyncRepository(
    private val database: AppDatabase,
    private val syncRemoteDataSource: SyncRemoteDataSource,
    private val syncClock: SyncClock,
) : SyncRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observePendingChangesCount(): Flow<Int> =
        database.pendingSyncDao().observePendingCount()

    override suspend fun getPendingChangesCount(): Int =
        database.pendingSyncDao().getPendingCount()

    override suspend fun sync(): Result<Unit> = runCatching {
        val activeHouseholdId = database.appKeyValueDao().get(RoomAuthRepository.KEY_ACTIVE_HOUSEHOLD_ID)
            ?: return@runCatching

        val cursorKey = "sync_cursor_$activeHouseholdId"
        val currentCursor = database.appKeyValueDao().get(cursorKey)?.toLongOrNull() ?: 0L

        // 1. Process pending outbox mutations (push)
        val pendingEntries = database.pendingSyncDao().getAllPending(100)
        if (pendingEntries.isNotEmpty()) {
            val mutationDtos = pendingEntries.map { entry ->
                SyncMutationRequestDto(
                    entityType = entry.entityType,
                    entityId = entry.entityId,
                    operation = entry.operation,
                    payload = entry.payload,
                    timestamp = entry.updatedAt,
                )
            }
            val pushPayload = SyncPushRequestDto(
                clientTimestamp = syncClock.now(),
                localVersion = currentCursor,
                mutations = mutationDtos,
            )

            val pushResponse = syncRemoteDataSource.push(activeHouseholdId, pushPayload).getOrThrow()
            syncClock.calibrate(pushResponse.serverTimestamp)

            // Reconcile product ID remappings atomically
            if (pushResponse.productIdMappings.isNotEmpty()) {
                applyProductIdMappings(pushResponse.productIdMappings)
            }

            // Delete processed entries from outbox
            database.pendingSyncDao().deleteByIds(pendingEntries.map { it.id })
        }

        // 2. Pull remote updates
        val pullResponse = syncRemoteDataSource.pull(activeHouseholdId, currentCursor, 100).getOrThrow()
        syncClock.calibrate(pullResponse.serverTimestamp)

        // 3. Apply pulled entities atomically
        applyPulledEntities(pullResponse.entities)

        // 4. Update cursor
        database.appKeyValueDao().put(
            AppKeyValueEntity(
                key = cursorKey,
                value = pullResponse.currentVersion.toString(),
                updatedAt = syncClock.now(),
            ),
        )
    }

    private suspend fun applyProductIdMappings(mappings: Map<String, String>) {
        database.useWriterConnection { conn ->
            for ((oldId, newId) in mappings) {
                if (oldId == newId) continue
                // If oldId exists and newId is not yet present, insert clone with newId
                val existingOld = database.catalogDao().findSnapshotById(oldId)
                if (existingOld != null) {
                    val existingNew = database.catalogDao().findSnapshotById(newId)
                    if (existingNew == null) {
                        database.catalogDao().insert(existingOld.copy(id = newId, ean = null))
                    }
                    // Reassign foreign keys
                    database.pantryDao().remapProductId(oldId, newId)
                    database.priceDao().remapProductId(oldId, newId)
                    database.shoppingItemDao().remapProductId(oldId, newId)
                    database.movementDao().remapProductId(oldId, newId)
                    database.productMediaDao().remapProductId(oldId, newId)

                    // Safely delete old parent row
                    database.catalogDao().deleteProductById(oldId)
                }
            }
        }
    }

    private suspend fun applyPulledEntities(entities: com.bitlabbr.minhadespensa.data.remote.dto.SyncEntitiesDto) {
        database.useWriterConnection { _ ->
            // Products
            for (p in entities.products) {
                val existing = database.catalogDao().findSnapshotById(p.id)
                val unit = runCatching { MeasureUnit.valueOf(p.measureUnit) }.getOrDefault(MeasureUnit.UNIT)
                if (existing == null) {
                    database.catalogDao().insert(
                        CatalogProductEntity(
                            id = p.id,
                            ean = p.ean,
                            name = p.name,
                            category = p.category,
                            brand = p.brand,
                            measureUnit = p.measureUnit,
                            netWeight = p.netWeight,
                            thumbnailUrl = p.thumbnailUrl,
                            updatedAt = p.updatedAt,
                            isDeleted = p.isDeleted,
                            manuallyAdded = p.manuallyAdded,
                            notes = p.notes,
                            imageHash = p.imageHash,
                        ),
                    )
                } else {
                    database.catalogDao().updateProductIfNewer(
                        id = p.id,
                        name = p.name,
                        ean = p.ean,
                        brand = p.brand,
                        category = p.category,
                        measureUnit = unit,
                        thumbnailUrl = p.thumbnailUrl,
                        netWeight = p.netWeight,
                        updatedAt = p.updatedAt,
                        isDeleted = p.isDeleted,
                        manuallyAdded = p.manuallyAdded,
                        notes = p.notes,
                    )
                }
            }

            // Categories
            for (cat in entities.categories) {
                val entity = ProductCategoryEntity(
                    id = cat.id,
                    name = cat.name,
                    isDefault = cat.isDefault,
                    updatedAt = cat.updatedAt,
                    isDeleted = cat.isDeleted,
                )
                val existing = database.categoryDao().findById(cat.id)
                if (existing == null) {
                    database.categoryDao().insertOrUpdate(entity)
                } else if (cat.updatedAt >= existing.updatedAt) {
                    database.categoryDao().updateIfNewer(cat.id, cat.name, cat.isDefault, cat.updatedAt, cat.isDeleted)
                }
            }

            // Pantry Items
            for (pi in entities.pantryItems) {
                val entity = PantryItemEntity(
                    id = pi.id,
                    productId = pi.productId,
                    quantity = pi.quantity,
                    expirationDate = pi.expirationDate,
                    batchNumber = pi.batchNumber,
                    unitPriceInCents = pi.unitPriceInCents,
                    updatedAt = pi.updatedAt,
                    isDeleted = pi.isDeleted,
                )
                val existing = database.pantryDao().findPantryItemById(pi.id)
                if (existing == null) {
                    database.pantryDao().insertPantryItem(entity)
                } else {
                    database.pantryDao().updatePantryItemIfNewer(
                        id = pi.id,
                        productId = pi.productId,
                        quantity = pi.quantity,
                        expirationDate = pi.expirationDate,
                        batchNumber = pi.batchNumber,
                        unitPriceInCents = pi.unitPriceInCents,
                        updatedAt = pi.updatedAt,
                        isDeleted = pi.isDeleted,
                    )
                }
            }

            // Movements
            for (m in entities.movements) {
                database.movementDao().insertMovement(
                    PantryMovementEntity(
                        id = m.id,
                        pantryItemId = m.pantryItemId,
                        productId = m.productId,
                        type = m.type,
                        quantity = m.quantity,
                        unitPriceInCents = m.unitPriceInCents,
                        totalPriceInCents = m.totalPriceInCents,
                        updatedAt = m.updatedAt,
                        storeOrReason = m.storeOrReason,
                        isDeleted = m.isDeleted,
                    ),
                )
            }

            // Shopping Lists
            for (sl in entities.shoppingLists) {
                val existing = database.shoppingListDao().getShoppingListById(sl.id)
                database.shoppingListDao().updateShoppingListIfNewer(
                    id = sl.id,
                    name = sl.name,
                    type = sl.listType,
                    status = sl.listStatus,
                    budgetInCents = sl.budgetInCents,
                    updatedAt = sl.updatedAt,
                    isDeleted = sl.isDeleted,
                )
            }

            // Shopping Items
            for (si in entities.shoppingItems) {
                database.shoppingItemDao().updateItemIfNewer(
                    id = si.id,
                    productId = si.productId,
                    rawText = si.rawText,
                    quantity = si.quantity,
                    priceAtTime = si.priceAtTime,
                    isChecked = si.isChecked,
                    updatedAt = si.updatedAt,
                    isDeleted = si.isDeleted,
                )
            }

            // Price Entries
            for (pe in entities.priceEntries) {
                database.priceDao().updatePriceEntryIfNewer(
                    id = pe.id,
                    productId = pe.productId,
                    priceInCents = pe.priceInCents,
                    storeName = pe.storeName,
                    updatedAt = pe.updatedAt,
                    isDeleted = pe.isDeleted,
                )
            }

            // Monthly Budgets
            for (mb in entities.budgets) {
                database.budgetDao().insertOrUpdate(
                    MonthlyBudgetEntity(
                        year = mb.year,
                        month = mb.month,
                        budgetInCents = mb.budgetInCents,
                        updatedAt = mb.updatedAt,
                        isDeleted = mb.isDeleted,
                    ),
                )
            }
        }
    }

    override suspend fun enqueueAllLocalEntitiesForInitialSync(): Result<Int> = runCatching {
        var count = 0
        val now = syncClock.now()

        // 1. Products
        val products = database.catalogDao().getAllSnapshot()
        for (p in products) {
            val dto = CatalogProductSyncDto(
                id = p.id,
                ean = p.ean,
                name = p.name,
                category = p.category,
                brand = p.brand,
                measureUnit = p.measureUnit,
                netWeight = p.netWeight,
                thumbnailUrl = p.thumbnailUrl,
                updatedAt = p.updatedAt,
                isDeleted = p.isDeleted,
                manuallyAdded = p.manuallyAdded,
                notes = p.notes,
                imageHash = p.imageHash,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "PRODUCT",
                    entityId = p.id,
                    operation = if (p.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = p.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        // 2. Categories
        val categories = database.categoryDao().getAllSnapshot()
        for (c in categories) {
            val dto = CategorySyncDto(
                id = c.id,
                name = c.name,
                isDefault = c.isDefault,
                updatedAt = c.updatedAt,
                isDeleted = c.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "CATEGORY",
                    entityId = c.id,
                    operation = if (c.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = c.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        // 3. Pantry Items
        val pantryItems = database.pantryDao().getAllSnapshot()
        for (pi in pantryItems) {
            val dto = PantryItemSyncDto(
                id = pi.id,
                productId = pi.productId,
                quantity = pi.quantity,
                expirationDate = pi.expirationDate,
                batchNumber = pi.batchNumber,
                unitPriceInCents = pi.unitPriceInCents,
                updatedAt = pi.updatedAt,
                isDeleted = pi.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "PANTRY_ITEM",
                    entityId = pi.id,
                    operation = if (pi.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = pi.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        // 4. Shopping Lists & Items
        val lists = database.shoppingListDao().getAllSnapshot()
        for (l in lists) {
            val dto = ShoppingListSyncDto(
                id = l.id,
                name = l.name,
                listType = l.type,
                listStatus = l.status,
                budgetInCents = l.budgedInCents,
                updatedAt = l.updatedAt,
                isDeleted = l.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "SHOPPING_LIST",
                    entityId = l.id,
                    operation = if (l.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = l.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        val items = database.shoppingItemDao().getAllSnapshot()
        for (i in items) {
            val dto = ShoppingItemSyncDto(
                id = i.id,
                listId = i.listId,
                productId = i.productId,
                rawText = i.rawText,
                quantity = i.quantity,
                priceAtTime = i.priceAtTime,
                isChecked = i.isChecked,
                updatedAt = i.updatedAt,
                isDeleted = i.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "SHOPPING_ITEM",
                    entityId = i.id,
                    operation = if (i.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = i.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        // 5. Price Entries
        val prices = database.priceDao().getAllSnapshot()
        for (pe in prices) {
            val dto = PriceEntrySyncDto(
                id = pe.id,
                productId = pe.productId,
                priceInCents = pe.priceInCents,
                storeName = pe.storeName,
                updatedAt = pe.updatedAt,
                isDeleted = pe.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "PRICE_ENTRY",
                    entityId = pe.id,
                    operation = if (pe.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = pe.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        // 6. Monthly Budgets
        val budgets = database.budgetDao().getAllSnapshot()
        for (b in budgets) {
            val dto = MonthlyBudgetSyncDto(
                year = b.year,
                month = b.month,
                budgetInCents = b.budgetInCents,
                updatedAt = b.updatedAt,
                isDeleted = b.isDeleted,
            )
            database.pendingSyncDao().insert(
                PendingSyncEntryEntity(
                    entityType = "MONTHLY_BUDGET",
                    entityId = "${b.year}-${b.month}",
                    operation = if (b.isDeleted) "DELETE" else "UPSERT",
                    payload = json.encodeToString(dto),
                    updatedAt = b.updatedAt,
                    createdAt = now,
                ),
            )
            count++
        }

        count
    }

    override suspend fun flushOutboxBeforeSwitch(forceDiscard: Boolean): WorkspaceSwitchCheckResult {
        val pendingCount = database.pendingSyncDao().getPendingCount()
        if (pendingCount > 0 && !forceDiscard) {
            return WorkspaceSwitchCheckResult.HasUnsyncedChanges(pendingCount)
        }
        if (forceDiscard) {
            database.pendingSyncDao().clearAll()
        }
        return WorkspaceSwitchCheckResult.Ready
    }

    override suspend fun clearLocalDataForHouseholdSwitch() {
        database.pantryDao().clearAll()
        database.movementDao().clearAll()
        database.shoppingItemDao().clearAll()
        database.shoppingListDao().clearAll()
        database.budgetDao().clearAll()
        database.priceDao().clearAll()
        database.pendingSyncDao().clearAll()
    }
}

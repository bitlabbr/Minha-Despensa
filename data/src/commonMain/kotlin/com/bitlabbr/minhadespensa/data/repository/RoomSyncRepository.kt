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
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
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
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
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
import com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer

class RoomSyncRepository(
    private val database: AppDatabase,
    private val syncRemoteDataSource: SyncRemoteDataSource,
    private val syncClock: SyncClock,
    private val logger: AppLogger? = null,
    private val outboxEnqueuer: OutboxEnqueuer = OutboxEnqueuer(database.pendingSyncDao(), syncClock),
    private val authRepository: AuthRepository? = null,
) : SyncRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observePendingChangesCount(): Flow<Int> =
        database.pendingSyncDao().observePendingCount()

    override suspend fun getPendingChangesCount(): Int =
        database.pendingSyncDao().getPendingCount()

    override suspend fun sync(): Result<Unit> = runCatching {
        val startTime = getCurrentTime()
        val activeHouseholdId = database.appKeyValueDao().get(RoomAuthRepository.KEY_ACTIVE_HOUSEHOLD_ID)
        if (activeHouseholdId.isNullOrBlank()) {
            logger?.d("Sync", "Sync skipped: no active household configured")
            return@runCatching
        }

        val cursorKey = "sync_cursor_$activeHouseholdId"
        val currentCursor = database.appKeyValueDao().get(cursorKey)?.toLongOrNull() ?: 0L
        logger?.i("Sync", "Starting bidirectional sync for household=$activeHouseholdId (cursor=$currentCursor)")

        // 1. Process pending outbox mutations (push)
        if (currentCursor == 0L && database.pendingSyncDao().getPendingCount() == 0) {
            val backfilled = outboxEnqueuer.enqueueAllLocalEntities(database)
            if (backfilled > 0) {
                logger?.i("Sync", "Backfilled $backfilled local pre-existing entities into outbox for initial push")
            }
        }

        var pendingEntries = database.pendingSyncDao().getAllPending(100)
        while (pendingEntries.isNotEmpty()) {
            logger?.d("Sync", "Processing ${pendingEntries.size} pending outbox mutations...")
            val mutationDtos = mutableListOf<SyncMutationRequestDto>()
            val categories = mutableListOf<CategorySyncDto>()
            val products = mutableListOf<CatalogProductSyncDto>()
            val shoppingLists = mutableListOf<ShoppingListSyncDto>()
            val shoppingItems = mutableListOf<ShoppingItemSyncDto>()
            val pantryItems = mutableListOf<PantryItemSyncDto>()
            val priceEntries = mutableListOf<PriceEntrySyncDto>()
            val movements = mutableListOf<PantryMovementSyncDto>()
            val budgets = mutableListOf<MonthlyBudgetSyncDto>()

            for (entry in pendingEntries) {
                mutationDtos.add(
                    SyncMutationRequestDto(
                        entityType = entry.entityType,
                        entityId = entry.entityId,
                        operation = entry.operation,
                        payload = entry.payload,
                        timestamp = entry.updatedAt,
                    ),
                )
                val payloadStr = entry.payload ?: continue
                runCatching {
                    when (entry.entityType) {
                        "CATEGORY", "PRODUCT_CATEGORY" -> categories.add(json.decodeFromString(payloadStr))
                        "PRODUCT", "CATALOG_PRODUCT" -> products.add(json.decodeFromString(payloadStr))
                        "PANTRY_ITEM" -> pantryItems.add(json.decodeFromString(payloadStr))
                        "SHOPPING_LIST" -> shoppingLists.add(json.decodeFromString(payloadStr))
                        "SHOPPING_ITEM" -> shoppingItems.add(json.decodeFromString(payloadStr))
                        "PRICE_ENTRY" -> priceEntries.add(json.decodeFromString(payloadStr))
                        "MOVEMENT", "PANTRY_MOVEMENT" -> movements.add(json.decodeFromString(payloadStr))
                        "BUDGET", "MONTHLY_BUDGET" -> budgets.add(json.decodeFromString(payloadStr))
                    }
                }
            }

            val pushPayload = SyncPushRequestDto(
                clientTimestamp = syncClock.now(),
                localVersion = currentCursor,
                mutations = mutationDtos,
                categories = categories,
                products = products,
                shoppingLists = shoppingLists,
                shoppingItems = shoppingItems,
                pantryItems = pantryItems,
                priceEntries = priceEntries,
                movements = movements,
                budgets = budgets,
            )

            val pushResponse = withAuthRetry { syncRemoteDataSource.push(activeHouseholdId, pushPayload) }.getOrThrow()
            syncClock.calibrate(pushResponse.serverTimestamp)
            logger?.i("Sync", "Outbox push completed: serverVersion=${pushResponse.serverVersion}, processed=${pushResponse.processedCount}")

            // Reconcile product ID remappings atomically
            if (pushResponse.productIdMappings.isNotEmpty()) {
                logger?.d("Sync", "Applying ${pushResponse.productIdMappings.size} product ID remappings")
                applyProductIdMappings(pushResponse.productIdMappings)
            }

            // Delete processed entries from outbox
            database.pendingSyncDao().deleteByIds(pendingEntries.map { it.id })
            logger?.d("Sync", "Drained ${pendingEntries.size} processed entries from local outbox")

            pendingEntries = database.pendingSyncDao().getAllPending(100)
        }

        // 2. Pull remote updates
        var pullCursor = currentCursor
        if (pullCursor > 0 && database.shoppingListDao().getAllSnapshot().isEmpty()) {
            logger?.i("Sync", "Local shopping lists empty with cursor=$pullCursor; pulling from 0 to backfill missing data")
            pullCursor = 0L
        }

        logger?.d("Sync", "Pulling delta updates since cursor=$pullCursor...")
        val pullResponse = withAuthRetry { syncRemoteDataSource.pull(activeHouseholdId, pullCursor, 100) }.getOrThrow()
        syncClock.calibrate(pullResponse.serverTimestamp)

        // 3. Apply pulled entities atomically
        val entities = pullResponse.effectiveEntities
        logger?.i(
            "Sync",
            "Applying pulled delta: nextCursor=${pullResponse.currentVersion}, products=${entities.products.size}, " +
                "categories=${entities.categories.size}, pantry=${entities.pantryItems.size}, lists=${entities.shoppingLists.size}",
        )
        applyPulledEntities(entities)

        // 4. Sync product media (photos)
        syncProductMedia(activeHouseholdId)

        // 5. Update cursor
        database.appKeyValueDao().put(
            AppKeyValueEntity(
                key = cursorKey,
                value = pullResponse.currentVersion.toString(),
                updatedAt = syncClock.now(),
            ),
        )
        val durationMs = getCurrentTime() - startTime
        logger?.i("Sync", "Bidirectional sync completed successfully in ${durationMs}ms")
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

                    // Safely reconcile 1:1 product media primary key without unique constraint conflict
                    val oldMedia = database.productMediaDao().getByProductId(oldId)
                    if (oldMedia != null) {
                        val newMedia = database.productMediaDao().getByProductId(newId)
                        if (newMedia == null || oldMedia.updatedAt > newMedia.updatedAt) {
                            database.productMediaDao().insertOrUpdate(oldMedia.copy(productId = newId))
                        }
                        database.productMediaDao().deleteByProductId(oldId)
                    }

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
                    val existingByEan = if (!p.ean.isNullOrBlank()) database.catalogDao().findSnapshotByEan(p.ean) else null
                    if (existingByEan != null && existingByEan.id != p.id) {
                        applyProductIdMappings(mapOf(existingByEan.id to p.id))
                    }
                    val existingAfterRemap = database.catalogDao().findSnapshotById(p.id)
                    if (existingAfterRemap == null) {
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
                            imageHash = p.imageHash,
                        )
                    }
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
                        imageHash = p.imageHash,
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
                val existing = database.shoppingListDao().findSnapshotById(sl.id)
                if (existing == null) {
                    database.shoppingListDao().insertShoppingList(
                        ShoppingListEntity(
                            id = sl.id,
                            name = sl.name,
                            type = sl.listType,
                            status = sl.listStatus,
                            budgedInCents = sl.budgetInCents,
                            updatedAt = sl.updatedAt,
                            isDeleted = sl.isDeleted,
                        ),
                    )
                } else {
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
            }

            // Shopping Items
            for (si in entities.shoppingItems) {
                val existing = database.shoppingItemDao().findSnapshotById(si.id)
                if (existing == null) {
                    database.shoppingItemDao().insertShoppingItem(
                        ShoppingItemEntity(
                            id = si.id,
                            listId = si.listId,
                            productId = si.productId,
                            rawText = si.rawText,
                            quantity = si.quantity,
                            priceAtTime = si.priceAtTime,
                            isChecked = si.isChecked,
                            updatedAt = si.updatedAt,
                            isDeleted = si.isDeleted,
                        ),
                    )
                } else {
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
            }

            // Price Entries
            for (pe in entities.priceEntries) {
                val existing = database.priceDao().findEntryById(pe.id)
                if (existing == null) {
                    database.priceDao().insertPriceEntry(
                        PriceEntryEntity(
                            id = pe.id,
                            productId = pe.productId,
                            priceInCents = pe.priceInCents,
                            storeName = pe.storeName,
                            updatedAt = pe.updatedAt,
                            isDeleted = pe.isDeleted,
                        ),
                    )
                } else {
                    database.priceDao().updatePriceEntryIfNewer(
                        id = pe.id,
                        productId = pe.productId,
                        priceInCents = pe.priceInCents,
                        storeName = pe.storeName,
                        updatedAt = pe.updatedAt,
                        isDeleted = pe.isDeleted,
                    )
                }
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
        outboxEnqueuer.enqueueAllLocalEntities(database)
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

    private suspend fun <T> withAuthRetry(block: suspend () -> Result<T>): Result<T> {
        val initial = block()
        if (initial.isFailure) {
            val err = initial.exceptionOrNull()
            val is401 = err?.message?.contains("401") == true || err?.message?.contains("Unauthorized") == true
            if (is401 && authRepository != null) {
                logger?.i("Sync", "Detected 401 Unauthorized; attempting session refresh...")
                val refreshResult = authRepository.refreshSession()
                if (refreshResult.isSuccess) {
                    logger?.i("Sync", "Session refreshed successfully; retrying sync operation")
                    return block()
                } else {
                    logger?.w("Sync", "Session refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                }
            }
        }
        return initial
    }

    private suspend fun syncProductMedia(householdId: String) {
        // A. Upload local media that is not yet synced or whose hash differs from recorded synced hash
        val allLocalMedia = database.productMediaDao().getAllSnapshot()
        for (media in allLocalMedia) {
            val hash = Buffer().write(media.blob).readByteString().sha256().hex()
            val syncedHashKey = "synced_media_hash_${media.productId}"
            val recordedSyncedHash = database.appKeyValueDao().get(syncedHashKey)
            if (recordedSyncedHash != hash) {
                runCatching {
                    val serverHash = withAuthRetry {
                        syncRemoteDataSource.uploadProductMedia(householdId, media.productId, media.blob)
                    }.getOrThrow()
                    val finalHash = if (serverHash.isNotBlank()) serverHash else hash
                    database.catalogDao().updateImageHash(media.productId, finalHash)
                    database.appKeyValueDao().put(
                        AppKeyValueEntity(
                            key = syncedHashKey,
                            value = finalHash,
                            updatedAt = syncClock.now(),
                        ),
                    )
                    logger?.i("Sync", "Uploaded media for product ${media.productId} (hash=$finalHash)")
                }.onFailure { err ->
                    logger?.w("Sync", "Failed to upload media for product ${media.productId}: ${err.message}")
                }
            }
        }

        // B. Download missing remote media for products that have imageHash
        val productsWithRemoteMedia = database.catalogDao().getAllSnapshot().filter { !it.imageHash.isNullOrBlank() }
        for (product in productsWithRemoteMedia) {
            val localMedia = database.productMediaDao().getByProductId(product.id)
            val currentLocalHash = localMedia?.let { Buffer().write(it.blob).readByteString().sha256().hex() }
            if (currentLocalHash != product.imageHash) {
                runCatching {
                    val bytes = withAuthRetry {
                        syncRemoteDataSource.downloadProductMedia(householdId, product.id)
                    }.getOrThrow()
                    if (bytes != null && bytes.isNotEmpty()) {
                        database.productMediaDao().insertOrUpdate(
                            ProductMediaEntity(
                                productId = product.id,
                                blob = bytes,
                                updatedAt = product.updatedAt,
                            ),
                        )
                        val downloadedHash = Buffer().write(bytes).readByteString().sha256().hex()
                        database.appKeyValueDao().put(
                            AppKeyValueEntity(
                                key = "synced_media_hash_${product.id}",
                                value = downloadedHash,
                                updatedAt = syncClock.now(),
                            ),
                        )
                        logger?.i("Sync", "Downloaded media for product ${product.id} (${bytes.size} bytes)")
                    }
                }.onFailure { err ->
                    logger?.w("Sync", "Failed to download media for product ${product.id}: ${err.message}")
                }
            }
        }
    }

    override suspend fun clearLocalDataForHouseholdSwitch() {
        database.pantryDao().clearAll()
        database.movementDao().clearAll()
        database.shoppingItemDao().clearAll()
        database.shoppingListDao().clearAll()
        database.budgetDao().clearAll()
        database.priceDao().clearAll()
        database.productMediaDao().clearAll()
        database.catalogDao().clearAll()
        database.categoryDao().clearCustomCategories()
        database.pendingSyncDao().clearAll()
    }

    override suspend fun hasLocalData(): Boolean {
        val hasProducts = database.catalogDao().getAllSnapshot().any { !it.isDeleted }
        if (hasProducts) return true
        val hasPantry = database.pantryDao().getAllSnapshot().any { !it.isDeleted }
        if (hasPantry) return true
        val hasShopping = database.shoppingListDao().getAllSnapshot().any { !it.isDeleted }
        if (hasShopping) return true
        val hasBudgets = database.budgetDao().getAllSnapshot().isNotEmpty()
        return hasBudgets
    }
}

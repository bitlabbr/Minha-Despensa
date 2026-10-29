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
import com.bitlabbr.minhadespensa.core.domain.model.MovementType
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryMovement
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.model.invoice.consolidateDuplicates
import com.bitlabbr.minhadespensa.core.domain.repository.InvoiceRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import com.bitlabbr.minhadespensa.data.remote.InvoiceRemoteDataSource
import com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer
import kotlin.math.roundToLong
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomInvoiceRepository(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val db: AppDatabase,
    private val logger: AppLogger,
    private val outboxEnqueuer: OutboxEnqueuer = OutboxEnqueuer(db.pendingSyncDao()),
) : InvoiceRepository {

    override suspend fun parseInvoiceFromQrCode(qrCodeContent: String): Result<Invoice> = runCatching {
        logger.d(TAG, "parseInvoiceFromQrCode called with content length ${qrCodeContent.length}")
        val responseDto = remoteDataSource.parseInvoice(qrCodeContent)
        responseDto.toDomain()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun importInvoiceToPantry(
        storeName: String,
        items: List<InvoiceItem>,
    ): Result<Int> = runCatching {
        val consolidatedItems = items.consolidateDuplicates()
        logger.d(
            TAG,
            "importInvoiceToPantry starting import for ${consolidatedItems.size} consolidated items (from ${items.size} raw) from store '$storeName'"
        )
        var importedCount = 0

        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = getCurrentTime()
                val groupedByProductId = LinkedHashMap<String, MutableList<InvoiceItem>>()

                consolidatedItems.forEach { item ->
                    val trimmedName = item.name.trim()
                    val sanitizedName = trimmedName.take(CoreConstants.Product.NAME_MAX_LENGTH).trim()

                    val existingProduct = item.ean?.takeIf { it.isNotBlank() }?.let { ean ->
                        db.catalogDao().findSnapshotByEan(ean)
                    } ?: db.catalogDao().findByName(sanitizedName)
                      ?: if (sanitizedName != trimmedName) db.catalogDao().findByName(trimmedName) else null

                    val targetProductId = if (existingProduct != null) {
                        existingProduct.id
                    } else {
                        val newId = Uuid.random().toString()
                        val notes = if (trimmedName != sanitizedName) {
                            "Nome original: $trimmedName".take(CoreConstants.Product.NOTES_MAX_LENGTH)
                        } else null

                        val newProduct = CatalogProductEntity(
                            id = newId,
                            ean = item.ean?.takeIf { it.isNotBlank() },
                            name = sanitizedName,
                            category = CoreConstants.Product.DEFAULT_CATEGORY,
                            brand = null,
                            measureUnit = item.measureUnit.name,
                            netWeight = CoreConstants.Product.DEFAULT_NET_WEIGHT,
                            thumbnailUrl = null,
                            updatedAt = now,
                            isDeleted = false,
                            manuallyAdded = false,
                            notes = notes,
                        )
                        db.catalogDao().insert(newProduct)
                        outboxEnqueuer.enqueueProduct(newProduct)
                        logger.d(TAG, "importInvoiceToPantry: Created new catalog product id=$newId, name='$sanitizedName', ean='${item.ean}'")
                        newId
                    }

                    groupedByProductId.getOrPut(targetProductId) { mutableListOf() }.add(item)
                }

                groupedByProductId.forEach { (targetProductId, productItems) ->
                    val totalQuantity = (productItems.sumOf { it.quantity } * 10000.0).roundToLong() / 10000.0
                    val unitPriceInCents = productItems.firstNotNullOfOrNull { it.unitPriceInCents }
                    val totalPriceInCents = if (productItems.any { it.computedTotalPriceInCents != null }) {
                        productItems.sumOf { it.computedTotalPriceInCents ?: 0L }
                    } else {
                        null
                    }

                    val pantryItemId = Uuid.random().toString()
                    val pantryEntity = PantryItem(
                        id = pantryItemId,
                        productId = targetProductId,
                        quantity = totalQuantity,
                        unitPriceInCents = unitPriceInCents,
                        updatedAt = now,
                        isDeleted = false,
                        expirationDate = null,
                        batchNumber = null,
                    ).toEntity()
                    db.pantryDao().insertPantryItem(pantryEntity)
                    outboxEnqueuer.enqueuePantryItem(pantryEntity)

                    unitPriceInCents?.let { price ->
                        val priceEntry = PriceEntry(
                            id = pantryItemId,
                            productId = targetProductId,
                            priceInCents = price,
                            updatedAt = now,
                            isDeleted = false,
                            storeName = storeName,
                        ).toEntity()
                        db.priceDao().insertPriceEntry(priceEntry)
                        outboxEnqueuer.enqueuePriceEntry(priceEntry)
                    }

                    val movementEntity = PantryMovement(
                        id = Uuid.random().toString(),
                        pantryItemId = pantryItemId,
                        productId = targetProductId,
                        type = MovementType.ENTRY_PURCHASE,
                        quantity = totalQuantity,
                        unitPriceInCents = unitPriceInCents,
                        totalPriceInCents = totalPriceInCents,
                        updatedAt = now,
                        storeOrReason = "Nota Fiscal: $storeName",
                        isDeleted = false,
                    ).toEntity()
                    db.movementDao().insertMovement(movementEntity)
                    outboxEnqueuer.enqueueMovement(movementEntity)

                    importedCount++
                }
            }
        }

        db.invalidationTracker.refreshAsync()
        logger.d(TAG, "importInvoiceToPantry successfully imported $importedCount items")
        importedCount
    }

    companion object {
        private const val TAG = "RoomInvoiceRepository"
    }
}

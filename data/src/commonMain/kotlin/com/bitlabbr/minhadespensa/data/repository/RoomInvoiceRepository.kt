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
import androidx.room.withTransaction
import com.bitlabbr.minhadespensa.core.domain.model.MovementType
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryMovement
import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.repository.InvoiceRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import com.bitlabbr.minhadespensa.data.remote.InvoiceRemoteDataSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomInvoiceRepository(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val db: AppDatabase,
    private val logger: AppLogger,
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
        logger.d(TAG, "importInvoiceToPantry starting import for ${items.size} items from store '$storeName'")
        var importedCount = 0

        db.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = getCurrentTime()

                items.forEach { item ->
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
                        logger.d(TAG, "importInvoiceToPantry: Created new catalog product id=$newId, name='$sanitizedName', ean='${item.ean}'")
                        newId
                    }

                    val pantryItemId = Uuid.random().toString()
                    db.pantryDao().insertPantryItem(
                        PantryItem(
                            id = pantryItemId,
                            productId = targetProductId,
                            quantity = item.quantity,
                            unitPriceInCents = item.unitPriceInCents,
                            updatedAt = now,
                            isDeleted = false,
                            expirationDate = null,
                            batchNumber = null,
                        ).toEntity()
                    )

                    item.unitPriceInCents?.let { price ->
                        db.priceDao().insertPriceEntry(
                            PriceEntry(
                                id = pantryItemId,
                                productId = targetProductId,
                                priceInCents = price,
                                updatedAt = now,
                                isDeleted = false,
                                storeName = storeName,
                            ).toEntity()
                        )
                    }

                    val movement = PantryMovement(
                        id = Uuid.random().toString(),
                        pantryItemId = pantryItemId,
                        productId = targetProductId,
                        type = MovementType.ENTRY_PURCHASE,
                        quantity = item.quantity,
                        unitPriceInCents = item.unitPriceInCents,
                        totalPriceInCents = item.computedTotalPriceInCents,
                        updatedAt = now,
                        storeOrReason = "Nota Fiscal: $storeName",
                        isDeleted = false,
                    )
                    db.movementDao().insertMovement(movement.toEntity())

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

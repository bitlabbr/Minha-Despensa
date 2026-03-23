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
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCatalogRepository(
    private val db: AppDatabase,
    private val logger: AppLogger
) : CatalogRepository {
    private val TAG = "RoomCatalogRepository"
    private val productDao = db.catalogDao()
    private val mediaDao = db.productMediaDao()

    override fun getProductByEan(ean: String): Flow<CatalogProduct?> {
        logger.d(TAG, "searchProducts: ean: $ean")
        return productDao.findByEan(ean).map { it?.toDomain() }
    }

    override fun getProductById(id: String): Flow<CatalogProduct?> {
        logger.d(TAG, "searchProducts: id: $id")
        return productDao.findById(id).map { it?.toDomain() }
    }

    override fun searchProducts(query: String): Flow<List<CatalogProduct>> {
        logger.d(TAG, "searchProducts: query: $query")
        return productDao.searchByNameOrBrand(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveProduct(product: CatalogProduct) {
        logger.d(TAG, "saveProduct:  ${product.name}")
        saveProductWithImage(product, null)
    }

    override suspend fun deleteProduct(id: String) {
        logger.d(TAG, "deleteProduct id:  $id")
        productDao.markAsDeleted(id, getCurrentTime())
    }

    suspend fun saveProductWithImage(product: CatalogProduct, imageBytes: ByteArray?) {
        logger.d(TAG, "saveProductWithImage: ${product.name} (hasImage: ${imageBytes != null})")
        db.useWriterConnection { conn ->
            conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                productDao.insertOrUpdate(product.toEntity())
                if (imageBytes != null) {
                    mediaDao.insertOrUpdate(
                        ProductMediaEntity(
                            productId = product.id,
                            blob = imageBytes,
                            updatedAt = getCurrentTime()
                        )
                    )
                }
            }
        }
    }
}

fun CatalogProductEntity.toDomain() = CatalogProduct(
    id = this.id,
    ean = this.ean,
    name = this.name,
    brand = this.brand,
    measureUnit = MeasureUnit.valueOf(this.measureUnit),
    netWeight = this.netWeight,
    thumbnailUrl = this.thumbnailUrl,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    manuallyAdded = this.manuallyAdded
)

fun CatalogProduct.toEntity() = CatalogProductEntity(
    id = this.id,
    ean = this.ean,
    name = this.name,
    brand = this.brand,
    measureUnit = this.measureUnit.name,
    netWeight = this.netWeight,
    thumbnailUrl = this.thumbnailUrl,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    manuallyAdded = this.manuallyAdded
)
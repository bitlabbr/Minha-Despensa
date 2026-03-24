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
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    override fun getAllActives(): Flow<List<CatalogProduct>> {
        logger.d(TAG, "getAllActives")
        return productDao.getAllActive().map { it.map { it.toDomain() } }
    }

    override fun searchProductsByNameOrBrand(query: String): Flow<List<CatalogProduct>> {
        logger.d(TAG, "searchProductsByNameOrBrand: query: $query")
        require(query.isNotBlank() && query.length < 50) { "the term should have between 1 and 50 characters" }
        return productDao.searchByNameOrBrand(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        logger.d(TAG, "insertProduct: ${product.name} (hasImage: ${imageBytes != null})")
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= 100) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
        validateProduct(product)
        db.useWriterConnection { conn ->
            conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                productDao.insert(product.toEntity())
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

    override suspend fun updateProduct(
        product: CatalogProduct,
        imageBytes: ByteArray?
    ) {
        logger.d(TAG, "updateProduct: ${product.name} (hasImage: ${imageBytes != null})")
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= 100) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
        validateProduct(product)
        db.useWriterConnection { conn ->
            conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                productDao.update(product.toEntity())
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

    override suspend fun deleteProductById(id: String) {
        logger.d(TAG, "deleteProduct id:  $id")
        productDao.deleteProductById(id)
    }

    override fun exists(id: String): Flow<Boolean> {
        logger.d(TAG, "exists id:  $id")
        return productDao.exists(id)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateProduct(product: CatalogProduct) {
        require(product.name.isNotBlank()) { "The name of the product shouldn`t be empty" }
        require(product.name.length <= 100) { "The name of the product should have at least 100 characters" }

        product.brand?.let {
            require(it.length <= 50) { "The brand should have at least 50 characters" }
        }

        product.ean?.let {
            require(it.length in listOf(8, 13, 14)) {
                "invalid EAN size: should have 8, 13 or 14 digits"
            }

            require(it.all { char -> char.isDigit() }) {
                "invalid EAN: should have only numbers"
            }
        }

        require(runCatching { Uuid.parse(product.id) }.isSuccess) {
            "Invalid UUID"
        }

        require(isValidTimestamp(product.updatedAt)) { "Invalid epoch time millis" }

        require(product.netWeight > 0) { "Product netweight should be more than zero" }
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
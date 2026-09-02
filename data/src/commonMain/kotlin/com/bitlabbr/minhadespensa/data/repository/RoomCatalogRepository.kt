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
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    private val defaultCategories = listOf(
        "Grãos",
        "Proteínas",
        "Bebidas",
        "Limpeza",
        "Lanches",
        "Hortifrúti",
        "Outros",
    )

    override fun getProductByEan(ean: String): Flow<CatalogProduct?> {
        return productDao.findByEan(ean)
            .map { entity ->
                val product = entity?.toDomain()
                logger.d(TAG, "getProductByEan:$ean result=$product")
                product
            }
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
        require(query.isNotBlank() && query.length <= 50) { "the term should have between 1 and 50 characters" }
        return productDao.searchByNameOrBrand(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        logger.d(TAG, "insertProduct: ${product.name} (hasImage: ${imageBytes != null})")
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= CoreConstants.Media.MAX_IMAGE_SIZE_KB) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
        validateProduct(product)
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

    override suspend fun forceUpdateForProduct(
        product: CatalogProduct,
        imageBytes: ByteArray?
    ) {
        logger.d(TAG, "forceUpdateForProduct: ${product.name} (hasImage: ${imageBytes != null})")
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= CoreConstants.Media.MAX_IMAGE_SIZE_KB) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
        validateProduct(product)
        db.useWriterConnection { conn ->
            conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                productDao.forceUpdateForProduct(product.toEntity())
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

    override fun getProductImage(productId: String): Flow<ByteArray?> {
        return mediaDao.getByProductIdFlow(productId).map { it?.blob }
    }

    override suspend fun updateForProductIfNewer(
        product: CatalogProduct,
        imageBytes: ByteArray?
    ) {
        logger.d(TAG, "updateForProductIfNewer: ${product.name} (hasImage: ${imageBytes != null})")
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= CoreConstants.Media.MAX_IMAGE_SIZE_KB) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
        validateProduct(product)
        db.useWriterConnection { conn ->
            conn.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val rowsAffected = productDao.updateProductIfNewer(
                    id = product.id,
                    name = product.name,
                    category = product.category,
                    ean = product.ean,
                    brand = product.brand,
                    measureUnit = product.measureUnit,
                    thumbnailUrl = product.thumbnailUrl,
                    netWeight = product.netWeight,
                    updatedAt = product.updatedAt,
                    isDeleted = product.isDeleted,
                    manuallyAdded = product.manuallyAdded
                )
                if (rowsAffected > 0 && imageBytes != null) {
                    mediaDao.insertOrUpdate(
                        ProductMediaEntity(
                            productId = product.id,
                            blob = imageBytes,
                            updatedAt = product.updatedAt
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

    override fun getCategories(): Flow<List<String>> {
        // Por enquanto retorna a lista padrão via Flow.
        // Futuro: productDao.getDistinctCategories().map { it.ifEmpty { defaultCategories } }
        return flowOf(defaultCategories)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateProduct(product: CatalogProduct) {
        require(product.name.isNotBlank()) { "The name of the product shouldn`t be empty" }
        require(product.name.length <= CoreConstants.Product.NAME_MAX_LENGTH)
        { "The name of the product should have at most ${CoreConstants.Product.NAME_MAX_LENGTH} characters" }

        product.brand?.let {
            require(it.length <= CoreConstants.Product.BRAND_MAX_LENGTH)
            { "The brand should have at most ${CoreConstants.Product.BRAND_MAX_LENGTH} characters" }
        }

        product.notes?.let {
            require(it.length <= CoreConstants.Product.NOTES_MAX_LENGTH)
            { "The notes should have at most ${CoreConstants.Product.NOTES_MAX_LENGTH} characters" }
        }

        product.ean?.let {
            require(it.length in CoreConstants.Product.EAN_VALID_LENGTHS) {
                "Invalid EAN length: ${it.length}. Expected one of ${CoreConstants.Product.EAN_VALID_LENGTHS}"
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

        require(product.category.isNotBlank()) { "The category shouldn't be empty" }
        require(product.category.length <= CoreConstants.Product.CATEGORY_MAX_LENGTH)
        { "The category should have at most ${CoreConstants.Product.CATEGORY_MAX_LENGTH} characters" }
    }
}

fun CatalogProductEntity.toDomain() = CatalogProduct(
    id = this.id,
    ean = this.ean,
    name = this.name,
    category = this.category,
    brand = this.brand,
    measureUnit = MeasureUnit.valueOf(this.measureUnit),
    netWeight = this.netWeight,
    thumbnailUrl = this.thumbnailUrl,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    manuallyAdded = this.manuallyAdded,
    notes = this.notes
)

fun CatalogProduct.toEntity() = CatalogProductEntity(
    id = this.id,
    ean = this.ean,
    name = this.name,
    category = this.category,
    brand = this.brand,
    measureUnit = this.measureUnit.name,
    netWeight = this.netWeight,
    thumbnailUrl = this.thumbnailUrl,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    manuallyAdded = this.manuallyAdded,
    notes = this.notes
)
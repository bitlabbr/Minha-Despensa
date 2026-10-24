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
import com.bitlabbr.minhadespensa.core.domain.model.ProductCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.isValidTimestamp
import com.bitlabbr.minhadespensa.core.domain.util.matchesSearchQuery
import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.entity.ProductCategoryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
import com.bitlabbr.minhadespensa.data.local.mapper.toDomain
import com.bitlabbr.minhadespensa.data.local.mapper.toEntity
import com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import okio.Buffer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RoomCatalogRepository(
    private val db: AppDatabase,
    private val logger: AppLogger,
    private val outboxEnqueuer: OutboxEnqueuer = OutboxEnqueuer(db.pendingSyncDao()),
) : CatalogRepository {
    private val productDao = db.catalogDao()
    private val mediaDao = db.productMediaDao()
    private val categoryDao = db.categoryDao()

    private val defaultCategories = CoreConstants.CatalogCategories.DEFAULT_CATEGORIES

    private suspend fun ensureDefaultCategoriesSeeded() {
        if (categoryDao.countAll() == 0) {
            val seedList = defaultCategories.mapIndexed { index, name ->
                ProductCategoryEntity(
                    id = "default_category_$index",
                    name = name,
                    isDefault = true,
                    updatedAt = 1000L,
                    isDeleted = false,
                )
            }
            categoryDao.insertAllIgnore(seedList)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun ensureCategoryRegistered(categoryName: String, updatedAt: Long) {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return
        ensureDefaultCategoriesSeeded()
        val normalizedTarget = trimmed.normalizeForSearch()
        val allCategories = categoryDao.getAllSnapshot()
        if (allCategories.any { !it.isDeleted && it.name.normalizeForSearch() == normalizedTarget }) {
            return
        }
        val softDeletedMatch = allCategories.firstOrNull {
            it.isDeleted && it.name.normalizeForSearch() == normalizedTarget
        }
        if (softDeletedMatch != null) {
            val updated = softDeletedMatch.copy(
                name = trimmed,
                isDeleted = false,
                updatedAt = maxOf(updatedAt, softDeletedMatch.updatedAt),
            )
            categoryDao.insertOrUpdate(updated)
            outboxEnqueuer.enqueueCategory(updated)
        } else {
            val created = ProductCategoryEntity(
                id = Uuid.random().toString(),
                name = trimmed,
                isDefault = false,
                updatedAt = updatedAt,
                isDeleted = false,
            )
            categoryDao.insertOrUpdate(created)
            outboxEnqueuer.enqueueCategory(created)
        }
    }

    override fun getProductByEan(ean: String): Flow<CatalogProduct?> {
        return productDao.findByEan(ean)
            .map { entity ->
                logger.d(TAG, "getProductByEan:$ean result=$entity")
                entity?.toDomain()
            }
    }

    override fun getProductById(id: String): Flow<CatalogProduct?> {
        logger.d(TAG, "searchProducts: id: $id")
        return productDao.findById(id).map { it?.toDomain() }
    }

    override fun getAllActiveProducts(): Flow<List<CatalogProduct>> {
        logger.d(TAG, "getAllActives")
        return productDao.getAllActive().map { it.map { it.toDomain() } }
    }

    override fun searchProductsByNameOrBrand(query: String): Flow<List<CatalogProduct>> {
        logger.d(TAG, "searchProductsByNameOrBrand: query: $query")
        require(query.isNotBlank() && query.length <= 50) { "the term should have between 1 and 50 characters" }
        val trimmed = query.trim()
        return productDao.getAllActive().map { entities ->
            entities
                .map { it.toDomain() }
                .filter { product ->
                    product.name.matchesSearchQuery(trimmed) ||
                        (product.brand?.matchesSearchQuery(trimmed) == true) ||
                        "${product.name} ${product.brand.orEmpty()}".matchesSearchQuery(trimmed)
                }
        }
    }

    override suspend fun insertProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        logger.d(TAG, "insertProduct: ${product.name} (hasImage: ${imageBytes != null})")
        validateImage(imageBytes)
        validateProduct(product)
        ensureCategoryRegistered(product.category, product.updatedAt)
        val computedHash = imageBytes?.let { Buffer().write(it).readByteString().sha256().hex() } ?: product.imageHash
        val entity = product.toEntity().copy(imageHash = computedHash)
        productDao.insert(entity)
        outboxEnqueuer.enqueueProduct(entity)
        if (imageBytes != null) {
            mediaDao.insertOrUpdate(
                ProductMediaEntity(
                    productId = product.id,
                    blob = imageBytes,
                    updatedAt = product.updatedAt
                )
            )
        }
    }

    override suspend fun forceUpdateProduct(
        product: CatalogProduct,
        imageBytes: ByteArray?,
        removeImage: Boolean,
    ) {
        logger.d(TAG, "forceUpdateForProduct: ${product.name} (hasImage: ${imageBytes != null}, removeImage: $removeImage)")
        validateImage(imageBytes)
        validateProduct(product)
        ensureCategoryRegistered(product.category, product.updatedAt)
        val computedHash = if (removeImage) null else imageBytes?.let { Buffer().write(it).readByteString().sha256().hex() } ?: product.imageHash
        val entity = product.toEntity().copy(imageHash = computedHash)
        productDao.forceUpdateForProduct(entity)
        outboxEnqueuer.enqueueProduct(entity)
        if (removeImage) {
            mediaDao.deleteByProductId(product.id)
        } else if (imageBytes != null) {
            mediaDao.insertOrUpdate(
                ProductMediaEntity(
                    productId = product.id,
                    blob = imageBytes,
                    updatedAt = product.updatedAt
                )
            )
        }
    }

    override fun getProductImage(productId: String): Flow<ByteArray?> {
        return mediaDao.getByProductIdFlow(productId).map { it?.blob }
    }

    override suspend fun updateProductIfNewer(
        product: CatalogProduct,
        imageBytes: ByteArray?,
        removeImage: Boolean,
    ) {
        logger.d(TAG, "updateForProductIfNewer: ${product.name} (hasImage: ${imageBytes != null}, removeImage: $removeImage)")
        validateImage(imageBytes)
        validateProduct(product)
        val computedHash = if (removeImage) null else imageBytes?.let { Buffer().write(it).readByteString().sha256().hex() } ?: product.imageHash
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
            manuallyAdded = product.manuallyAdded,
            notes = product.notes,
            imageHash = computedHash,
        )
        if (rowsAffected > 0) {
            val entity = product.toEntity().copy(imageHash = computedHash)
            outboxEnqueuer.enqueueProduct(entity)
            if (!product.isDeleted) {
                ensureCategoryRegistered(product.category, product.updatedAt)
            }
            if (removeImage) {
                mediaDao.deleteByProductId(product.id)
            } else if (imageBytes != null) {
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

    override suspend fun markProductAsDeleted(id: String, updatedAt: Long) {
        logger.d(TAG, "markProductAsDeleted id: $id, updatedAt: $updatedAt")
        productDao.markAsDeleted(id, updatedAt)
        productDao.findSnapshotById(id)?.let { outboxEnqueuer.enqueueProduct(it.copy(isDeleted = true, updatedAt = updatedAt)) }
    }

    override suspend fun deleteProductById(id: String) {
        logger.d(TAG, "deleteProduct id:  $id")
        productDao.findSnapshotById(id)?.let { outboxEnqueuer.enqueueProduct(it.copy(isDeleted = true)) }
        productDao.deleteProductById(id)
    }

    override fun existsById(id: String): Flow<Boolean> {
        logger.d(TAG, "exists id:  $id")
        return productDao.exists(id)
    }

    override fun getAllActiveCategories(): Flow<List<ProductCategory>> {
        return categoryDao.getAllActive()
            .onStart { ensureDefaultCategoriesSeeded() }
            .map { entities ->
                entities
                    .map { it.toDomain() }
                    .distinctBy { it.name.normalizeForSearch() }
                    .sortedBy { it.name.normalizeForSearch() }
            }
    }

    override fun getCategories(): Flow<List<String>> {
        return combine(
            categoryDao.getAllActive().onStart { ensureDefaultCategoriesSeeded() },
            productDao.getAllActive(),
        ) { categoryEntities, productEntities ->
            val persistedNames = categoryEntities.map { it.name.trim() }
            val productCategories = productEntities.map { it.category.trim() }
            (persistedNames + productCategories + listOf(CoreConstants.Product.DEFAULT_CATEGORY))
                .filter { it.isNotBlank() }
                .distinctBy { it.normalizeForSearch() }
                .sortedBy { it.normalizeForSearch() }
        }
    }

    override suspend fun saveCategory(category: ProductCategory) {
        logger.d(TAG, "saveCategory: ${category.name}")
        val trimmedName = category.name.trim()
        validateCategory(category.id, trimmedName, category.updatedAt)
        ensureDefaultCategoriesSeeded()

        val normalizedTarget = trimmedName.normalizeForSearch()
        val allExisting = categoryDao.getAllSnapshot()
        val activeMatch = allExisting.firstOrNull {
            !it.isDeleted && it.name.normalizeForSearch() == normalizedTarget
        }
        if (activeMatch != null) {
            val updated = activeMatch.copy(
                name = trimmedName,
                updatedAt = maxOf(category.updatedAt, activeMatch.updatedAt),
            )
            categoryDao.insertOrUpdate(updated)
            outboxEnqueuer.enqueueCategory(updated)
            return
        }
        val matchingDeleted = allExisting.firstOrNull {
            it.isDeleted && it.name.normalizeForSearch() == normalizedTarget
        }
        if (matchingDeleted != null) {
            val updated = matchingDeleted.copy(
                name = trimmedName,
                isDeleted = false,
                updatedAt = maxOf(category.updatedAt, matchingDeleted.updatedAt),
            )
            categoryDao.insertOrUpdate(updated)
            outboxEnqueuer.enqueueCategory(updated)
        } else {
            val entity = category.copy(name = trimmedName, isDeleted = false).toEntity()
            categoryDao.insertOrUpdate(entity)
            outboxEnqueuer.enqueueCategory(entity)
        }
    }

    override suspend fun renameCategory(
        id: String,
        oldName: String,
        newName: String,
        updatedAt: Long,
    ) {
        logger.d(TAG, "renameCategory: id=$id, oldName=$oldName, newName=$newName")
        val trimmedNew = newName.trim()
        val trimmedOld = oldName.trim()
        validateCategory(id, trimmedNew, updatedAt)
        require(trimmedOld.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
        require(trimmedOld.normalizeForSearch() != CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()) {
            CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE
        }

        ensureDefaultCategoriesSeeded()
        val normalizedOld = trimmedOld.normalizeForSearch()

        db.useWriterConnection { transactor ->
            transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val matchingCategories = categoryDao.getAllSnapshot().filter {
                    it.id == id || (!it.isDeleted && it.name.normalizeForSearch() == normalizedOld)
                }

                if (matchingCategories.isNotEmpty()) {
                    matchingCategories.forEach { cat ->
                        if (cat.updatedAt <= updatedAt) {
                            val updated = cat.copy(
                                name = trimmedNew,
                                isDeleted = false,
                                updatedAt = updatedAt,
                            )
                            categoryDao.insertOrUpdate(updated)
                            outboxEnqueuer.enqueueCategory(updated)
                        }
                    }
                } else {
                    val created = ProductCategoryEntity(
                        id = id,
                        name = trimmedNew,
                        isDefault = false,
                        updatedAt = updatedAt,
                        isDeleted = false,
                    )
                    categoryDao.insertOrUpdate(created)
                    outboxEnqueuer.enqueueCategory(created)
                }

                val activeProducts = productDao.getAllActiveSnapshot()
                activeProducts
                    .filter { it.category.normalizeForSearch() == normalizedOld }
                    .forEach { product ->
                        val effectiveTimestamp = maxOf(updatedAt, product.updatedAt + 1)
                        productDao.updateCategoryById(
                            id = product.id,
                            newCategory = trimmedNew,
                            updatedAt = effectiveTimestamp,
                        )
                        outboxEnqueuer.enqueueProduct(
                            product.copy(
                                category = trimmedNew,
                                updatedAt = effectiveTimestamp,
                            ),
                        )
                    }
            }
        }
        db.invalidationTracker.refreshAsync()
    }

    override suspend fun deleteCategory(
        id: String,
        categoryName: String,
        fallbackCategory: String,
        updatedAt: Long,
    ) {
        logger.d(TAG, "deleteCategory: id=$id, name=$categoryName, fallback=$fallbackCategory")
        val trimmedName = categoryName.trim()
        val trimmedFallback = fallbackCategory.trim().ifBlank { CoreConstants.Product.DEFAULT_CATEGORY }
        require(id.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_ID_REQUIRED }
        require(trimmedName.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
        require(isValidTimestamp(updatedAt)) { "Invalid epoch time millis" }
        require(trimmedName.normalizeForSearch() != CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()) {
            CoreConstants.Validation.ERROR_CATEGORY_DEFAULT_IMMUTABLE
        }

        ensureDefaultCategoriesSeeded()
        val normalizedTarget = trimmedName.normalizeForSearch()

        db.useWriterConnection { transactor ->
            transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val matchingCategories = categoryDao.getAllSnapshot().filter {
                    it.id == id || (!it.isDeleted && it.name.normalizeForSearch() == normalizedTarget)
                }
                matchingCategories.forEach { cat ->
                    val effectiveTs = maxOf(updatedAt, cat.updatedAt)
                    categoryDao.markAsDeleted(cat.id, effectiveTs)
                    outboxEnqueuer.enqueueCategory(cat.copy(isDeleted = true, updatedAt = effectiveTs))
                }

                val activeProducts = productDao.getAllActiveSnapshot()
                activeProducts
                    .filter { it.category.normalizeForSearch() == normalizedTarget }
                    .forEach { product ->
                        val effectiveTimestamp = maxOf(updatedAt, product.updatedAt + 1)
                        productDao.updateCategoryById(
                            id = product.id,
                            newCategory = trimmedFallback,
                            updatedAt = effectiveTimestamp,
                        )
                        outboxEnqueuer.enqueueProduct(
                            product.copy(
                                category = trimmedFallback,
                                updatedAt = effectiveTimestamp,
                            ),
                        )
                    }
            }
        }
        db.invalidationTracker.refreshAsync()
    }

    private fun validateCategory(id: String, name: String, updatedAt: Long) {
        require(id.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_ID_REQUIRED }
        require(name.isNotBlank()) { CoreConstants.Validation.ERROR_CATEGORY_NAME_BLANK }
        require(name.length <= CoreConstants.Product.CATEGORY_MAX_LENGTH) {
            "${CoreConstants.Validation.ERROR_PRODUCT_CATEGORY_TOO_LONG}: ${CoreConstants.Product.CATEGORY_MAX_LENGTH}"
        }
        require(isValidTimestamp(updatedAt)) { "Invalid epoch time millis" }
    }

    private fun validateImage(imageBytes: ByteArray?) {
        if (imageBytes != null) {
            val imageSizeKb = imageBytes.size / 1024
            require(imageSizeKb <= CoreConstants.Media.MAX_IMAGE_SIZE_KB) {
                "File is too large (size: ${imageSizeKb}KB)"
            }
        }
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

    private companion object {
        const val TAG = "RoomCatalogRepository"
    }
}
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

import com.bitlabbr.minhadespensa.core.domain.model.Product
import com.bitlabbr.minhadespensa.core.domain.repository.ProductRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.data.local.dao.ProductDao
import com.bitlabbr.minhadespensa.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProductRepository(
    private val dao: ProductDao,
    private val logger: AppLogger
): ProductRepository {
    private val TAG = "RoomProductRepository"

    override fun getAllProducts(): Flow<List<Product>> {
        logger.d(TAG, "getAllProducts")
        return dao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertProduct(product: Product) {
        logger.d(TAG, "insert product ${product.name}")
        dao.insert(product.toEntity())
    }

    override suspend fun deleteProductById(id: String) {
        logger.d(TAG, "delete product $id")
        dao.markAsDeleted(id)
    }

    override suspend fun getProductById(id: String): Product? {
        logger.d(TAG, "getProductById $id")
        return dao.findById(id)?.toDomain()
    }

    private fun ProductEntity.toDomain(): Product {
        return Product(
            id = this.id,
            name = this.name,
            amount = this.amount,
            measureUnit = this.measureUnit,
            expirationDate = this.expirationDate,
            updatedAt = this.updatedAt,
            isDeleted = this.isDeleted
        )
    }

    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = this.id,
            name = this.name,
            amount = this.amount,
            measureUnit = this.measureUnit,
            expirationDate = this.expirationDate,
            updatedAt = this.updatedAt,
            isDeleted = this.isDeleted
        )
    }
}

/*
 * Copyright (c) 2026 Willian Santos
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0
 * International License (CC BY-NC 4.0).
 *
 * You may use, copy, modify, and distribute this file for non-commercial
 * purposes only, provided that proper attribution is given.
 *
 * The copyright holder retains all commercial rights and may
 * license this work under different terms.
 *
 * License: https://creativecommons.org/licenses/by-nc/4.0/
 *
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

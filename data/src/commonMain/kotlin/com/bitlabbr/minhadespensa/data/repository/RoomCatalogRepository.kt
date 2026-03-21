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

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.dao.CatalogProductDao
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class RoomCatalogRepository(
    private val dao: CatalogProductDao,
    private val logger: AppLogger
) : CatalogRepository {
    private val TAG = "RoomCatalogRepository"

    override fun getProductByEan(ean: String): Flow<CatalogProduct?> {
        logger.d(TAG, "searchProducts: ean: $ean")
        return dao.findByEan(ean).map { it?.toDomain() }
    }

    override fun getProductById(id: String): Flow<CatalogProduct?> {
        logger.d(TAG, "searchProducts: id: $id")
        return dao.findById(id).map { it?.toDomain() }
    }

    override fun searchProducts(query: String): Flow<List<CatalogProduct>> {
        logger.d(TAG, "searchProducts: query: $query")
        return dao.searchByNameOrBrand(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveProduct(product: CatalogProduct) {
        logger.d(TAG, "saveProduct:  ${product.name}")
        dao.insertOrUpdate(product.toEntity())
    }

    override suspend fun deleteProduct(id: String) {
        logger.d(TAG, "deleteProduct id:  $id")
        dao.markAsDeleted(id, getCurrentTime())
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
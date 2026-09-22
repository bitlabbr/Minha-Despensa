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

package com.bitlabbr.minhadespensa.core.domain.fakes

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCatalogRepository : CatalogRepository {
    val products = MutableStateFlow<Map<String, CatalogProduct>>(emptyMap())

    override fun getProductById(id: String): Flow<CatalogProduct?> = products.map { it[id] }
    override fun getProductByEan(ean: String): Flow<CatalogProduct?> =
        products.map { map -> map.values.firstOrNull { it.ean == ean } }

    override fun getAllActiveProducts(): Flow<List<CatalogProduct>> =
        products.map { map -> map.values.filter { !it.isDeleted } }

    override fun searchProductsByNameOrBrand(query: String): Flow<List<CatalogProduct>> =
        products.map { map ->
            map.values.filter {
                !it.isDeleted && (it.name.contains(query, true) || it.brand?.contains(query, true) == true)
            }
        }

    override fun existsById(id: String): Flow<Boolean> = products.map { it.containsKey(id) }
    override fun getCategories(): Flow<List<String>> =
        products.map { it.values.map { p -> p.category }.distinct() }

    override fun getProductImage(productId: String): Flow<ByteArray?> = MutableStateFlow(null)

    override suspend fun insertProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        products.value += (product.id to product)
    }

    override suspend fun forceUpdateProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        products.value += (product.id to product)
    }

    override suspend fun updateProductIfNewer(product: CatalogProduct, imageBytes: ByteArray?) {
        products.value += (product.id to product)
    }

    override suspend fun markProductAsDeleted(id: String, updatedAt: Long) {
        products.value[id]?.let {
            products.value += (id to it.copy(isDeleted = true, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteProductById(id: String) {
        products.value -= id
    }
}
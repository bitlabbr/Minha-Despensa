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

package com.bitlabbr.minhadespensa.uisystem.fakes

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.ProductCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.matchesSearchQuery
import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeCatalogRepository : CatalogRepository {
    val products = MutableStateFlow<Map<String, CatalogProduct>>(emptyMap())
    val productImages = MutableStateFlow<Map<String, ByteArray?>>(emptyMap())
    val categories = MutableStateFlow<Map<String, ProductCategory>>(
        CoreConstants.CatalogCategories.DEFAULT_CATEGORIES.mapIndexed { index, name ->
            val id = "default-${index + 1}"
            id to ProductCategory(
                id = id,
                name = name,
                isDefault = true,
                updatedAt = 0L,
                isDeleted = false,
            )
        }.toMap()
    )

    override fun getProductById(id: String): Flow<CatalogProduct?> = products.map { it[id] }

    override fun getProductByEan(ean: String): Flow<CatalogProduct?> =
        products.map { map -> map.values.firstOrNull { it.ean == ean } }

    override fun getAllActiveProducts(): Flow<List<CatalogProduct>> =
        products.map { map -> map.values.filter { !it.isDeleted } }

    override fun searchProductsByNameOrBrand(query: String): Flow<List<CatalogProduct>> =
        products.map { map ->
            map.values.filter {
                !it.isDeleted && (
                    it.name.matchesSearchQuery(query) ||
                        it.brand?.matchesSearchQuery(query) == true ||
                        "${it.name} ${it.brand.orEmpty()}".matchesSearchQuery(query)
                )
            }
        }

    override fun existsById(id: String): Flow<Boolean> = products.map { it.containsKey(id) }

    override fun getAllActiveCategories(): Flow<List<ProductCategory>> =
        categories.map { map ->
            map.values.filter { !it.isDeleted }.sortedBy { it.name.lowercase() }
        }

    override fun getCategories(): Flow<List<String>> =
        combine(categories, products) { catMap, prodMap ->
            val activeCategoryNames = catMap.values.filter { !it.isDeleted }.map { it.name.trim() }
            val activeProductCategories = prodMap.values.filter { !it.isDeleted }.map { it.category.trim() }
            (activeCategoryNames + activeProductCategories)
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }

    override suspend fun saveCategory(category: ProductCategory) {
        categories.value += (category.id to category)
    }

    override suspend fun renameCategory(id: String, oldName: String, newName: String, updatedAt: Long) {
        val current = categories.value[id]
            ?: throw IllegalArgumentException("Categoria não encontrada")
        val trimmed = newName.trim()
        val oldNormalized = oldName.normalizeForSearch()
        categories.value += (id to current.copy(name = trimmed, updatedAt = updatedAt, isDeleted = false))
        products.value = products.value.mapValues { (_, prod) ->
            if (!prod.isDeleted && prod.category.normalizeForSearch() == oldNormalized) {
                prod.copy(category = trimmed, updatedAt = updatedAt)
            } else {
                prod
            }
        }
    }

    override suspend fun deleteCategory(
        id: String,
        categoryName: String,
        fallbackCategory: String,
        updatedAt: Long,
    ) {
        val current = categories.value[id]
            ?: throw IllegalArgumentException("Categoria não encontrada")
        if (categoryName.normalizeForSearch() == CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()) {
            throw IllegalArgumentException("A categoria 'Outros' não pode ser excluída")
        }
        val oldNormalized = categoryName.normalizeForSearch()
        categories.value += (id to current.copy(isDeleted = true, updatedAt = updatedAt))
        products.value = products.value.mapValues { (_, prod) ->
            if (!prod.isDeleted && prod.category.normalizeForSearch() == oldNormalized) {
                prod.copy(category = fallbackCategory, updatedAt = updatedAt)
            } else {
                prod
            }
        }
    }

    override fun getProductImage(productId: String): Flow<ByteArray?> =
        productImages.map { it[productId] }

    override suspend fun insertProduct(product: CatalogProduct, imageBytes: ByteArray?) {
        products.value += (product.id to product)
        if (imageBytes != null) {
            productImages.value += (product.id to imageBytes)
        }
    }

    override suspend fun forceUpdateProduct(product: CatalogProduct, imageBytes: ByteArray?, removeImage: Boolean) {
        products.value += (product.id to product)
        if (removeImage) {
            productImages.value -= product.id
        } else if (imageBytes != null) {
            productImages.value += (product.id to imageBytes)
        }
    }

    override suspend fun updateProductIfNewer(product: CatalogProduct, imageBytes: ByteArray?, removeImage: Boolean) {
        products.value += (product.id to product)
        if (removeImage) {
            productImages.value -= product.id
        } else if (imageBytes != null) {
            productImages.value += (product.id to imageBytes)
        }
    }

    override suspend fun markProductAsDeleted(id: String, updatedAt: Long) {
        products.value[id]?.let {
            products.value += (id to it.copy(isDeleted = true, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteProductById(id: String) {
        products.value -= id
        productImages.value -= id
    }
}

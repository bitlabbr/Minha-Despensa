package com.bill.minhadispensa.core.domain.repository

import com.bill.minhadispensa.core.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun insertProduct(product: Product)
    suspend fun deleteProductById(id: String)
    suspend fun getProductById(id: String): Product?
}

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
 */


package com.bill.minhadispensa.minhadispensa.data.repository

import com.bill.minhadispensa.core.domain.model.MeasureUnit
import com.bill.minhadispensa.core.domain.model.Product
import com.bill.minhadispensa.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class FakeProductRepository : ProductRepository {

    private val bulkInsert = mutableListOf(
        Product(
            id = "1",
            name = "Arroz Branco",
            amount = 2.0,
            unitMeasure = MeasureUnit.KILOGRAM,
            expirationDate = Clock.System.now(),
            imgUrl = null
        ),
        Product(
            id = "2",
            name = "Leite Integral",
            amount = 4.0,
            unitMeasure = MeasureUnit.LITER,
            expirationDate = null,
            imgUrl = null
        ),
        Product(
            id = "3",
            name = "Macarrão",
            amount = 3.0,
            unitMeasure = MeasureUnit.PACKAGE,
            expirationDate = Clock.System.now(),
            imgUrl = null
        )
    )

    override fun getAllProducts(): Flow<List<Product>> = flow {
        emit(bulkInsert)
    }

    override suspend fun insertProduct(product: Product) {
        bulkInsert.add(product)
        println("Adicionando: ${product.name}")
    }

    override suspend fun deleteProductById(id: String) {
        bulkInsert.remove(bulkInsert.find { it.id == id })
        println("Removendo ID: $id")
    }

    override suspend fun getProductById(id: String): Product? {
        return bulkInsert.find { it.id == id }
    }
}
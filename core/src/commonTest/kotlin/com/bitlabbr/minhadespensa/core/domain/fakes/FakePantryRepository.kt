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

import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemConsumption
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePantryRepository : PantryRepository {
    val items = MutableStateFlow<Map<String, PantryItem>>(emptyMap())

    override fun getPantryItemById(pantryItemId: String): Flow<PantryItem?> = items.map { it[pantryItemId] }
    override fun getPantryItemsByProductId(productId: String): Flow<List<PantryItem>> =
        items.map { map -> map.values.filter { it.productId == productId && !it.isDeleted } }

    override fun getAllActivePantryItems(): Flow<List<PantryItem>> =
        items.map { map -> map.values.filter { !it.isDeleted } }

    override fun getAllActivePantryItemsWithCategory(): Flow<List<PantryItemWithCategory>> =
        MutableStateFlow(emptyList())

    override fun getPantryItemWithCategoryById(pantryItemId: String): Flow<PantryItemWithCategory?> =
        MutableStateFlow(null)

    override fun getExpiringPantryItems(thresholdDays: Int): Flow<List<PantryItemWithCategory>> =
        MutableStateFlow(emptyList())

    override suspend fun insertPantryItem(item: PantryItem) {
        items.value += (item.id to item)
    }

    override suspend fun forceUpdatePantryItem(item: PantryItem) {
        items.value += (item.id to item)
    }

    override suspend fun updatePantryItemIfNewer(item: PantryItem) {
        items.value += (item.id to item)
    }

    override suspend fun markPantryItemAsDeleted(id: String, updatedAt: Long) {
        items.value[id]?.let { items.value += (id to it.copy(isDeleted = true, updatedAt = updatedAt)) }
    }

    override suspend fun deletePantryItemById(id: String) {
        items.value -= id
    }

    override suspend fun consumePantryItem(pantryItemId: String, quantityToConsume: Double) {}
    override suspend fun consumeBatch(consumptions: List<PantryItemConsumption>) {}
}
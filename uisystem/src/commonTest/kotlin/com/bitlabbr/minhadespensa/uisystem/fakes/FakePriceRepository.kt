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

import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePriceRepository : PriceRepository {

    val priceEntries = MutableStateFlow<Map<String, PriceEntry>>(emptyMap())

    override fun getPriceHistoryByProductId(productId: String): Flow<List<PriceEntry>> =
        priceEntries.map { map ->
            map.values.filter { it.productId == productId && !it.isDeleted }
                .sortedByDescending { it.updatedAt }
        }

    override fun getLatestPriceForProductId(productId: String): Flow<PriceEntry?> =
        priceEntries.map { map ->
            map.values.filter { it.productId == productId && !it.isDeleted }
                .maxByOrNull { it.updatedAt }
        }

    override suspend fun insertPriceEntry(priceEntry: PriceEntry) {
        priceEntries.value += (priceEntry.id to priceEntry)
    }

    override suspend fun forceUpdatePriceEntry(priceEntry: PriceEntry) {
        priceEntries.value += (priceEntry.id to priceEntry)
    }

    override suspend fun updatePriceEntryIfNewer(priceEntry: PriceEntry) {
        priceEntries.value += (priceEntry.id to priceEntry)
    }

    override suspend fun markPriceEntryAsDeleted(priceEntryId: String, updatedAt: Long) {
        val existing = priceEntries.value[priceEntryId]
        if (existing != null) {
            priceEntries.value += (priceEntryId to existing.copy(isDeleted = true, updatedAt = updatedAt))
        }
    }

    override suspend fun deletePriceEntryById(priceEntryId: String) {
        priceEntries.value -= priceEntryId
    }
}

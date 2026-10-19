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

package com.bitlabbr.minhadespensa.core.domain.model.invoice

import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class Invoice(
    val accessKey: String? = null,
    val storeName: String,
    val emissionDate: String? = null,
    val items: List<InvoiceItem> = emptyList(),
) {
    val totalInvoiceInCents: Long
        get() = items.sumOf { it.computedTotalPriceInCents ?: 0L }

    val totalSelectedItems: Int
        get() = items.count { it.isSelected }

    val totalSelectedInCents: Long
        get() = items.filter { it.isSelected }.sumOf { it.computedTotalPriceInCents ?: 0L }

    fun consolidateDuplicateItems(): Invoice = copy(
        items = items.consolidateDuplicates()
    )
}

fun List<InvoiceItem>.consolidateDuplicates(): List<InvoiceItem> {
    if (size <= 1) return this

    val groups = mutableListOf<MutableList<InvoiceItem>>()

    for (item in this) {
        val itemName = item.name.normalizeForSearch().replace(Regex("\\s+"), " ")
        val itemEan = item.ean?.trim()?.takeIf { it.isNotBlank() }

        val matchingGroup = groups.firstOrNull { group ->
            val representative = group.first()
            val repName = representative.name.normalizeForSearch().replace(Regex("\\s+"), " ")
            val repEan = group.firstNotNullOfOrNull { it.ean?.trim()?.takeIf { e -> e.isNotBlank() } }

            representative.measureUnit == item.measureUnit && (
                (itemEan != null && repEan != null && itemEan == repEan) ||
                    (repName.isNotBlank() && repName == itemName && (itemEan == null || repEan == null || itemEan == repEan))
                )
        }

        if (matchingGroup != null) {
            matchingGroup.add(item)
        } else {
            groups.add(mutableListOf(item))
        }
    }

    return groups.map { group ->
        if (group.size == 1) {
            group.first()
        } else {
            val first = group.first()
            val totalQuantity = (group.sumOf { it.quantity } * 10000.0).roundToLong() / 10000.0
            val totalPriceInCents = group.sumOf { it.computedTotalPriceInCents ?: 0L }
            val resolvedEan = group.firstNotNullOfOrNull { it.ean?.trim()?.takeIf { e -> e.isNotBlank() } }
            val resolvedUnitPrice = first.unitPriceInCents
                ?: group.firstNotNullOfOrNull { it.unitPriceInCents }

            first.copy(
                ean = resolvedEan,
                quantity = totalQuantity,
                unitPriceInCents = resolvedUnitPrice,
                totalPriceInCents = totalPriceInCents,
                isSelected = group.any { it.isSelected },
            )
        }
    }
}

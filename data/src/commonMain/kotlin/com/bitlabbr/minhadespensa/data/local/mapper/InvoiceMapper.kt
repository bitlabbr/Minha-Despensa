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

package com.bitlabbr.minhadespensa.data.local.mapper

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceItemDto
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceResponseDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun InvoiceResponseDto.toDomain(): Invoice = Invoice(
    accessKey = this.accessKey?.trim()?.takeIf { it.isNotBlank() },
    storeName = this.storeName.trim(),
    emissionDate = this.emissionDate?.trim()?.takeIf { it.isNotBlank() },
    items = this.items.mapIndexed { index, itemDto ->
        itemDto.toDomain()
    },
)

@OptIn(ExperimentalUuidApi::class)
fun InvoiceItemDto.toDomain(): InvoiceItem = InvoiceItem(
    id = Uuid.random().toString(),
    ean = this.ean?.trim()?.takeIf { it.isNotBlank() },
    name = this.name.trim(),
    quantity = this.quantity,
    measureUnit = parseMeasureUnit(this.measureUnit ?: this.unit),
    unitPriceInCents = this.unitPriceInCents,
    totalPriceInCents = this.totalPriceInCents,
    isSelected = true,
)

internal fun parseMeasureUnit(rawUnit: String?): MeasureUnit {
    if (rawUnit == null) return MeasureUnit.UNIT
    val normalized = rawUnit.trim().uppercase()
    return when (normalized) {
        "KG", "KILO", "KILOGRAM" -> MeasureUnit.KILOGRAM
        "G", "GR", "GRAM" -> MeasureUnit.GRAM
        "L", "LT", "LITER" -> MeasureUnit.LITER
        "ML", "MILLILITER" -> MeasureUnit.MILLILITER
        "CX", "PCT", "PACOTE", "PACKAGE" -> MeasureUnit.PACKAGE
        else -> MeasureUnit.UNIT
    }
}

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

import com.bitlabbr.minhadespensa.core.domain.model.PriceEntry
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity

fun PriceEntryEntity.toDomain(): PriceEntry = PriceEntry(
    id = this.id,
    productId = this.productId,
    priceInCents = this.priceInCents,
    storeName = this.storeName,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
)

fun PriceEntry.toEntity(): PriceEntryEntity = PriceEntryEntity(
    id = this.id,
    productId = this.productId,
    priceInCents = this.priceInCents,
    storeName = this.storeName,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
)

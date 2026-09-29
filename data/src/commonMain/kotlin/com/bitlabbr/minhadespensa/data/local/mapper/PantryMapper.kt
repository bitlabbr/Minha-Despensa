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

import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.data.local.dto.PantryItemWithCategoryDaoResult
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity

fun PantryItemEntity.toDomain(): PantryItem = PantryItem(
    id = this.id,
    productId = this.productId,
    quantity = this.quantity,
    expirationDate = this.expirationDate,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    batchNumber = this.batchNumber,
)

fun PantryItem.toEntity(): PantryItemEntity = PantryItemEntity(
    id = this.id,
    productId = this.productId,
    quantity = this.quantity,
    expirationDate = this.expirationDate,
    updatedAt = this.updatedAt,
    isDeleted = this.isDeleted,
    batchNumber = this.batchNumber,
)

fun PantryItemWithCategoryDaoResult.toDomain(): PantryItemWithCategory = PantryItemWithCategory(
    pantryItem = this.pantryItem.toDomain(),
    category = this.category,
    name = this.name,
)

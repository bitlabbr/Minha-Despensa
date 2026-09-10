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

import com.bitlabbr.minhadespensa.core.domain.model.ShoppingItem
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingList
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListStatus
import com.bitlabbr.minhadespensa.core.domain.model.ShoppingListType
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingListWithItems

fun ShoppingListEntity.toDomain(items: List<ShoppingItem> = emptyList()): ShoppingList =
    ShoppingList(
        id = id,
        name = name,
        type = runCatching { ShoppingListType.valueOf(type) }.getOrDefault(ShoppingListType.PLANNED),
        status = runCatching { ShoppingListStatus.valueOf(status) }.getOrDefault(ShoppingListStatus.DRAFT),
        items = items,
        budgetInCents = budgedInCents,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
    )

fun ShoppingList.toEntity(): ShoppingListEntity =
    ShoppingListEntity(
        id = id,
        name = name,
        type = type.name,
        status = status.name,
        budgedInCents = budgetInCents,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
    )

fun ShoppingItemEntity.toDomain(): ShoppingItem =
    ShoppingItem(
        id = id,
        listId = listId,
        productId = productId,
        rawText = rawText,
        quantity = quantity,
        priceAtTime = priceAtTime,
        isChecked = isChecked,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
    )

fun ShoppingItem.toEntity(): ShoppingItemEntity =
    ShoppingItemEntity(
        id = id,
        listId = listId,
        productId = productId,
        rawText = rawText,
        quantity = quantity,
        priceAtTime = priceAtTime,
        isChecked = isChecked,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
    )

fun ShoppingListWithItems.toDomain(): ShoppingList =
    list.toDomain(
        items = items.filterNot { it.isDeleted }.map { it.toDomain() }
    )
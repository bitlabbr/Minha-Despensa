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

package com.bitlabbr.minhadespensa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CatalogProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("list_id"),
        Index("product_id"),
    ],
)
data class ShoppingItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "list_id")
    val listId: String,
    @ColumnInfo(name = "product_id")
    val productId: String?,
    @ColumnInfo(name = "raw_text")
    val rawText: String?,
    val quantity: Double,
    @ColumnInfo(name = "price_at_time")
    val priceAtTime: Long?,
    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
)
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
    tableName = "pantry_movements",
    foreignKeys = [
        ForeignKey(
            entity = CatalogProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["type", "timestamp"]),
        Index(value = ["timestamp"])
    ]
)
data class PantryMovementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "pantry_item_id")
    val pantryItemId: String? = null,
    val productId: String,
    val type: String,
    val quantity: Double,
    @ColumnInfo(name = "unit_price_in_cents")
    val unitPriceInCents: Long? = null,
    @ColumnInfo(name = "total_price_in_cents")
    val totalPriceInCents: Long? = null,
    val timestamp: Long,
    @ColumnInfo(name = "store_or_reason")
    val storeOrReason: String? = null,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
)

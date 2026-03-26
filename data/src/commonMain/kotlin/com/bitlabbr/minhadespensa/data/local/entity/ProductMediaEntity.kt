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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_media",
    foreignKeys = [
        ForeignKey(
            entity = CatalogProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ProductMediaEntity(
    @PrimaryKey val productId: String,
    val blob: ByteArray,
    val updatedAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ProductMediaEntity

        if (updatedAt != other.updatedAt) return false
        if (productId != other.productId) return false
        if (!blob.contentEquals(other.blob)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = updatedAt.hashCode()
        result = 31 * result + productId.hashCode()
        result = 31 * result + blob.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ProductMediaEntity(productId='$productId', blob=${blob.contentToString()}, updatedAt=$updatedAt)"
    }

}

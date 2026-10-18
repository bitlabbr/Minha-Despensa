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

package com.bitlabbr.minhadespensa.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
enum class MovementType {
    ENTRY_MANUAL,
    ENTRY_PURCHASE,
    CONSUMPTION,
    EXPIRATION_DISCARD,
    ADJUSTMENT
}

@Serializable
data class PantryMovement(
    val id: String,
    /**
     * Optional reference to the specific pantry item. Nullable because audit ledger movements
     * persist even if a pantry item is deleted, or when recorded from bulk shopping purchases.
     */
    val pantryItemId: String? = null,
    val productId: String,
    val type: MovementType,
    val quantity: Double,
    val unitPriceInCents: Long? = null,
    val totalPriceInCents: Long? = null,
    val updatedAt: Long,
    val storeOrReason: String? = null,
    val isDeleted: Boolean = false,
) {
    val computedTotalPriceInCents: Long?
        get() = totalPriceInCents ?: unitPriceInCents?.let { (quantity * it).roundToLong() }
}

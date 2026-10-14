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
    ENTRY_MANUAL,       // Adição avulsa direta na despensa
    ENTRY_PURCHASE,     // Entrada oriunda de finalização de lista de compras
    CONSUMPTION,        // Baixa por consumo de alimento
    EXPIRATION_DISCARD, // Baixa por descarte de produto vencido
    ADJUSTMENT          // Ajuste de contagem física de estoque
}

@Serializable
data class PantryMovement(
    val id: String,
    val pantryItemId: String? = null,
    val productId: String,
    val type: MovementType,
    val quantity: Double,
    val unitPriceInCents: Long? = null,
    val totalPriceInCents: Long? = null,
    val timestamp: Long,
    val storeOrReason: String? = null,
    val isDeleted: Boolean = false,
) {
    val computedTotalPriceInCents: Long?
        get() = totalPriceInCents ?: unitPriceInCents?.let { (quantity * it).roundToLong() }
}

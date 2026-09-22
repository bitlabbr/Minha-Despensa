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

@Serializable
data class ShoppingList(
    val id: String,
    val name: String,
    val type: ShoppingListType = ShoppingListType.PLANNED,
    val status: ShoppingListStatus = ShoppingListStatus.DRAFT,
    val items: List<ShoppingItem> = emptyList(),
    val budgetInCents: Long? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
){
    val totalCheckedItems: Int
        get() = items.count { it.isChecked && !it.isDeleted }

    val totalActiveItems: Int
        get() = items.count { !it.isDeleted }

    val totalCartInCents: Long
        get() = items
            .filter { it.isChecked && !it.isDeleted }
            .mapNotNull { it.subtotalInCents }
            .sum()

    val isOverBudget: Boolean
        get() = budgetInCents != null && totalCartInCents > budgetInCents
}

enum class ShoppingListType {
    SCRATCHPAD,
    PLANNED,
    ASSISTANT
}

enum class ShoppingListStatus {
    DRAFT,
    SHOPPING,
    COMPLETED,
    CANCELLED
}

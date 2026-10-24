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

package com.bitlabbr.minhadespensa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMutationRequestDto(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String?,
    val timestamp: Long,
)

@Serializable
data class SyncPushRequestDto(
    val clientTimestamp: Long = 0L,
    val localVersion: Long = 0L,
    val mutations: List<SyncMutationRequestDto> = emptyList(),
    val categories: List<CategorySyncDto> = emptyList(),
    val products: List<CatalogProductSyncDto> = emptyList(),
    val shoppingLists: List<ShoppingListSyncDto> = emptyList(),
    val shoppingItems: List<ShoppingItemSyncDto> = emptyList(),
    val pantryItems: List<PantryItemSyncDto> = emptyList(),
    val priceEntries: List<PriceEntrySyncDto> = emptyList(),
    @SerialName("pantryMovements")
    val movements: List<PantryMovementSyncDto> = emptyList(),
    @SerialName("monthlyBudgets")
    val budgets: List<MonthlyBudgetSyncDto> = emptyList(),
)

@Serializable
data class SyncPushResponseDto(
    val serverTimestamp: Long = 0L,
    val serverVersion: Long = 0L,
    @SerialName("totalProcessed")
    val processedCount: Int = 0,
    val productIdMappings: Map<String, String> = emptyMap(),
    val categoryIdMappings: Map<String, String> = emptyMap(),
    val rejectedMutations: List<String> = emptyList(),
)

@Serializable
data class CatalogProductSyncDto(
    val id: String,
    val ean: String? = null,
    val name: String,
    val category: String,
    val brand: String? = null,
    val measureUnit: String,
    val netWeight: Double,
    val thumbnailUrl: String? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val manuallyAdded: Boolean = false,
    val notes: String? = null,
    val imageHash: String? = null,
)

@Serializable
data class ProductMediaSyncDto(
    val productId: String,
    val base64Blob: String? = null,
    val imageHash: String? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class CategorySyncDto(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class PantryItemSyncDto(
    val id: String,
    val productId: String,
    val quantity: Double,
    val expirationDate: Long? = null,
    val batchNumber: String? = null,
    val unitPriceInCents: Long? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class PantryMovementSyncDto(
    val id: String,
    val pantryItemId: String? = null,
    val productId: String,
    val type: String,
    val quantity: Double,
    val unitPriceInCents: Long? = null,
    val totalPriceInCents: Long? = null,
    val updatedAt: Long,
    val storeOrReason: String? = null,
    val isDeleted: Boolean = false,
)

@Serializable
data class ShoppingListSyncDto(
    val id: String,
    val name: String,
    @SerialName("type")
    val listType: String = "PLANNED",
    @SerialName("status")
    val listStatus: String = "DRAFT",
    val budgetInCents: Long? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class ShoppingItemSyncDto(
    val id: String,
    val listId: String,
    val productId: String? = null,
    val rawText: String? = null,
    val quantity: Double,
    val priceAtTime: Long? = null,
    val isChecked: Boolean = false,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class PriceEntrySyncDto(
    val id: String,
    val productId: String,
    val priceInCents: Long,
    val storeName: String? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class MonthlyBudgetSyncDto(
    val year: Int,
    val month: Int,
    val budgetInCents: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
)

@Serializable
data class SyncEntitiesDto(
    val products: List<CatalogProductSyncDto> = emptyList(),
    val productMedia: List<ProductMediaSyncDto> = emptyList(),
    val categories: List<CategorySyncDto> = emptyList(),
    val pantryItems: List<PantryItemSyncDto> = emptyList(),
    val movements: List<PantryMovementSyncDto> = emptyList(),
    val shoppingLists: List<ShoppingListSyncDto> = emptyList(),
    val shoppingItems: List<ShoppingItemSyncDto> = emptyList(),
    val priceEntries: List<PriceEntrySyncDto> = emptyList(),
    val budgets: List<MonthlyBudgetSyncDto> = emptyList(),
)

@Serializable
data class SyncPullResponseDto(
    val serverTimestamp: Long = 0L,
    @SerialName("serverVersion")
    val currentVersion: Long = 0L,
    val hasMore: Boolean = false,
    val entities: SyncEntitiesDto = SyncEntitiesDto(),
    val categories: List<CategorySyncDto> = emptyList(),
    val products: List<CatalogProductSyncDto> = emptyList(),
    val shoppingLists: List<ShoppingListSyncDto> = emptyList(),
    val shoppingItems: List<ShoppingItemSyncDto> = emptyList(),
    val pantryItems: List<PantryItemSyncDto> = emptyList(),
    val priceEntries: List<PriceEntrySyncDto> = emptyList(),
    @SerialName("pantryMovements")
    val movements: List<PantryMovementSyncDto> = emptyList(),
    @SerialName("monthlyBudgets")
    val budgets: List<MonthlyBudgetSyncDto> = emptyList(),
) {
    val effectiveEntities: SyncEntitiesDto
        get() = if (entities.products.isNotEmpty() || entities.categories.isNotEmpty() ||
            entities.pantryItems.isNotEmpty() || entities.shoppingLists.isNotEmpty() ||
            entities.priceEntries.isNotEmpty() || entities.movements.isNotEmpty() ||
            entities.budgets.isNotEmpty()
        ) {
            entities
        } else {
            SyncEntitiesDto(
                products = products,
                categories = categories,
                shoppingLists = shoppingLists,
                shoppingItems = shoppingItems,
                pantryItems = pantryItems,
                priceEntries = priceEntries,
                movements = movements,
                budgets = budgets,
            )
        }
}

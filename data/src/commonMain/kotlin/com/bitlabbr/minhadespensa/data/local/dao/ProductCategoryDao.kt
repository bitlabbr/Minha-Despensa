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

package com.bitlabbr.minhadespensa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bitlabbr.minhadespensa.data.local.entity.ProductCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(category: ProductCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(categories: List<ProductCategoryEntity>)

    @Query("SELECT * FROM product_categories WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<ProductCategoryEntity>>

    @Query("SELECT * FROM product_categories")
    suspend fun getAllSnapshot(): List<ProductCategoryEntity>

    @Query("SELECT COUNT(*) FROM product_categories")
    suspend fun countAll(): Int

    @Query("SELECT * FROM product_categories WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProductCategoryEntity?

    @Query(
        """
        UPDATE product_categories 
        SET name = :newName, isDeleted = 0, updatedAt = :updatedAt 
        WHERE id = :id AND updatedAt <= :updatedAt
        """
    )
    suspend fun updateName(id: String, newName: String, updatedAt: Long): Int

    @Query(
        """
        UPDATE product_categories 
        SET isDeleted = 1, updatedAt = :updatedAt 
        WHERE id = :id AND updatedAt <= :updatedAt
        """
    )
    suspend fun markAsDeleted(id: String, updatedAt: Long): Int

    @Query(
        """
        UPDATE product_categories 
        SET name = :name, isDefault = :isDefault, updatedAt = :updatedAt, isDeleted = :isDeleted 
        WHERE id = :id AND updatedAt <= :updatedAt
        """
    )
    suspend fun updateIfNewer(
        id: String,
        name: String,
        isDefault: Boolean,
        updatedAt: Long,
        isDeleted: Boolean,
    ): Int

    @Query("DELETE FROM product_categories WHERE isDefault = 0")
    suspend fun clearCustomCategories(): Int
}

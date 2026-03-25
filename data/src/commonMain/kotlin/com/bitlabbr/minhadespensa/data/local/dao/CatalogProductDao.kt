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
import androidx.room.Update
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: CatalogProductEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(product: CatalogProductEntity): Int

    @Query("SELECT EXISTS(SELECT 1 FROM catalog_products WHERE id = :id)")
    fun exists(id: String): Flow<Boolean>

    @Query("SELECT * FROM catalog_products WHERE id = :id")
    fun findById(id: String): Flow<CatalogProductEntity?>

    @Query("SELECT * FROM catalog_products WHERE ean = :ean AND isDeleted = 0")
    fun findByEan(ean: String): Flow<CatalogProductEntity?>

    @Query("""
        SELECT * FROM catalog_products 
        WHERE (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%') 
        AND isDeleted = 0
    """)
    fun searchByNameOrBrand(query: String): Flow<List<CatalogProductEntity>>

    @Query("SELECT * FROM catalog_products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<CatalogProductEntity>>

    @Query("""
        UPDATE catalog_products 
        SET isDeleted = 1, updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markAsDeleted(id: String, updatedAt: Long)

    @Query("DELETE FROM catalog_products WHERE id = :id")
    suspend fun deleteProductById(id: String)
}

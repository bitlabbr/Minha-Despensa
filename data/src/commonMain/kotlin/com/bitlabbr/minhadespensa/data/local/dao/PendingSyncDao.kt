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
import com.bitlabbr.minhadespensa.data.local.entity.PendingSyncEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PendingSyncEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PendingSyncEntryEntity>)

    @Query("SELECT * FROM pending_sync_entries ORDER BY createdAt ASC, id ASC LIMIT :limit")
    suspend fun getAllPending(limit: Int = 100): List<PendingSyncEntryEntity>

    @Query("SELECT COUNT(*) FROM pending_sync_entries")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_sync_entries")
    suspend fun getPendingCount(): Int

    @Query("DELETE FROM pending_sync_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_sync_entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM pending_sync_entries WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteByEntity(entityType: String, entityId: String)

    @Query("DELETE FROM pending_sync_entries")
    suspend fun clearAll()
}

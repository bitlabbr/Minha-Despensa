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

package com.bitlabbr.minhadespensa.core.domain.repository

import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun observeActiveHousehold(): Flow<Household?>
    suspend fun getActiveHousehold(): Household?
    suspend fun getUserHouseholds(): Result<List<HouseholdDetail>>
    suspend fun createHousehold(name: String): Result<HouseholdDetail>
    suspend fun updateHousehold(id: String, name: String): Result<HouseholdDetail>
    suspend fun joinHousehold(inviteCode: String): Result<HouseholdDetail>
    suspend fun leaveHousehold(id: String): Result<Unit>
    suspend fun regenerateInviteCode(id: String): Result<String>
    suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit>
    suspend fun switchActiveHousehold(householdId: String): Result<Unit>
}

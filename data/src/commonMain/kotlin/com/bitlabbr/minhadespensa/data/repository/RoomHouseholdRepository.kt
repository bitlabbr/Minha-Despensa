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

package com.bitlabbr.minhadespensa.data.repository

import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdMember
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.LogSanitizer
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.dao.AppKeyValueDao
import com.bitlabbr.minhadespensa.data.local.entity.AppKeyValueEntity
import com.bitlabbr.minhadespensa.data.remote.HouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomHouseholdRepository(
    private val householdRemoteDataSource: HouseholdRemoteDataSource,
    private val appKeyValueDao: AppKeyValueDao,
    private val authRepository: AuthRepository,
    private val logger: AppLogger? = null,
) : HouseholdRepository {

    private val tag = "Household"
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeActiveHousehold(): Flow<Household?> =
        authRepository.observeCurrentSession().map { it?.activeHousehold }

    override suspend fun getActiveHousehold(): Household? =
        authRepository.getCurrentSession()?.activeHousehold

    override suspend fun getUserHouseholds(): Result<List<HouseholdDetail>> = runCatching {
        val dtos = householdRemoteDataSource.listHouseholds().getOrThrow()
        dtos.map { it.toDetail() }
    }

    override suspend fun createHousehold(name: String): Result<HouseholdDetail> = runCatching {
        val dto = householdRemoteDataSource.createHousehold(name).getOrThrow()
        dto.toDetail()
    }

    override suspend fun updateHousehold(id: String, name: String): Result<HouseholdDetail> = runCatching {
        val dto = householdRemoteDataSource.updateHousehold(id, name).getOrThrow()
        val currentSession = authRepository.getCurrentSession()
        if (currentSession?.activeHousehold?.id == id) {
            val updated = currentSession.copy(
                activeHousehold = currentSession.activeHousehold.copy(name = dto.name),
            )
            // Save updated session
            val now = getCurrentTime()
            appKeyValueDao.put(
                AppKeyValueEntity(
                    RoomAuthRepository.KEY_SESSION,
                    json.encodeToString(updated),
                    now,
                ),
            )
        }
        dto.toDetail()
    }

    override suspend fun joinHousehold(inviteCode: String): Result<HouseholdDetail> = runCatching {
        val dto = householdRemoteDataSource.joinHousehold(inviteCode).getOrThrow()
        dto.toDetail()
    }

    override suspend fun leaveHousehold(id: String): Result<Unit> = runCatching {
        householdRemoteDataSource.leaveHousehold(id).getOrThrow()
    }

    override suspend fun regenerateInviteCode(id: String): Result<String> = runCatching {
        householdRemoteDataSource.regenerateInviteCode(id).getOrThrow()
    }

    override suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit> = runCatching {
        householdRemoteDataSource.removeMember(householdId, memberUserId).getOrThrow()
    }

    override suspend fun switchActiveHousehold(householdId: String): Result<Unit> = runCatching {
        val current = authRepository.getCurrentSession() ?: throw IllegalStateException("No active session")
        val householdDetail = householdRemoteDataSource.getHousehold(householdId)
            .recoverCatching {
                householdRemoteDataSource.listHouseholds().getOrThrow().first { it.id == householdId }
            }
            .getOrThrow()
        val newHousehold = Household(
            id = householdDetail.id,
            name = householdDetail.name,
            inviteCode = householdDetail.inviteCode,
            role = runCatching { HouseholdRole.valueOf(householdDetail.role) }.getOrDefault(HouseholdRole.MEMBER),
        )
        val updatedSession = current.copy(activeHousehold = newHousehold)
        val now = getCurrentTime()
        appKeyValueDao.put(AppKeyValueEntity(RoomAuthRepository.KEY_ACTIVE_HOUSEHOLD_ID, householdId, now))
        appKeyValueDao.put(
            AppKeyValueEntity(
                RoomAuthRepository.KEY_SESSION,
                json.encodeToString(updatedSession),
                now,
            ),
        )
    }

    private fun HouseholdDto.toDetail(): HouseholdDetail = HouseholdDetail(
        id = id,
        name = name,
        inviteCode = inviteCode,
        role = runCatching { HouseholdRole.valueOf(role) }.getOrDefault(HouseholdRole.MEMBER),
        members = members.map {
            HouseholdMember(
                userId = it.userId,
                name = it.name,
                email = it.email,
                role = runCatching { HouseholdRole.valueOf(it.role) }.getOrDefault(HouseholdRole.MEMBER),
                joinedAt = it.joinedAt,
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

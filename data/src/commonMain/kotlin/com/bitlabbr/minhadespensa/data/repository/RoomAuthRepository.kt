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

import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.model.UserAccount
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.dao.AppKeyValueDao
import com.bitlabbr.minhadespensa.data.local.entity.AppKeyValueEntity
import com.bitlabbr.minhadespensa.data.remote.AuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.AuthResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomAuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val appKeyValueDao: AppKeyValueDao,
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        const val KEY_SESSION = "auth_session"
        const val KEY_ACTIVE_HOUSEHOLD_ID = "active_household_id"
    }

    override fun observeCurrentSession(): Flow<AuthSession?> =
        appKeyValueDao.observe(KEY_SESSION).map { raw ->
            raw?.let { runCatching { json.decodeFromString<AuthSession>(it) }.getOrNull() }
        }

    override suspend fun getCurrentSession(): AuthSession? {
        val raw = appKeyValueDao.get(KEY_SESSION) ?: return null
        return runCatching { json.decodeFromString<AuthSession>(raw) }.getOrNull()
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthSession> = runCatching {
        val response = authRemoteDataSource.register(name, email, password).getOrThrow()
        saveSessionFromResponse(response)
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> = runCatching {
        val response = authRemoteDataSource.login(email, password).getOrThrow()
        saveSessionFromResponse(response)
    }

    override suspend fun logout(forceDiscard: Boolean): Result<Unit> = runCatching {
        clearSession()
    }

    override suspend fun refreshSession(): Result<AuthSession> = runCatching {
        val current = getCurrentSession() ?: throw IllegalStateException("No active session to refresh")
        val response = authRemoteDataSource.refreshToken(current.refreshToken).getOrThrow()
        val updated = current.copy(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresInSeconds = response.expiresIn,
        )
        persistSession(updated)
        updated
    }

    override suspend fun clearSession() {
        appKeyValueDao.delete(KEY_ACCESS_TOKEN)
        appKeyValueDao.delete(KEY_REFRESH_TOKEN)
        appKeyValueDao.delete(KEY_SESSION)
        appKeyValueDao.delete(KEY_ACTIVE_HOUSEHOLD_ID)
    }

    private suspend fun saveSessionFromResponse(response: AuthResponseDto): AuthSession {
        val household = response.household ?: HouseholdDto(
            id = response.user.defaultHouseholdId ?: "default",
            name = "Minha Casa",
            inviteCode = "",
        )
        val role = runCatching { HouseholdRole.valueOf(household.role) }.getOrDefault(HouseholdRole.MEMBER)
        val session = AuthSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresInSeconds = response.expiresIn,
            user = UserAccount(
                id = response.user.id,
                name = response.user.name,
                email = response.user.email,
                defaultHouseholdId = response.user.defaultHouseholdId ?: household.id,
            ),
            activeHousehold = Household(
                id = household.id,
                name = household.name,
                inviteCode = household.inviteCode,
                role = role,
            ),
        )
        persistSession(session)
        appKeyValueDao.put(AppKeyValueEntity(KEY_ACTIVE_HOUSEHOLD_ID, household.id, getCurrentTime()))
        return session
    }

    private suspend fun persistSession(session: AuthSession) {
        val now = getCurrentTime()
        appKeyValueDao.put(AppKeyValueEntity(KEY_ACCESS_TOKEN, session.accessToken, now))
        appKeyValueDao.put(AppKeyValueEntity(KEY_REFRESH_TOKEN, session.refreshToken, now))
        appKeyValueDao.put(AppKeyValueEntity(KEY_SESSION, json.encodeToString(session), now))
    }
}

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

package com.bitlabbr.minhadespensa.core.domain.fakes

import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.model.UserAccount
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    private val _session = MutableStateFlow<AuthSession?>(null)
    var shouldFail: Boolean = false
    var failureException: Throwable = RuntimeException("Auth failed")

    override fun observeCurrentSession(): Flow<AuthSession?> = _session.asStateFlow()

    override suspend fun getCurrentSession(): AuthSession? = _session.value

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        if (shouldFail) return Result.failure(failureException)
        val user = UserAccount(id = "user-123", name = "Test User", email = email)
        val household = Household(id = "hh-1", name = "Test Household", inviteCode = "ABCDEFGH", role = HouseholdRole.OWNER)
        val session = AuthSession(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-123",
            expiresInSeconds = 3600L,
            user = user,
            activeHousehold = household,
        )
        _session.value = session
        return Result.success(session)
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthSession> {
        if (shouldFail) return Result.failure(failureException)
        val user = UserAccount(id = "user-123", name = name, email = email)
        val household = Household(id = "hh-1", name = "Test Household", inviteCode = "ABCDEFGH", role = HouseholdRole.OWNER)
        val session = AuthSession(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-123",
            expiresInSeconds = 3600L,
            user = user,
            activeHousehold = household,
        )
        _session.value = session
        return Result.success(session)
    }

    override suspend fun logout(forceDiscard: Boolean): Result<Unit> {
        if (shouldFail) return Result.failure(failureException)
        _session.value = null
        return Result.success(Unit)
    }

    override suspend fun refreshSession(): Result<AuthSession> {
        if (shouldFail) return Result.failure(failureException)
        val current = _session.value ?: return Result.failure(IllegalStateException("No session"))
        val updated = current.copy(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        _session.value = updated
        return Result.success(updated)
    }

    override suspend fun clearSession() {
        _session.value = null
    }
}

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

import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdMember
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeHouseholdRepository : HouseholdRepository {
    val households = MutableStateFlow<Map<String, HouseholdDetail>>(emptyMap())
    private val _activeHousehold = MutableStateFlow<Household?>(null)
    var shouldFail: Boolean = false
    var failureException: Throwable = RuntimeException("Household error")

    override fun observeActiveHousehold(): Flow<Household?> = _activeHousehold.asStateFlow()

    override suspend fun getActiveHousehold(): Household? = _activeHousehold.value

    override suspend fun getUserHouseholds(): Result<List<HouseholdDetail>> {
        if (shouldFail) return Result.failure(failureException)
        return Result.success(households.value.values.toList())
    }

    override suspend fun createHousehold(name: String): Result<HouseholdDetail> {
        if (shouldFail) return Result.failure(failureException)
        val id = "hh-${households.value.size + 1}"
        val household = HouseholdDetail(
            id = id,
            name = name,
            inviteCode = "ABCDEFGH",
            role = HouseholdRole.OWNER,
            members = listOf(
                HouseholdMember(
                    userId = "user-123",
                    name = "Test User",
                    email = "test@example.com",
                    role = HouseholdRole.OWNER,
                    joinedAt = 1000L,
                ),
            ),
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        households.value = households.value + (household.id to household)
        return Result.success(household)
    }

    override suspend fun updateHousehold(id: String, name: String): Result<HouseholdDetail> {
        if (shouldFail) return Result.failure(failureException)
        val existing = households.value[id] ?: return Result.failure(NoSuchElementException())
        val updated = existing.copy(name = name)
        households.value = households.value + (id to updated)
        if (_activeHousehold.value?.id == id) {
            _activeHousehold.value = _activeHousehold.value?.copy(name = name)
        }
        return Result.success(updated)
    }

    override suspend fun joinHousehold(inviteCode: String): Result<HouseholdDetail> {
        if (shouldFail) return Result.failure(failureException)
        val household = HouseholdDetail(
            id = "hh-joined",
            name = "Joined Household",
            inviteCode = inviteCode,
            role = HouseholdRole.MEMBER,
            members = emptyList(),
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        households.value = households.value + (household.id to household)
        return Result.success(household)
    }

    override suspend fun leaveHousehold(id: String): Result<Unit> {
        if (shouldFail) return Result.failure(failureException)
        households.value = households.value - id
        if (_activeHousehold.value?.id == id) {
            _activeHousehold.value = null
        }
        return Result.success(Unit)
    }

    override suspend fun regenerateInviteCode(id: String): Result<String> {
        if (shouldFail) return Result.failure(failureException)
        val newCode = "NEWCODE1"
        val existing = households.value[id]
        if (existing != null) {
            households.value = households.value + (id to existing.copy(inviteCode = newCode))
        }
        return Result.success(newCode)
    }

    override suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit> {
        if (shouldFail) return Result.failure(failureException)
        val existing = households.value[householdId]
        if (existing != null) {
            val updatedMembers = existing.members.filterNot { it.userId == memberUserId }
            households.value = households.value + (householdId to existing.copy(members = updatedMembers))
        }
        return Result.success(Unit)
    }

    override suspend fun switchActiveHousehold(householdId: String): Result<Unit> {
        if (shouldFail) return Result.failure(failureException)
        val target = households.value[householdId]
        _activeHousehold.value = target?.let {
            Household(id = it.id, name = it.name, inviteCode = it.inviteCode, role = it.role)
        } ?: Household(id = householdId, name = "Household $householdId", inviteCode = "CODE1234")
        return Result.success(Unit)
    }
}

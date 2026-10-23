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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants

class ManageHouseholdUseCase(
    private val householdRepository: HouseholdRepository,
    private val syncRepository: SyncRepository,
) {
    suspend fun createHousehold(name: String): Result<HouseholdDetail> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { CoreConstants.Validation.ERROR_HOUSEHOLD_NAME_BLANK }
        require(trimmed.length <= CoreConstants.Household.NAME_MAX_LENGTH) {
            CoreConstants.Validation.ERROR_HOUSEHOLD_NAME_TOO_LONG
        }

        syncRepository.flushOutboxBeforeSwitch(forceDiscard = false)

        val household = householdRepository.createHousehold(trimmed).getOrThrow()
        syncRepository.clearLocalDataForHouseholdSwitch()
        householdRepository.switchActiveHousehold(household.id).getOrThrow()
        syncRepository.sync().getOrThrow()

        household
    }

    suspend fun updateHousehold(id: String, name: String): Result<HouseholdDetail> = runCatching {
        require(id.isNotBlank()) { "ID da despensa é obrigatório" }
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { CoreConstants.Validation.ERROR_HOUSEHOLD_NAME_BLANK }
        require(trimmed.length <= CoreConstants.Household.NAME_MAX_LENGTH) {
            CoreConstants.Validation.ERROR_HOUSEHOLD_NAME_TOO_LONG
        }
        householdRepository.updateHousehold(id, trimmed).getOrThrow()
    }

    suspend fun leaveHousehold(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "ID da despensa é obrigatório" }
        householdRepository.leaveHousehold(id).getOrThrow()
    }

    suspend fun regenerateInviteCode(householdId: String): Result<String> = runCatching {
        require(householdId.isNotBlank()) { "ID da despensa é obrigatório" }
        householdRepository.regenerateInviteCode(householdId).getOrThrow()
    }

    suspend fun removeMember(householdId: String, userId: String): Result<Unit> = runCatching {
        require(householdId.isNotBlank()) { "ID da despensa é obrigatório" }
        require(userId.isNotBlank()) { "ID do usuário é obrigatório" }
        householdRepository.removeMember(householdId, userId).getOrThrow()
    }
}

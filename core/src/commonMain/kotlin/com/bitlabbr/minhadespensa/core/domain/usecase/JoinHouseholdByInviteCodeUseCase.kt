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

class JoinHouseholdByInviteCodeUseCase(
    private val householdRepository: HouseholdRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(inviteCode: String): Result<HouseholdDetail> = runCatching {
        val normalizedCode = inviteCode.trim().uppercase()
        require(normalizedCode.length == CoreConstants.Household.INVITE_CODE_LENGTH) {
            CoreConstants.Validation.ERROR_INVITE_CODE_INVALID
        }

        val household = householdRepository.joinHousehold(normalizedCode).getOrThrow()
        householdRepository.switchActiveHousehold(household.id).getOrThrow()
        syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()

        household
    }
}

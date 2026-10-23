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

import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants

class RegisterUserUseCase(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        keepLocalData: Boolean = true,
    ): Result<AuthSession> = runCatching {
        require(name.isNotBlank()) { CoreConstants.Validation.ERROR_USER_NAME_BLANK }
        require(name.trim().length <= CoreConstants.Auth.NAME_MAX_LENGTH) {
            CoreConstants.Validation.ERROR_USER_NAME_TOO_LONG
        }
        val trimmedEmail = email.trim().lowercase()
        require(CoreConstants.Auth.EMAIL_REGEX.matches(trimmedEmail)) {
            CoreConstants.Validation.ERROR_EMAIL_INVALID
        }
        require(password.length >= CoreConstants.Auth.PASSWORD_MIN_LENGTH) {
            CoreConstants.Validation.ERROR_PASSWORD_TOO_SHORT
        }

        if (!keepLocalData) {
            syncRepository.clearLocalDataForHouseholdSwitch()
        }

        val session = authRepository.register(
            name = name.trim(),
            email = trimmedEmail,
            password = password,
        ).getOrThrow()

        if (keepLocalData) {
            syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()
        }

        syncRepository.sync().getOrThrow()

        session
    }
}

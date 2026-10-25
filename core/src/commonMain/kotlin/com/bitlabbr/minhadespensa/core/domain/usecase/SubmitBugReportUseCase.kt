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

import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.repository.SupportRepository

class SubmitBugReportUseCase(
    private val supportRepository: SupportRepository
) {
    suspend operator fun invoke(
        category: SupportCategory,
        title: String,
        description: String,
        userContact: String? = null,
        includeLogs: Boolean = true
    ): Result<SupportReceipt> {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()

        if (trimmedTitle.length < 3) {
            return Result.failure(IllegalArgumentException("O título deve conter pelo menos 3 caracteres."))
        }
        if (trimmedTitle.length > 255) {
            return Result.failure(IllegalArgumentException("O título deve conter no máximo 255 caracteres."))
        }
        if (trimmedDescription.length < 5) {
            return Result.failure(IllegalArgumentException("A descrição deve conter pelo menos 5 caracteres."))
        }

        val diagnostics = supportRepository.getDeviceDiagnostics(includeLogs = includeLogs)
        val report = SupportReport(
            category = category,
            title = trimmedTitle,
            description = trimmedDescription,
            diagnostics = diagnostics,
            userContact = userContact?.trim()?.takeIf { it.isNotBlank() }
        )

        return supportRepository.submitReport(report)
    }
}

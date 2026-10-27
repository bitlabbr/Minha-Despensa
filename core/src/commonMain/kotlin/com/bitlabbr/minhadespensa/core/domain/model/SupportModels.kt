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

package com.bitlabbr.minhadespensa.core.domain.model

enum class SupportCategory {
    BUG,
    SUGGESTION,
    QUESTION,
    CRASH;

    companion object {
        fun fromRaw(raw: String?): SupportCategory? {
            if (raw.isNullOrBlank()) return null
            return entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
        }
    }
}

data class DeviceDiagnostics(
    val appVersion: String,
    val deviceModel: String? = null,
    val osVersion: String? = null,
    val userId: String? = null,
    val householdId: String? = null,
    val deviceLogs: String? = null
)

data class SupportReport(
    val category: SupportCategory,
    val title: String,
    val description: String,
    val diagnostics: DeviceDiagnostics,
    val userContact: String? = null
)

data class SupportReceipt(
    val id: String,
    val protocol: String,
    val category: SupportCategory,
    val title: String,
    val status: String,
    val hasLogs: Boolean,
    val createdAt: Long
)

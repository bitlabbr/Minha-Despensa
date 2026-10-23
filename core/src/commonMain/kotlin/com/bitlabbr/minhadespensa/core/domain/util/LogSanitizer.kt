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

package com.bitlabbr.minhadespensa.core.domain.util

object LogSanitizer {

    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return "[EMPTY]"
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return "***@${email.substringAfter('@', "")}"
        val namePart = email.substring(0, atIndex)
        val domainPart = email.substring(atIndex + 1)
        val visibleChar = namePart.first()
        return "$visibleChar***@$domainPart"
    }

    fun maskToken(token: String?): String {
        if (token.isNullOrBlank()) return "[NONE]"
        if (token.length <= 12) return "[REDACTED]"
        val prefix = token.take(6)
        val suffix = token.takeLast(4)
        return "$prefix...[REDACTED]...$suffix"
    }

    fun maskInviteCode(code: String?): String {
        if (code.isNullOrBlank()) return "[EMPTY]"
        val prefix = code.take(4)
        return "$prefix-****"
    }
}

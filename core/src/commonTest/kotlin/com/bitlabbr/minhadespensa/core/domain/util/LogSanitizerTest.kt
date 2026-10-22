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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogSanitizerTest {

    @Test
    fun maskEmailShouldProtectLocalPartWhilePreservingDomain() {
        assertEquals("w***@gmail.com", LogSanitizer.maskEmail("wjsarq@gmail.com"))
        assertEquals("a***@empresa.com.br", LogSanitizer.maskEmail("antonio@empresa.com.br"))
        assertEquals("[EMPTY]", LogSanitizer.maskEmail(null))
        assertEquals("[EMPTY]", LogSanitizer.maskEmail(""))
    }

    @Test
    fun maskTokenShouldRedactMiddleBytesAndNeverExposeCompleteSecret() {
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.sflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val masked = LogSanitizer.maskToken(jwt)
        assertTrue(masked.startsWith("eyJhbG"))
        assertTrue(masked.endsWith("sw5c"))
        assertTrue(masked.contains("[REDACTED]"))
        assertFalse(masked.contains("sflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
        assertEquals("[NONE]", LogSanitizer.maskToken(null))
        assertEquals("[REDACTED]", LogSanitizer.maskToken("short_token"))
    }

    @Test
    fun maskInviteCodeShouldObfuscateTrailingCode() {
        val masked = LogSanitizer.maskInviteCode("CASA-7X9K")
        assertTrue(masked.startsWith("CASA"))
        assertTrue(masked.endsWith("****"))
        assertEquals("[EMPTY]", LogSanitizer.maskInviteCode(null))
    }
}

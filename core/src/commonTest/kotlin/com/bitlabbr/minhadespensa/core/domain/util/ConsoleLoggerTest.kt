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
import kotlin.test.assertTrue

class ConsoleLoggerTest {

    private val logger = ConsoleLogger(moduleName = "TestModule")

    @Test
    fun `ConsoleLogger methods execute without throwing exceptions`() {
        // Verify all log levels run cleanly
        logger.d("TestTag", "Debug message")
        logger.i("TestTag", "Info message")
        logger.w("TestTag", "Warning message")
        logger.w("TestTag", "Warning message with error", IllegalStateException("Test error"))
        logger.e("TestTag", "Error message")
        logger.e("TestTag", "Error message with error", RuntimeException("Crash test"))

        assertTrue(true)
    }
}

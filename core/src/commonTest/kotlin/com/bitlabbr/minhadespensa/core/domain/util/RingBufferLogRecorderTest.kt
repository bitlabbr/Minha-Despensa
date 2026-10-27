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
import kotlin.test.assertTrue

class RingBufferLogRecorderTest {

    @Test
    fun `record should append non-blank lines and respect order`() {
        val recorder = RingBufferLogRecorder(maxEntries = 5)

        recorder.record("Line 1")
        recorder.record("   ") // Should be ignored
        recorder.record("Line 2")

        val snapshot = recorder.getSnapshot()
        assertEquals(2, snapshot.size)
        assertEquals("Line 1", snapshot[0])
        assertEquals("Line 2", snapshot[1])
        assertEquals("Line 1\nLine 2", recorder.dumpToString())
    }

    @Test
    fun `record should evict oldest entries when capacity is exceeded`() {
        val recorder = RingBufferLogRecorder(maxEntries = 3)

        recorder.record("Line 1")
        recorder.record("Line 2")
        recorder.record("Line 3")
        assertEquals(listOf("Line 1", "Line 2", "Line 3"), recorder.getSnapshot())

        recorder.record("Line 4")
        assertEquals(listOf("Line 2", "Line 3", "Line 4"), recorder.getSnapshot())
        assertEquals(3, recorder.size)

        recorder.record("Line 5")
        assertEquals(listOf("Line 3", "Line 4", "Line 5"), recorder.getSnapshot())
    }

    @Test
    fun `clear should empty the buffer`() {
        val recorder = RingBufferLogRecorder(maxEntries = 10)
        recorder.record("Line 1")
        recorder.record("Line 2")
        assertEquals(2, recorder.size)

        recorder.clear()
        assertEquals(0, recorder.size)
        assertTrue(recorder.getSnapshot().isEmpty())
        assertEquals("", recorder.dumpToString())
    }

    @Test
    fun `ConsoleLogger with recorder should stream all log levels to buffer`() {
        val recorder = RingBufferLogRecorder(maxEntries = 10)
        val logger = ConsoleLogger(moduleName = "TestModule", recorder = recorder)

        logger.d("DebugTag", "debug message")
        logger.i("InfoTag", "info message")
        logger.w("WarnTag", "warn message", RuntimeException("warn-err"))
        logger.e("ErrorTag", "error message", IllegalStateException("err-fatal"))

        val snapshot = recorder.getSnapshot()
        assertEquals(4, snapshot.size)
        assertTrue(snapshot[0].contains("[D] [Minha Despensa] [TestModule] [DebugTag]: debug message"))
        assertTrue(snapshot[1].contains("[I] [Minha Despensa] [TestModule] [InfoTag]: info message"))
        assertTrue(snapshot[2].contains("[W] [Minha Despensa] [TestModule] [WarnTag]: warn message"))
        assertTrue(snapshot[2].contains("RuntimeException: warn-err"))
        assertTrue(snapshot[3].contains("[E] [Minha Despensa] [TestModule] [ErrorTag]: error message"))
        assertTrue(snapshot[3].contains("IllegalStateException: err-fatal"))
    }
}

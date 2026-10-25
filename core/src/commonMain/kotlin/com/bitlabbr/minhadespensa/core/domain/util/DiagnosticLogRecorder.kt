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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface DiagnosticLogRecorder {
    fun record(line: String)
    fun getSnapshot(): List<String>
    fun dumpToString(): String
    fun clear()
    val size: Int
}

class RingBufferLogRecorder(
    private val maxEntries: Int = 1500
) : DiagnosticLogRecorder {

    private val _buffer = MutableStateFlow<List<String>>(emptyList())

    override fun record(line: String) {
        if (line.isBlank()) return
        _buffer.update { current ->
            if (current.size >= maxEntries) {
                val dropCount = current.size - maxEntries + 1
                current.drop(dropCount) + line
            } else {
                current + line
            }
        }
    }

    override fun getSnapshot(): List<String> = _buffer.value

    override fun dumpToString(): String = _buffer.value.joinToString("\n")

    override fun clear() {
        _buffer.value = emptyList()
    }

    override val size: Int
        get() = _buffer.value.size
}

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

package com.bitlabbr.minhadespensa.data.local

import com.bitlabbr.minhadespensa.data.util.toByteArray
import kotlinx.cinterop.*
import platform.Foundation.*

actual class TestFileReader {
    @OptIn(ExperimentalForeignApi::class)
    actual fun readBytes(fileName: String): ByteArray {
        val path = NSBundle.mainBundle.pathForResource(fileName, null)
            ?: throw IllegalArgumentException("File not found in Bundle: $fileName")
        val data = NSData.dataWithContentsOfFile(path)
            ?: throw IllegalStateException("File not found in Bundle: $fileName")
        return data.toByteArray()
    }
}
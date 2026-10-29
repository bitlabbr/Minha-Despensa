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

package com.bitlabbr.minhadespensa.app.provider

import com.bitlabbr.minhadespensa.data.util.SupportPlatformProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

class IosSupportPlatformProvider : SupportPlatformProvider {

    override fun getDeviceModel(): String = "Apple iOS Device"

    override fun getOsVersion(): String = "iOS"

    override fun getAppVersionName(): String = "1.2.1-alpha"

    @OptIn(ExperimentalForeignApi::class)
    private fun getCrashFilePath(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val basePath = requireNotNull(documentDirectory?.path)
        return "$basePath/pending_crash.txt"
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun savePendingCrash(stackTrace: String) {
        runCatching {
            val path = getCrashFilePath()
            (stackTrace as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getPendingCrash(): String? {
        return runCatching {
            val path = getCrashFilePath()
            if (NSFileManager.defaultManager.fileExistsAtPath(path)) {
                NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)?.toString()
            } else {
                null
            }
        }.getOrNull()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun clearPendingCrash() {
        runCatching {
            val path = getCrashFilePath()
            if (NSFileManager.defaultManager.fileExistsAtPath(path)) {
                NSFileManager.defaultManager.removeItemAtPath(path, error = null)
            }
        }
    }

    override suspend fun shareLogFile(title: String, content: String): Result<Unit> {
        return Result.success(Unit)
    }
}

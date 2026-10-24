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

package com.bitlabbr.minhadespensa.uisystem.fakes

import com.bitlabbr.minhadespensa.core.domain.model.AppVersionInfo
import com.bitlabbr.minhadespensa.core.domain.repository.AppUpdateRepository

class FakeAppUpdateRepository(
    var isSupported: Boolean = true,
    var versionName: String = "1.0.0",
    var versionCode: Long = 100L,
    var updateResult: Result<AppVersionInfo?> = Result.success(null),
    var downloadResult: Result<Unit> = Result.success(Unit),
) : AppUpdateRepository {

    var checkForUpdatesCalled = false
    var downloadAndInstallCalled = false
    var lastInstalledVersion: AppVersionInfo? = null

    override fun isSelfUpdateSupported(): Boolean = isSupported

    override fun getCurrentVersionName(): String = versionName

    override fun getCurrentVersionCode(): Long = versionCode

    override suspend fun checkForUpdates(): Result<AppVersionInfo?> {
        checkForUpdatesCalled = true
        return updateResult
    }

    override suspend fun downloadAndInstall(
        versionInfo: AppVersionInfo,
        onProgress: (progress: Float) -> Unit
    ): Result<Unit> {
        downloadAndInstallCalled = true
        lastInstalledVersion = versionInfo
        onProgress(0.5f)
        onProgress(1.0f)
        return downloadResult
    }
}

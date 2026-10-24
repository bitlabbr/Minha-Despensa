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

import com.bitlabbr.minhadespensa.core.domain.model.AppVersionInfo
import com.bitlabbr.minhadespensa.core.domain.repository.AppUpdateRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CheckForAppUpdateUseCaseTest {

    private class FakeAppUpdateRepository(
        private val supported: Boolean = true,
        private val mockUpdate: AppVersionInfo? = null
    ) : AppUpdateRepository {
        override fun isSelfUpdateSupported(): Boolean = supported
        override fun getCurrentVersionName(): String = "2.0.0"
        override fun getCurrentVersionCode(): Long = 2000L
        override suspend fun checkForUpdates(): Result<AppVersionInfo?> = Result.success(mockUpdate)
        override suspend fun downloadAndInstall(
            versionInfo: AppVersionInfo,
            onProgress: (progress: Float) -> Unit
        ): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun `invoke should return null when self update is not supported`() = runTest {
        val fakeRepo = FakeAppUpdateRepository(supported = false)
        val useCase = CheckForAppUpdateUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `invoke should return version info when supported and update exists`() = runTest {
        val versionInfo = AppVersionInfo(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 2100L,
            minRequiredVersion = 2000L,
            releaseNotes = "Melhorias",
            downloadUrl = "/download",
            sha256Checksum = "sha",
            fileSizeInBytes = 1000L,
            releasedAt = 1000L,
            isUpdateAvailable = true
        )
        val fakeRepo = FakeAppUpdateRepository(supported = true, mockUpdate = versionInfo)
        val useCase = CheckForAppUpdateUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(versionInfo, result.getOrNull())
    }
}

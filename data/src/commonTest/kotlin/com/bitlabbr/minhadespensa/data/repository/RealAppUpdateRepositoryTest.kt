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

package com.bitlabbr.minhadespensa.data.repository

import com.bitlabbr.minhadespensa.core.domain.model.AppVersionInfo
import com.bitlabbr.minhadespensa.data.remote.AppUpdateRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.AppVersionResponseDto
import com.bitlabbr.minhadespensa.data.util.AppPackageInstaller
import com.bitlabbr.minhadespensa.data.util.AppPlatformInfoProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RealAppUpdateRepositoryTest {

    private class FakeRemoteDataSource(
        var remoteDto: AppVersionResponseDto? = null,
        var shouldFail: Boolean = false,
    ) : AppUpdateRemoteDataSource {
        override suspend fun getLatestVersion(platform: String): Result<AppVersionResponseDto> {
            if (shouldFail) return Result.failure(RuntimeException("Network error"))
            return Result.success(remoteDto ?: throw RuntimeException("Not found"))
        }

        override suspend fun downloadApk(
            downloadUrl: String,
            onProgress: (progress: Float) -> Unit,
            onChunkReceived: suspend (bytes: ByteArray) -> Unit,
        ): Result<Unit> {
            onProgress(0.5f)
            onChunkReceived("apk-bytes".toByteArray())
            onProgress(1.0f)
            return Result.success(Unit)
        }
    }

    private class FakePlatformInfoProvider(
        var supported: Boolean = true,
        var code: Long = 2000L,
        var name: String = "2.0.0",
        var platform: String = "android",
    ) : AppPlatformInfoProvider {
        override fun isSelfUpdateSupported(): Boolean = supported
        override fun getPlatformName(): String = platform
        override fun getVersionName(): String = name
        override fun getVersionCode(): Long = code
    }

    private class FakePackageInstaller : AppPackageInstaller {
        var openedFileName: String? = null
        override suspend fun openApkFile(
            bytesProvider: suspend (onChunk: suspend (ByteArray) -> Unit) -> Unit,
            fileName: String,
        ): Result<Unit> {
            openedFileName = fileName
            bytesProvider {}
            return Result.success(Unit)
        }
    }

    @Test
    fun `checkForUpdates should return null when self update is not supported`() = runTest {
        val platformProvider = FakePlatformInfoProvider(supported = false)
        val repository = RealAppUpdateRepository(
            remoteDataSource = FakeRemoteDataSource(),
            platformInfoProvider = platformProvider,
            packageInstaller = FakePackageInstaller(),
        )

        val result = repository.checkForUpdates()
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `checkForUpdates should identify update when remote code is higher`() = runTest {
        val platformProvider = FakePlatformInfoProvider(code = 2000L)
        val remoteDto = AppVersionResponseDto(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 2100L,
            minRequiredVersion = 2000L,
            releaseNotes = "Notas da versão",
            downloadUrl = "/download",
        )
        val repository = RealAppUpdateRepository(
            remoteDataSource = FakeRemoteDataSource(remoteDto = remoteDto),
            platformInfoProvider = platformProvider,
            packageInstaller = FakePackageInstaller(),
        )

        val result = repository.checkForUpdates()
        assertTrue(result.isSuccess)
        val updateInfo = result.getOrNull()
        assertTrue(updateInfo != null)
        assertTrue(updateInfo.isUpdateAvailable)
        assertEquals("2.1.0", updateInfo.versionName)
        assertEquals(2100L, updateInfo.versionCode)
    }

    @Test
    fun `checkForUpdates should mark isUpdateAvailable false when remote code is equal or lower`() = runTest {
        val platformProvider = FakePlatformInfoProvider(code = 2100L)
        val remoteDto = AppVersionResponseDto(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 2100L,
            minRequiredVersion = 2000L,
            releaseNotes = "Mesma versão",
            downloadUrl = "/download",
        )
        val repository = RealAppUpdateRepository(
            remoteDataSource = FakeRemoteDataSource(remoteDto = remoteDto),
            platformInfoProvider = platformProvider,
            packageInstaller = FakePackageInstaller(),
        )

        val result = repository.checkForUpdates()
        assertTrue(result.isSuccess)
        val updateInfo = result.getOrNull()
        assertTrue(updateInfo != null)
        assertFalse(updateInfo.isUpdateAvailable)
    }

    @Test
    fun `downloadAndInstall should stream apk and invoke package installer`() = runTest {
        val installer = FakePackageInstaller()
        val repository = RealAppUpdateRepository(
            remoteDataSource = FakeRemoteDataSource(),
            platformInfoProvider = FakePlatformInfoProvider(supported = true),
            packageInstaller = installer,
        )

        val versionInfo = AppVersionInfo(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 2100L,
            minRequiredVersion = 2000L,
            releaseNotes = "Notas",
            downloadUrl = "/download",
            sha256Checksum = "sha",
            fileSizeInBytes = 1000L,
            releasedAt = 1000L,
            isUpdateAvailable = true,
        )

        var lastProgress = 0f
        val result = repository.downloadAndInstall(versionInfo) { progress ->
            lastProgress = progress
        }

        assertTrue(result.isSuccess)
        assertEquals(1.0f, lastProgress)
        assertEquals("minha-despensa-2.1.0.apk", installer.openedFileName)
    }
}

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
import com.bitlabbr.minhadespensa.core.domain.repository.AppUpdateRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.data.remote.AppUpdateRemoteDataSource
import com.bitlabbr.minhadespensa.data.util.AppPackageInstaller
import com.bitlabbr.minhadespensa.data.util.AppPlatformInfoProvider

class RealAppUpdateRepository(
    private val remoteDataSource: AppUpdateRemoteDataSource,
    private val platformInfoProvider: AppPlatformInfoProvider,
    private val packageInstaller: AppPackageInstaller,
    private val logger: AppLogger? = null,
) : AppUpdateRepository {

    private val tag = "AppUpdate"

    override fun isSelfUpdateSupported(): Boolean = platformInfoProvider.isSelfUpdateSupported()

    override fun getCurrentVersionName(): String = platformInfoProvider.getVersionName()

    override fun getCurrentVersionCode(): Long = platformInfoProvider.getVersionCode()

    override suspend fun checkForUpdates(): Result<AppVersionInfo?> = runCatching {
        if (!isSelfUpdateSupported()) {
            logger?.d(tag, "Self-update not supported on this platform: ${platformInfoProvider.getPlatformName()}")
            return@runCatching null
        }

        val platform = platformInfoProvider.getPlatformName().lowercase()
        logger?.i(tag, "Checking for updates [platform=$platform, currentVersion=${getCurrentVersionName()}]")

        val remoteDto = remoteDataSource.getLatestVersion(platform).getOrThrow()
        val currentCode = getCurrentVersionCode()
        val isAvailable = remoteDto.versionCode > currentCode

        logger?.i(
            tag,
            "Update check completed: remoteCode=${remoteDto.versionCode}, currentCode=$currentCode, isAvailable=$isAvailable"
        )

        AppVersionInfo(
            platform = remoteDto.platform,
            versionName = remoteDto.versionName,
            versionCode = remoteDto.versionCode,
            minRequiredVersion = remoteDto.minRequiredVersion,
            releaseNotes = remoteDto.releaseNotes,
            downloadUrl = remoteDto.downloadUrl,
            sha256Checksum = remoteDto.sha256Checksum,
            fileSizeInBytes = remoteDto.fileSizeInBytes,
            releasedAt = remoteDto.releasedAt,
            isUpdateAvailable = isAvailable,
        )
    }

    override suspend fun downloadAndInstall(
        versionInfo: AppVersionInfo,
        onProgress: (progress: Float) -> Unit,
    ): Result<Unit> = runCatching {
        if (!isSelfUpdateSupported()) {
            throw IllegalStateException("Self-update not supported on platform: ${platformInfoProvider.getPlatformName()}")
        }

        logger?.i(tag, "Starting download and install for version: ${versionInfo.versionName} (${versionInfo.versionCode})")

        val fileName = "minha-despensa-${versionInfo.versionName}.apk"
        packageInstaller.openApkFile(
            bytesProvider = { onChunk ->
                remoteDataSource.downloadApk(
                    downloadUrl = versionInfo.downloadUrl,
                    onProgress = onProgress,
                    onChunkReceived = onChunk,
                ).getOrThrow()
            },
            fileName = fileName,
        ).getOrThrow()

        logger?.i(tag, "Installation intent triggered successfully for $fileName")
    }
}

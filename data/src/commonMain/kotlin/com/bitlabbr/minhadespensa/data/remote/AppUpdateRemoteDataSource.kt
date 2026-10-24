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

package com.bitlabbr.minhadespensa.data.remote

import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.data.remote.dto.ApiErrorResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.ApiResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.AppVersionResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

interface AppUpdateRemoteDataSource {
    suspend fun getLatestVersion(platform: String = "android"): Result<AppVersionResponseDto>
    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (progress: Float) -> Unit,
        onChunkReceived: suspend (bytes: ByteArray) -> Unit
    ): Result<Unit>
}

class KtorAppUpdateRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
    private val logger: AppLogger? = null,
) : AppUpdateRemoteDataSource {

    private val tag = "AppUpdateRemote"

    override suspend fun getLatestVersion(platform: String): Result<AppVersionResponseDto> = runCatching {
        val url = "${config.baseUrl}/api/v1/app/version/latest?platform=$platform"
        logger?.d(tag, "--> GET $url")

        val response = httpClient.get(url)
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Check for update failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- GET $url FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }

        val envelope = response.body<ApiResponseDto<AppVersionResponseDto>>()
        val versionDto = envelope.data ?: throw RuntimeException(envelope.message ?: "Versão não encontrada")
        logger?.i(tag, "<-- GET $url SUCCESS: version=${versionDto.versionName} (${versionDto.versionCode})")
        versionDto
    }

    override suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (progress: Float) -> Unit,
        onChunkReceived: suspend (bytes: ByteArray) -> Unit
    ): Result<Unit> = runCatching {
        val fullUrl = if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
            downloadUrl
        } else {
            "${config.baseUrl}$downloadUrl"
        }
        logger?.i(tag, "--> Starting APK download from: $fullUrl")

        val response = httpClient.get(fullUrl) {
            onDownload { bytesSentTotal, contentLength ->
                if (contentLength != null && contentLength > 0L) {
                    val progress = (bytesSentTotal.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                    onProgress(progress)
                }
            }
        }

        if (!response.status.isSuccess()) {
            val errorMsg = "Download APK failed: HTTP ${response.status.value}"
            logger?.w(tag, "<-- $errorMsg")
            throw RuntimeException(errorMsg)
        }

        val channel: ByteReadChannel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(8192)
            while (!packet.exhausted()) {
                val bytes = packet.readByteArray()
                onChunkReceived(bytes)
            }
        }
        onProgress(1.0f)
        logger?.i(tag, "<-- APK download stream finished successfully")
    }

    private suspend fun extractErrorMessage(response: HttpResponse, fallback: String): String {
        return runCatching {
            val errorDto = response.body<ApiErrorResponseDto>()
            errorDto.message ?: fallback
        }.getOrDefault(fallback)
    }
}

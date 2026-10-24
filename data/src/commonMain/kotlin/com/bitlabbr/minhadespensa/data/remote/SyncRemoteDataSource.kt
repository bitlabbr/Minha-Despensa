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
import com.bitlabbr.minhadespensa.core.domain.util.LogSanitizer
import com.bitlabbr.minhadespensa.data.remote.dto.ApiErrorResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.ApiResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPullResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

interface SyncRemoteDataSource {
    suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto>
    suspend fun pull(householdId: String, sinceVersion: Long, limit: Int = 100): Result<SyncPullResponseDto>
    suspend fun uploadProductMedia(householdId: String, productId: String, bytes: ByteArray): Result<String>
    suspend fun downloadProductMedia(householdId: String, productId: String): Result<ByteArray?>
}

class KtorSyncRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
    private val authTokenProvider: AuthTokenProvider = AuthTokenProvider { null },
    private val logger: AppLogger? = null,
) : SyncRemoteDataSource {

    private val tag = "SyncRemote"

    override suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(
            tag,
            "--> PUSH /api/v1/sync/push [household=$householdId, token=${LogSanitizer.maskToken(token)}, " +
                "categories=${payload.categories.size}, products=${payload.products.size}, " +
                "pantry=${payload.pantryItems.size}, shopping=${payload.shoppingLists.size}]",
        )

        val response = httpClient.post("${config.baseUrl}/api/v1/sync/push") {
            contentType(ContentType.Application.Json)
            header("X-Household-Id", householdId)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(payload)
        }

        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Sync push failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- PUSH FAILED: status=${response.status.value}, message=$errorMsg")
            throw RuntimeException(errorMsg)
        }

        val envelope = response.body<ApiResponseDto<SyncPushResponseDto>>()
        val result = envelope.data ?: throw RuntimeException(envelope.message ?: "Sync push failed: empty data")
        logger?.i(
            tag,
            "<-- PUSH SUCCESS: serverVersion=${result.serverVersion}, processed=${result.processedCount}, " +
                "remappedProducts=${result.productIdMappings.size}",
        )
        result
    }

    override suspend fun pull(
        householdId: String,
        sinceVersion: Long,
        limit: Int,
    ): Result<SyncPullResponseDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(
            tag,
            "--> PULL /api/v1/sync/pull [household=$householdId, sinceVersion=$sinceVersion, " +
                "token=${LogSanitizer.maskToken(token)}]",
        )

        val response = httpClient.get("${config.baseUrl}/api/v1/sync/pull") {
            header("X-Household-Id", householdId)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parameter("sinceVersion", sinceVersion)
            parameter("since", sinceVersion)
            parameter("limit", limit)
        }

        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Sync pull failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- PULL FAILED: status=${response.status.value}, message=$errorMsg")
            throw RuntimeException(errorMsg)
        }

        val envelope = response.body<ApiResponseDto<SyncPullResponseDto>>()
        val result = envelope.data ?: throw RuntimeException(envelope.message ?: "Sync pull failed: empty data")
        val entities = result.effectiveEntities
        logger?.i(
            tag,
            "<-- PULL SUCCESS: serverVersion=${result.currentVersion}, products=${entities.products.size}, " +
                "categories=${entities.categories.size}, pantry=${entities.pantryItems.size}, " +
                "lists=${entities.shoppingLists.size}",
        )
        result
    }

    override suspend fun uploadProductMedia(
        householdId: String,
        productId: String,
        bytes: ByteArray,
    ): Result<String> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> PUT /api/v1/media/products/$productId [household=$householdId, bytes=${bytes.size}]")
        val response = httpClient.put("${config.baseUrl}/api/v1/media/products/$productId") {
            contentType(ContentType.parse("image/webp"))
            header("X-Household-Id", householdId)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(bytes)
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Upload media failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- PUT MEDIA FAILED: status=${response.status.value}, message=$errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<Map<String, String>>>()
        val hash = envelope.data?.get("sha256Hash") ?: ""
        logger?.i(tag, "<-- PUT MEDIA SUCCESS: product=$productId, hash=$hash")
        hash
    }

    override suspend fun downloadProductMedia(
        householdId: String,
        productId: String,
    ): Result<ByteArray?> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> GET /api/v1/media/products/$productId [household=$householdId]")
        val response = httpClient.get("${config.baseUrl}/api/v1/media/products/$productId") {
            header("X-Household-Id", householdId)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (response.status == HttpStatusCode.NotFound) {
            logger?.d(tag, "<-- GET MEDIA: 404 Not Found for product $productId")
            return@runCatching null
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Download media failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- GET MEDIA FAILED: status=${response.status.value}, message=$errorMsg")
            throw RuntimeException(errorMsg)
        }
        val bytes = response.body<ByteArray>()
        logger?.i(tag, "<-- GET MEDIA SUCCESS: product=$productId, bytes=${bytes.size}")
        bytes
    }

    private suspend fun extractErrorMessage(response: HttpResponse, fallback: String): String {
        return runCatching {
            val errorDto = response.body<ApiErrorResponseDto>()
            errorDto.message ?: fallback
        }.getOrDefault(fallback)
    }
}

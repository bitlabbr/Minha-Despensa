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

import com.bitlabbr.minhadespensa.data.remote.dto.SyncPullResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

interface SyncRemoteDataSource {
    suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto>
    suspend fun pull(householdId: String, sinceVersion: Long, limit: Int = 100): Result<SyncPullResponseDto>
}

class KtorSyncRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
) : SyncRemoteDataSource {

    override suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/sync/push") {
            contentType(ContentType.Application.Json)
            header("X-Household-Id", householdId)
            setBody(payload)
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException("Sync push failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun pull(
        householdId: String,
        sinceVersion: Long,
        limit: Int,
    ): Result<SyncPullResponseDto> = runCatching {
        val response = httpClient.get("${config.baseUrl}/api/v1/sync/pull") {
            header("X-Household-Id", householdId)
            parameter("since", sinceVersion)
            parameter("limit", limit)
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException("Sync pull failed: HTTP ${response.status.value}")
        }
        response.body()
    }
}

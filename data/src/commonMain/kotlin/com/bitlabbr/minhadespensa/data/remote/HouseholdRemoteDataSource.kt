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
import com.bitlabbr.minhadespensa.data.remote.dto.CreateHouseholdRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import com.bitlabbr.minhadespensa.data.remote.dto.InviteCodeResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.JoinHouseholdRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

interface HouseholdRemoteDataSource {
    suspend fun listHouseholds(): Result<List<HouseholdDto>>
    suspend fun getHousehold(id: String): Result<HouseholdDto>
    suspend fun createHousehold(name: String): Result<HouseholdDto>
    suspend fun updateHousehold(id: String, name: String): Result<HouseholdDto>
    suspend fun joinHousehold(inviteCode: String): Result<HouseholdDto>
    suspend fun leaveHousehold(id: String): Result<Unit>
    suspend fun regenerateInviteCode(id: String): Result<String>
    suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit>
}

class KtorHouseholdRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
    private val authTokenProvider: AuthTokenProvider = AuthTokenProvider { null },
    private val logger: AppLogger? = null,
) : HouseholdRemoteDataSource {

    private val tag = "HouseholdRemote"

    override suspend fun listHouseholds(): Result<List<HouseholdDto>> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> GET /api/v1/households [token=${LogSanitizer.maskToken(token)}]")
        val response = httpClient.get("${config.baseUrl}/api/v1/households") {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "List households failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- GET /api/v1/households FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<List<HouseholdDto>>>()
        val list = envelope.data ?: emptyList()
        logger?.i(tag, "<-- GET /api/v1/households SUCCESS: count=${list.size}")
        list
    }

    override suspend fun getHousehold(id: String): Result<HouseholdDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> GET /api/v1/households/$id")
        val response = httpClient.get("${config.baseUrl}/api/v1/households/$id") {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Get household failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- GET /api/v1/households/$id FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<HouseholdDto>>()
        val household = envelope.data ?: throw RuntimeException(envelope.message ?: "Household not found")
        logger?.i(tag, "<-- GET /api/v1/households/$id SUCCESS: name=${household.name}")
        household
    }

    override suspend fun createHousehold(name: String): Result<HouseholdDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> POST /api/v1/households [name=$name]")
        val response = httpClient.post("${config.baseUrl}/api/v1/households") {
            contentType(ContentType.Application.Json)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(CreateHouseholdRequestDto(name = name))
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Create household failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- POST /api/v1/households FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<HouseholdDto>>()
        val created = envelope.data ?: throw RuntimeException(envelope.message ?: "Create household failed: empty data")
        logger?.i(tag, "<-- POST /api/v1/households SUCCESS: id=${created.id}")
        created
    }

    override suspend fun updateHousehold(id: String, name: String): Result<HouseholdDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> PUT /api/v1/households/$id [name=$name]")
        val response = httpClient.put("${config.baseUrl}/api/v1/households/$id") {
            contentType(ContentType.Application.Json)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(CreateHouseholdRequestDto(name = name))
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Update household failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- PUT /api/v1/households/$id FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<HouseholdDto>>()
        val updated = envelope.data ?: throw RuntimeException(envelope.message ?: "Update household failed: empty data")
        logger?.i(tag, "<-- PUT /api/v1/households/$id SUCCESS")
        updated
    }

    override suspend fun joinHousehold(inviteCode: String): Result<HouseholdDto> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> POST /api/v1/households/join [code=${LogSanitizer.maskInviteCode(inviteCode)}]")
        val response = httpClient.post("${config.baseUrl}/api/v1/households/join") {
            contentType(ContentType.Application.Json)
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(JoinHouseholdRequestDto(inviteCode = inviteCode))
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Join household failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- POST /api/v1/households/join FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<HouseholdDto>>()
        val joined = envelope.data ?: throw RuntimeException(envelope.message ?: "Join household failed: empty data")
        logger?.i(tag, "<-- POST /api/v1/households/join SUCCESS: id=${joined.id}")
        joined
    }

    override suspend fun leaveHousehold(id: String): Result<Unit> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> DELETE /api/v1/households/$id/leave")
        val response = httpClient.delete("${config.baseUrl}/api/v1/households/$id/leave") {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Leave household failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- DELETE /api/v1/households/$id/leave FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        logger?.i(tag, "<-- DELETE /api/v1/households/$id/leave SUCCESS")
    }

    override suspend fun regenerateInviteCode(id: String): Result<String> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> POST /api/v1/households/$id/regenerate-invite-code")
        val response = httpClient.post("${config.baseUrl}/api/v1/households/$id/regenerate-invite-code") {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Regenerate invite code failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- POST /api/v1/households/$id/regenerate-invite-code FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        val envelope = response.body<ApiResponseDto<InviteCodeResponseDto>>()
        val code = envelope.data?.inviteCode ?: throw RuntimeException(envelope.message ?: "Regenerate invite code failed: empty data")
        logger?.i(tag, "<-- POST /api/v1/households/$id/regenerate-invite-code SUCCESS")
        code
    }

    override suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit> = runCatching {
        val token = authTokenProvider.getAccessToken()
        logger?.d(tag, "--> DELETE /api/v1/households/$householdId/members/$memberUserId")
        val response = httpClient.delete("${config.baseUrl}/api/v1/households/$householdId/members/$memberUserId") {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Remove member failed: HTTP ${response.status.value}")
            logger?.w(tag, "<-- DELETE /api/v1/households/$householdId/members/$memberUserId FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }
        logger?.i(tag, "<-- DELETE /api/v1/households/$householdId/members/$memberUserId SUCCESS")
    }

    private suspend fun extractErrorMessage(response: HttpResponse, fallback: String): String {
        return runCatching {
            val errorDto = response.body<ApiErrorResponseDto>()
            errorDto.message ?: fallback
        }.getOrDefault(fallback)
    }
}

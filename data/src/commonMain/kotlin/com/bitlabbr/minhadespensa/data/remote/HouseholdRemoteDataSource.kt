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

import com.bitlabbr.minhadespensa.data.remote.dto.CreateHouseholdRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import com.bitlabbr.minhadespensa.data.remote.dto.InviteCodeResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.JoinHouseholdRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
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
) : HouseholdRemoteDataSource {

    override suspend fun listHouseholds(): Result<List<HouseholdDto>> = runCatching {
        val response = httpClient.get("${config.baseUrl}/api/v1/households")
        if (!response.status.isSuccess()) {
            throw RuntimeException("List households failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun getHousehold(id: String): Result<HouseholdDto> = runCatching {
        val response = httpClient.get("${config.baseUrl}/api/v1/households/$id")
        if (!response.status.isSuccess()) {
            throw RuntimeException("Get household failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun createHousehold(name: String): Result<HouseholdDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/households") {
            contentType(ContentType.Application.Json)
            setBody(CreateHouseholdRequestDto(name = name))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException("Create household failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun updateHousehold(id: String, name: String): Result<HouseholdDto> = runCatching {
        val response = httpClient.put("${config.baseUrl}/api/v1/households/$id") {
            contentType(ContentType.Application.Json)
            setBody(CreateHouseholdRequestDto(name = name))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException("Update household failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun joinHousehold(inviteCode: String): Result<HouseholdDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/households/join") {
            contentType(ContentType.Application.Json)
            setBody(JoinHouseholdRequestDto(inviteCode = inviteCode))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException("Join household failed: HTTP ${response.status.value}")
        }
        response.body()
    }

    override suspend fun leaveHousehold(id: String): Result<Unit> = runCatching {
        val response = httpClient.delete("${config.baseUrl}/api/v1/households/$id/leave")
        if (!response.status.isSuccess()) {
            throw RuntimeException("Leave household failed: HTTP ${response.status.value}")
        }
    }

    override suspend fun regenerateInviteCode(id: String): Result<String> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/households/$id/regenerate-invite-code")
        if (!response.status.isSuccess()) {
            throw RuntimeException("Regenerate invite code failed: HTTP ${response.status.value}")
        }
        val body = response.body<InviteCodeResponseDto>()
        body.inviteCode
    }

    override suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit> = runCatching {
        val response = httpClient.delete("${config.baseUrl}/api/v1/households/$householdId/members/$memberUserId")
        if (!response.status.isSuccess()) {
            throw RuntimeException("Remove member failed: HTTP ${response.status.value}")
        }
    }
}

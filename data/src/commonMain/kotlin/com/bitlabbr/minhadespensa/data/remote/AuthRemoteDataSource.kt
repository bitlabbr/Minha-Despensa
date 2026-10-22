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

import com.bitlabbr.minhadespensa.data.remote.dto.ApiErrorResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.ApiResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.AuthResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.LoginRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.RefreshTokenRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.RegisterRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.TokenRefreshResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

interface AuthRemoteDataSource {
    suspend fun register(name: String, email: String, password: String): Result<AuthResponseDto>
    suspend fun login(email: String, password: String): Result<AuthResponseDto>
    suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponseDto>
}

class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
) : AuthRemoteDataSource {

    override suspend fun register(name: String, email: String, password: String): Result<AuthResponseDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(name = name, email = email, password = password))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException(extractErrorMessage(response, "Registration failed: HTTP ${response.status.value}"))
        }
        val envelope = response.body<ApiResponseDto<AuthResponseDto>>()
        envelope.data ?: throw RuntimeException(envelope.message ?: "Registration failed: empty data")
    }

    override suspend fun login(email: String, password: String): Result<AuthResponseDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException(extractErrorMessage(response, "Login failed: HTTP ${response.status.value}"))
        }
        val envelope = response.body<ApiResponseDto<AuthResponseDto>>()
        envelope.data ?: throw RuntimeException(envelope.message ?: "Login failed: empty data")
    }

    override suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponseDto> = runCatching {
        val response = httpClient.post("${config.baseUrl}/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException(extractErrorMessage(response, "Token refresh failed: HTTP ${response.status.value}"))
        }
        val envelope = response.body<ApiResponseDto<AuthResponseDto>>()
        val data = envelope.data ?: throw RuntimeException(envelope.message ?: "Token refresh failed: empty data")
        TokenRefreshResponseDto(
            accessToken = data.accessToken,
            refreshToken = data.refreshToken,
            tokenType = data.tokenType,
            expiresIn = data.expiresIn,
        )
    }

    private suspend fun extractErrorMessage(response: HttpResponse, fallback: String): String {
        return runCatching {
            val errorDto = response.body<ApiErrorResponseDto>()
            errorDto.message ?: fallback
        }.getOrDefault(fallback)
    }
}

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

package com.bitlabbr.minhadespensa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val defaultHouseholdId: String? = null,
)

@Serializable
data class HouseholdMemberDto(
    val userId: String,
    val name: String,
    val email: String,
    val role: String = "MEMBER",
    val joinedAt: Long = 0L,
)

@Serializable
data class HouseholdDto(
    val id: String,
    val name: String,
    val inviteCode: String,
    val role: String = "MEMBER",
    val members: List<HouseholdMemberDto> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    @SerialName("expiresInSeconds")
    val expiresIn: Long = 86400L,
    val user: UserDto,
    @SerialName("activeHousehold")
    val household: HouseholdDto? = null,
)

@Serializable
data class TokenRefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)

@Serializable
data class InviteCodeResponseDto(
    val inviteCode: String,
)

@Serializable
data class CreateHouseholdRequestDto(
    val name: String,
)

@Serializable
data class JoinHouseholdRequestDto(
    val inviteCode: String,
)

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

package com.bitlabbr.minhadespensa.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class HouseholdRole {
    OWNER,
    MEMBER
}

@Serializable
data class Household(
    val id: String,
    val name: String,
    val inviteCode: String,
    val role: HouseholdRole = HouseholdRole.MEMBER
)

@Serializable
data class HouseholdMember(
    val userId: String,
    val name: String,
    val email: String,
    val role: HouseholdRole,
    val joinedAt: Long
)

@Serializable
data class HouseholdDetail(
    val id: String,
    val name: String,
    val inviteCode: String,
    val role: HouseholdRole,
    val members: List<HouseholdMember>,
    val createdAt: Long,
    val updatedAt: Long
)

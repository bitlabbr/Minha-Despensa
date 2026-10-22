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

package com.bitlabbr.minhadespensa.uisystem.features.account.model

import androidx.compose.runtime.Immutable
import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.model.SyncState

enum class AuthTab {
    LOGIN,
    REGISTER
}

@Immutable
data class HouseholdAccountUiState(
    val session: AuthSession? = null,
    val activeHousehold: Household? = null,
    val activeHouseholdDetail: HouseholdDetail? = null,
    val userHouseholds: List<HouseholdDetail> = emptyList(),
    val syncState: SyncState = SyncState.IDLE,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedTab: AuthTab = AuthTab.LOGIN,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val isJoinDialogOpen: Boolean = false,
    val joinInviteCode: String = "",
    val isCreateDialogOpen: Boolean = false,
    val createHouseholdName: String = "",
    val isLogoutConfirmDialogOpen: Boolean = false,
    val isSwitchingHousehold: Boolean = false,
) {
    val isAuthenticated: Boolean
        get() = session != null

    val isOwnerOfActiveHousehold: Boolean
        get() = activeHousehold?.role == HouseholdRole.OWNER
}

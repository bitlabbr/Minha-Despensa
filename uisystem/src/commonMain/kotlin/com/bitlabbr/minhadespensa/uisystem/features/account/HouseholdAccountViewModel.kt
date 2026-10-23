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

package com.bitlabbr.minhadespensa.uisystem.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdDetail
import com.bitlabbr.minhadespensa.core.domain.model.SyncState
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.JoinHouseholdByInviteCodeUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LoginUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LogoutUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ManageHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.RegisterUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SwitchActiveHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.SyncCoordinator
import com.bitlabbr.minhadespensa.uisystem.features.account.model.AuthTab
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.account_household_create_success
import minhadespensa.uisystem.generated.resources.account_household_join_success
import minhadespensa.uisystem.generated.resources.account_household_switched_success
import minhadespensa.uisystem.generated.resources.account_login_success
import minhadespensa.uisystem.generated.resources.account_logout_success
import minhadespensa.uisystem.generated.resources.account_register_success
import minhadespensa.uisystem.generated.resources.sync_status_error

class HouseholdAccountViewModel(
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val syncCoordinator: SyncCoordinator,
    private val syncRepository: SyncRepository,
    private val manageHouseholdUseCase: ManageHouseholdUseCase,
    private val joinHouseholdByInviteCodeUseCase: JoinHouseholdByInviteCodeUseCase,
    private val switchActiveHouseholdUseCase: SwitchActiveHouseholdUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val notificationManager: AppNotificationManager,
    private val logger: AppLogger? = null,
) : ViewModel() {

    companion object {
        private const val TAG = "HouseholdAccountViewModel"
    }

    private enum class PendingAuthAction {
        LOGIN,
        REGISTER,
    }

    private val _selectedTab = MutableStateFlow(AuthTab.LOGIN)
    private val _loginEmail = MutableStateFlow("")
    private val _loginPassword = MutableStateFlow("")
    private val _registerName = MutableStateFlow("")
    private val _registerEmail = MutableStateFlow("")
    private val _registerPassword = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _isJoinDialogOpen = MutableStateFlow(false)
    private val _joinInviteCode = MutableStateFlow("")
    private val _isCreateDialogOpen = MutableStateFlow(false)
    private val _createHouseholdName = MutableStateFlow("")
    private val _isLogoutConfirmDialogOpen = MutableStateFlow(false)
    private val _isSwitchingHousehold = MutableStateFlow(false)
    private val _isMergeDecisionDialogOpen = MutableStateFlow(false)
    private var pendingAuthAction: PendingAuthAction? = null
    private val _userHouseholds = MutableStateFlow<List<HouseholdDetail>>(emptyList())

    init {
        refreshHouseholds()
    }

    private data class FormState(
        val selectedTab: AuthTab,
        val loginEmail: String,
        val loginPassword: String,
        val registerName: String,
        val registerEmail: String,
        val registerPassword: String,
        val isLoading: Boolean,
        val isJoinDialogOpen: Boolean,
        val joinInviteCode: String,
        val isCreateDialogOpen: Boolean,
        val createHouseholdName: String,
        val isLogoutConfirmDialogOpen: Boolean,
        val isSwitchingHousehold: Boolean,
        val isMergeDecisionDialogOpen: Boolean,
    )

    private val formStateFlow = combine(
        combine(
            _selectedTab,
            _loginEmail,
            _loginPassword,
            _registerName,
            _registerEmail,
        ) { tab, lEmail, lPass, rName, rEmail ->
            Tuple5(tab, lEmail, lPass, rName, rEmail)
        },
        combine(
            _registerPassword,
            _isLoading,
            _isJoinDialogOpen,
            _joinInviteCode,
            _isCreateDialogOpen,
        ) { rPass, loading, joinOpen, joinCode, createOpen ->
            Tuple5(rPass, loading, joinOpen, joinCode, createOpen)
        },
        combine(
            _createHouseholdName,
            _isLogoutConfirmDialogOpen,
            _isSwitchingHousehold,
            _isMergeDecisionDialogOpen,
        ) { createName, logoutOpen, switching, mergeOpen ->
            Tuple4(createName, logoutOpen, switching, mergeOpen)
        }
    ) { part1, part2, part3 ->
        FormState(
            selectedTab = part1.v1,
            loginEmail = part1.v2,
            loginPassword = part1.v3,
            registerName = part1.v4,
            registerEmail = part1.v5,
            registerPassword = part2.v1,
            isLoading = part2.v2,
            isJoinDialogOpen = part2.v3,
            joinInviteCode = part2.v4,
            isCreateDialogOpen = part2.v5,
            createHouseholdName = part3.v1,
            isLogoutConfirmDialogOpen = part3.v2,
            isSwitchingHousehold = part3.v3,
            isMergeDecisionDialogOpen = part3.v4,
        )
    }

    val uiState: StateFlow<HouseholdAccountUiState> = combine(
        authRepository.observeCurrentSession(),
        householdRepository.observeActiveHousehold(),
        _userHouseholds,
        syncCoordinator.status,
        formStateFlow,
    ) { session, activeHousehold, userHouseholds, syncState, form ->
        val activeDetail = userHouseholds.find { it.id == activeHousehold?.id }
        HouseholdAccountUiState(
            session = session,
            activeHousehold = activeHousehold,
            activeHouseholdDetail = activeDetail,
            userHouseholds = userHouseholds,
            syncState = syncState,
            isLoading = form.isLoading,
            isSyncing = syncState == SyncState.SYNCING,
            selectedTab = form.selectedTab,
            loginEmail = form.loginEmail,
            loginPassword = form.loginPassword,
            registerName = form.registerName,
            registerEmail = form.registerEmail,
            registerPassword = form.registerPassword,
            isJoinDialogOpen = form.isJoinDialogOpen,
            joinInviteCode = form.joinInviteCode,
            isCreateDialogOpen = form.isCreateDialogOpen,
            createHouseholdName = form.createHouseholdName,
            isLogoutConfirmDialogOpen = form.isLogoutConfirmDialogOpen,
            isSwitchingHousehold = form.isSwitchingHousehold,
            isMergeDecisionDialogOpen = form.isMergeDecisionDialogOpen,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HouseholdAccountUiState(isLoading = true)
    )

    fun onTabSelected(tab: AuthTab) {
        _selectedTab.value = tab
    }

    fun onLoginEmailChange(email: String) {
        _loginEmail.value = email
    }

    fun onLoginPasswordChange(password: String) {
        _loginPassword.value = password
    }

    fun onRegisterNameChange(name: String) {
        _registerName.value = name
    }

    fun onRegisterEmailChange(email: String) {
        _registerEmail.value = email
    }

    fun onRegisterPasswordChange(password: String) {
        _registerPassword.value = password
    }

    fun login() {
        viewModelScope.launch {
            if (syncRepository.hasLocalData()) {
                pendingAuthAction = PendingAuthAction.LOGIN
                _isMergeDecisionDialogOpen.value = true
            } else {
                executeLogin(keepLocalData = true)
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            if (syncRepository.hasLocalData()) {
                pendingAuthAction = PendingAuthAction.REGISTER
                _isMergeDecisionDialogOpen.value = true
            } else {
                executeRegister(keepLocalData = true)
            }
        }
    }

    fun onConfirmMergeData() {
        val action = pendingAuthAction ?: PendingAuthAction.LOGIN
        dismissMergeDecisionDialog()
        when (action) {
            PendingAuthAction.LOGIN -> executeLogin(keepLocalData = true)
            PendingAuthAction.REGISTER -> executeRegister(keepLocalData = true)
        }
    }

    fun onConfirmDiscardLocalData() {
        val action = pendingAuthAction ?: PendingAuthAction.LOGIN
        dismissMergeDecisionDialog()
        when (action) {
            PendingAuthAction.LOGIN -> executeLogin(keepLocalData = false)
            PendingAuthAction.REGISTER -> executeRegister(keepLocalData = false)
        }
    }

    fun dismissMergeDecisionDialog() {
        _isMergeDecisionDialogOpen.value = false
        pendingAuthAction = null
    }

    private fun executeLogin(keepLocalData: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            loginUserUseCase(
                email = _loginEmail.value,
                password = _loginPassword.value,
                keepLocalData = keepLocalData,
            ).fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_login_success))
                    _loginEmail.value = ""
                    _loginPassword.value = ""
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Login failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Authentication failed"))
                }
            )
            _isLoading.value = false
        }
    }

    private fun executeRegister(keepLocalData: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            registerUserUseCase(
                name = _registerName.value,
                email = _registerEmail.value,
                password = _registerPassword.value,
                keepLocalData = keepLocalData,
            ).fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_register_success))
                    _registerName.value = ""
                    _registerEmail.value = ""
                    _registerPassword.value = ""
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Registration failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Registration failed"))
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            logoutUserUseCase().fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_logout_success))
                    _userHouseholds.value = emptyList()
                    dismissLogoutDialog()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Logout failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Logout failed"))
                }
            )
            _isLoading.value = false
        }
    }

    fun openJoinDialog() {
        _joinInviteCode.value = ""
        _isJoinDialogOpen.value = true
    }

    fun dismissJoinDialog() {
        _isJoinDialogOpen.value = false
        _joinInviteCode.value = ""
    }

    fun onJoinInviteCodeChange(code: String) {
        _joinInviteCode.value = code.uppercase().take(CoreConstants.Household.INVITE_CODE_LENGTH)
    }

    fun joinHousehold() {
        val code = _joinInviteCode.value
        viewModelScope.launch {
            _isLoading.value = true
            joinHouseholdByInviteCodeUseCase(code).fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_household_join_success))
                    dismissJoinDialog()
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Join household failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Failed to join household"))
                }
            )
            _isLoading.value = false
        }
    }

    fun openCreateDialog() {
        _createHouseholdName.value = ""
        _isCreateDialogOpen.value = true
    }

    fun dismissCreateDialog() {
        _isCreateDialogOpen.value = false
        _createHouseholdName.value = ""
    }

    fun onCreateHouseholdNameChange(name: String) {
        _createHouseholdName.value = name.take(80)
    }

    fun createHousehold() {
        val name = _createHouseholdName.value
        viewModelScope.launch {
            _isLoading.value = true
            manageHouseholdUseCase.createHousehold(name).fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_household_create_success))
                    dismissCreateDialog()
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Create household failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Failed to create household"))
                }
            )
            _isLoading.value = false
        }
    }

    fun switchActiveHousehold(householdId: String) {
        viewModelScope.launch {
            _isSwitchingHousehold.value = true
            switchActiveHouseholdUseCase(householdId).fold(
                onSuccess = {
                    notificationManager.showSuccess(UiText.Resource(Res.string.account_household_switched_success))
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Switch household failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Failed to switch household"))
                }
            )
            _isSwitchingHousehold.value = false
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncCoordinator.triggerSync().fold(
                onSuccess = {
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Manual sync failed: ${error.message}", error)
                    notificationManager.showError(UiText.Resource(Res.string.sync_status_error))
                }
            )
        }
    }

    fun regenerateInviteCode() {
        viewModelScope.launch {
            val active = householdRepository.getActiveHousehold() ?: return@launch
            householdRepository.regenerateInviteCode(active.id).fold(
                onSuccess = {
                    refreshHouseholds()
                },
                onFailure = { error ->
                    logger?.w(TAG, "Regenerate invite code failed: ${error.message}", error)
                    notificationManager.showError(UiText.DynamicString(error.message ?: "Failed to regenerate invite code"))
                }
            )
        }
    }

    fun openLogoutDialog() {
        _isLogoutConfirmDialogOpen.value = true
    }

    fun dismissLogoutDialog() {
        _isLogoutConfirmDialogOpen.value = false
    }

    fun refreshHouseholds() {
        viewModelScope.launch {
            householdRepository.getUserHouseholds().onSuccess { list ->
                _userHouseholds.value = list
            }
        }
    }

    private data class Tuple4<A, B, C, D>(
        val v1: A,
        val v2: B,
        val v3: C,
        val v4: D,
    )

    private data class Tuple5<A, B, C, D, E>(
        val v1: A,
        val v2: B,
        val v3: C,
        val v4: D,
        val v5: E,
    )
}

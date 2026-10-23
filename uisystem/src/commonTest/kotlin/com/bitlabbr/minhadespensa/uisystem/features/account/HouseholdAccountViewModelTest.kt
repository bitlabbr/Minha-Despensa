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

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.bitlabbr.minhadespensa.core.domain.model.AppVersionInfo
import com.bitlabbr.minhadespensa.core.domain.model.SyncState
import com.bitlabbr.minhadespensa.core.domain.usecase.JoinHouseholdByInviteCodeUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LoginUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LogoutUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ManageHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.RegisterUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SwitchActiveHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SyncHouseholdDataUseCase
import com.bitlabbr.minhadespensa.core.domain.util.SyncCoordinator
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppLogger
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAppUpdateRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeAuthRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeHouseholdRepository
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeSyncRepository
import com.bitlabbr.minhadespensa.uisystem.features.account.model.AuthTab
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdAccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var householdRepository: FakeHouseholdRepository
    private lateinit var syncRepository: FakeSyncRepository
    private lateinit var appUpdateRepository: FakeAppUpdateRepository
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var logger: FakeAppLogger

    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var loginUserUseCase: LoginUserUseCase
    private lateinit var registerUserUseCase: RegisterUserUseCase
    private lateinit var logoutUserUseCase: LogoutUserUseCase
    private lateinit var manageHouseholdUseCase: ManageHouseholdUseCase
    private lateinit var joinHouseholdByInviteCodeUseCase: JoinHouseholdByInviteCodeUseCase
    private lateinit var switchActiveHouseholdUseCase: SwitchActiveHouseholdUseCase

    private lateinit var viewModel: HouseholdAccountViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = FakeAuthRepository()
        householdRepository = FakeHouseholdRepository()
        syncRepository = FakeSyncRepository()
        appUpdateRepository = FakeAppUpdateRepository()
        notificationManager = AppNotificationManager()
        logger = FakeAppLogger()

        val syncHouseholdDataUseCase = SyncHouseholdDataUseCase(householdRepository, syncRepository)
        syncCoordinator = SyncCoordinator(syncHouseholdDataUseCase, syncRepository)
        loginUserUseCase = LoginUserUseCase(authRepository, syncRepository)
        registerUserUseCase = RegisterUserUseCase(authRepository, syncRepository)
        logoutUserUseCase = LogoutUserUseCase(authRepository)
        manageHouseholdUseCase = ManageHouseholdUseCase(householdRepository, syncRepository)
        joinHouseholdByInviteCodeUseCase = JoinHouseholdByInviteCodeUseCase(householdRepository, syncRepository)
        switchActiveHouseholdUseCase = SwitchActiveHouseholdUseCase(householdRepository, syncRepository)

        viewModel = HouseholdAccountViewModel(
            authRepository = authRepository,
            householdRepository = householdRepository,
            syncCoordinator = syncCoordinator,
            syncRepository = syncRepository,
            manageHouseholdUseCase = manageHouseholdUseCase,
            joinHouseholdByInviteCodeUseCase = joinHouseholdByInviteCodeUseCase,
            switchActiveHouseholdUseCase = switchActiveHouseholdUseCase,
            loginUserUseCase = loginUserUseCase,
            registerUserUseCase = registerUserUseCase,
            logoutUserUseCase = logoutUserUseCase,
            appUpdateRepository = appUpdateRepository,
            notificationManager = notificationManager,
            logger = logger,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun ReceiveTurbine<HouseholdAccountUiState>.awaitLoadedState(): HouseholdAccountUiState {
        var item = awaitItem()
        while (item.isLoading) {
            item = awaitItem()
        }
        return item
    }

    @Test
    fun `initial state reflects unauthenticated and idle`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isAuthenticated)
            assertEquals(AuthTab.LOGIN, state.selectedTab)
            assertEquals(SyncState.IDLE, state.syncState)
            assertNull(state.session)
            assertNull(state.activeHousehold)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switching tabs updates selected tab`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitLoadedState()
            viewModel.onTabSelected(AuthTab.REGISTER)
            val state = awaitItem()
            assertEquals(AuthTab.REGISTER, state.selectedTab)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updating input fields updates corresponding state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitLoadedState()
            viewModel.onLoginEmailChange("test@example.com")
            val s1 = awaitItem()
            assertEquals("test@example.com", s1.loginEmail)

            viewModel.onLoginPasswordChange("password123")
            val s2 = awaitItem()
            assertEquals("password123", s2.loginPassword)

            viewModel.onRegisterNameChange("Willian Santos")
            val s3 = awaitItem()
            assertEquals("Willian Santos", s3.registerName)

            viewModel.onRegisterEmailChange("new@example.com")
            val s4 = awaitItem()
            assertEquals("new@example.com", s4.registerEmail)

            viewModel.onRegisterPasswordChange("secret123")
            val s5 = awaitItem()
            assertEquals("secret123", s5.registerPassword)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login success updates session and emits success notification`() = runTest(testDispatcher) {
        notificationManager.notifications.test {
            viewModel.onLoginEmailChange("user@example.com")
            viewModel.onLoginPasswordChange("password123")
            viewModel.login()

            val notification = awaitItem()
            assertNotNull(notification)

            viewModel.uiState.test {
                val state = awaitLoadedState()
                assertTrue(state.isAuthenticated)
                assertEquals("user@example.com", state.session?.user?.email)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure with invalid email emits error notification`() = runTest(testDispatcher) {
        notificationManager.notifications.test {
            viewModel.onLoginEmailChange("invalid-email")
            viewModel.onLoginPasswordChange("pass")
            viewModel.login()

            val notification = awaitItem()
            assertEquals(com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType.ERROR, notification.type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register success updates session and initial sync`() = runTest(testDispatcher) {
        notificationManager.notifications.test {
            viewModel.onRegisterNameChange("Willian Santos")
            viewModel.onRegisterEmailChange("willian@example.com")
            viewModel.onRegisterPasswordChange("securepassword123")
            viewModel.register()

            val notification = awaitItem()
            assertNotNull(notification)

            viewModel.uiState.test {
                val state = awaitLoadedState()
                assertTrue(state.isAuthenticated)
                assertEquals("Willian Santos", state.session?.user?.name)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout success clears session and user households`() = runTest(testDispatcher) {
        authRepository.login("user@example.com", "pass123")
        advanceUntilIdle()

        notificationManager.notifications.test {
            viewModel.openLogoutDialog()
            viewModel.logout()

            val notification = awaitItem()
            assertNotNull(notification)

            viewModel.uiState.test {
                val state = awaitLoadedState()
                assertFalse(state.isAuthenticated)
                assertFalse(state.isLogoutConfirmDialogOpen)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `open and dismiss join dialog updates state`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitLoadedState()
            viewModel.openJoinDialog()
            viewModel.onJoinInviteCodeChange("abcdefgh")
            val s1 = awaitItem()
            val s2 = if (s1.joinInviteCode.isEmpty()) awaitItem() else s1
            assertTrue(s2.isJoinDialogOpen)
            assertEquals("ABCDEFGH", s2.joinInviteCode)

            viewModel.onJoinInviteCodeChange("casa-xga92ts7")
            val s3 = awaitItem()
            assertEquals("CASA-XGA92TS7", s3.joinInviteCode)

            viewModel.dismissJoinDialog()
            val s4 = awaitItem()
            assertFalse(s4.isJoinDialogOpen)
            assertEquals("", s4.joinInviteCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `join household by invite code success updates active household`() = runTest(testDispatcher) {
        viewModel.openJoinDialog()
        viewModel.onJoinInviteCodeChange("ABCDEFGH")
        viewModel.joinHousehold()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isJoinDialogOpen)
            assertEquals("hh-joined", state.activeHousehold?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `create household success updates households list`() = runTest(testDispatcher) {
        viewModel.openCreateDialog()
        viewModel.onCreateHouseholdNameChange("Casa de Campo")
        viewModel.createHousehold()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isCreateDialogOpen)
            assertEquals("Casa de Campo", state.activeHousehold?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switch active household updates active household`() = runTest(testDispatcher) {
        val h1 = householdRepository.createHousehold("Casa Principal").getOrThrow()
        val h2 = householdRepository.createHousehold("Casa de Praia").getOrThrow()
        householdRepository.switchActiveHousehold(h1.id)
        viewModel.refreshHouseholds()
        advanceUntilIdle()

        viewModel.switchActiveHousehold(h2.id)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertEquals("Casa de Praia", state.activeHousehold?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `triggerSync transitions syncState and invokes sync`() = runTest(testDispatcher) {
        val h = householdRepository.createHousehold("Minha Casa").getOrThrow()
        householdRepository.switchActiveHousehold(h.id)
        advanceUntilIdle()

        viewModel.triggerSync()
        advanceUntilIdle()

        assertEquals(1, syncRepository.syncCallCount)
        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertEquals(SyncState.SUCCESS, state.syncState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `regenerateInviteCode updates invite code on active household`() = runTest(testDispatcher) {
        val h = householdRepository.createHousehold("Minha Casa").getOrThrow()
        householdRepository.switchActiveHousehold(h.id)
        viewModel.refreshHouseholds()
        advanceUntilIdle()

        viewModel.regenerateInviteCode()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            val activeDetail = state.userHouseholds.find { it.id == h.id }
            assertEquals("NEWCODE1", activeDetail?.inviteCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with local data opens merge decision dialog and confirm merge keeps local data`() = runTest(testDispatcher) {
        syncRepository.hasLocalDataResult = true
        viewModel.onLoginEmailChange("merge@example.com")
        viewModel.onLoginPasswordChange("pass12345")

        viewModel.login()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertTrue(state.isMergeDecisionDialogOpen)
            assertFalse(state.isAuthenticated)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onConfirmMergeData()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isMergeDecisionDialogOpen)
            assertTrue(state.isAuthenticated)
            assertEquals("merge@example.com", state.session?.user?.email)
            assertTrue(syncRepository.enqueueAllLocalEntitiesCalled)
            assertFalse(syncRepository.clearLocalDataCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with local data and confirm discard wipes local data before sync`() = runTest(testDispatcher) {
        syncRepository.hasLocalDataResult = true
        viewModel.onLoginEmailChange("discard@example.com")
        viewModel.onLoginPasswordChange("pass12345")

        viewModel.login()
        advanceUntilIdle()

        viewModel.onConfirmDiscardLocalData()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isMergeDecisionDialogOpen)
            assertTrue(state.isAuthenticated)
            assertEquals("discard@example.com", state.session?.user?.email)
            assertTrue(syncRepository.clearLocalDataCalled)
            assertFalse(syncRepository.enqueueAllLocalEntitiesCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismiss merge decision dialog cancels login attempt`() = runTest(testDispatcher) {
        syncRepository.hasLocalDataResult = true
        viewModel.onLoginEmailChange("cancel@example.com")
        viewModel.onLoginPasswordChange("pass12345")

        viewModel.login()
        advanceUntilIdle()

        viewModel.dismissMergeDecisionDialog()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isMergeDecisionDialogOpen)
            assertFalse(state.isAuthenticated)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkForUpdates when update is available updates availableUpdate state`() = runTest(testDispatcher) {
        val newVersion = AppVersionInfo(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 210L,
            minRequiredVersion = 100L,
            releaseNotes = "Novas funcionalidades e correções.",
            downloadUrl = "https://despensa.domus.arpa/api/v1/app/download/latest",
            sha256Checksum = "fakehash",
            fileSizeInBytes = 25_000_000L,
            releasedAt = 1700000000000L,
            isUpdateAvailable = true,
        )
        appUpdateRepository.updateResult = Result.success(newVersion)

        viewModel.checkForUpdates()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isCheckingUpdate)
            assertEquals(newVersion, state.availableUpdate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadAndInstallUpdate calls repository and updates downloadProgress`() = runTest(testDispatcher) {
        val newVersion = AppVersionInfo(
            platform = "android",
            versionName = "2.1.0",
            versionCode = 210L,
            minRequiredVersion = 100L,
            releaseNotes = "Correções",
            downloadUrl = "https://despensa.domus.arpa/api/v1/app/download/latest",
            sha256Checksum = "fakehash",
            fileSizeInBytes = 25_000_000L,
            releasedAt = 1700000000000L,
            isUpdateAvailable = true,
        )
        appUpdateRepository.updateResult = Result.success(newVersion)

        viewModel.checkForUpdates()
        advanceUntilIdle()

        viewModel.downloadAndInstallUpdate()
        advanceUntilIdle()

        assertTrue(appUpdateRepository.downloadAndInstallCalled)
        assertEquals(newVersion, appUpdateRepository.lastInstalledVersion)

        viewModel.uiState.test {
            val state = awaitLoadedState()
            assertFalse(state.isDownloadingUpdate)
            assertEquals(1f, state.downloadProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

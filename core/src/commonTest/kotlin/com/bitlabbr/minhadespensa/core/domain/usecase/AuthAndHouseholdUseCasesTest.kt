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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.fakes.FakeAuthRepository
import com.bitlabbr.minhadespensa.core.domain.fakes.FakeHouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.fakes.FakeSyncRepository
import com.bitlabbr.minhadespensa.core.domain.model.WorkspaceSwitchCheckResult
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthAndHouseholdUseCasesTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var householdRepository: FakeHouseholdRepository
    private lateinit var syncRepository: FakeSyncRepository

    private lateinit var registerUserUseCase: RegisterUserUseCase
    private lateinit var loginUserUseCase: LoginUserUseCase
    private lateinit var logoutUserUseCase: LogoutUserUseCase
    private lateinit var joinHouseholdByInviteCodeUseCase: JoinHouseholdByInviteCodeUseCase
    private lateinit var switchActiveHouseholdUseCase: SwitchActiveHouseholdUseCase
    private lateinit var manageHouseholdUseCase: ManageHouseholdUseCase
    private lateinit var syncHouseholdDataUseCase: SyncHouseholdDataUseCase

    @BeforeTest
    fun setup() {
        authRepository = FakeAuthRepository()
        householdRepository = FakeHouseholdRepository()
        syncRepository = FakeSyncRepository()

        registerUserUseCase = RegisterUserUseCase(authRepository, syncRepository)
        loginUserUseCase = LoginUserUseCase(authRepository, syncRepository)
        logoutUserUseCase = LogoutUserUseCase(authRepository)
        joinHouseholdByInviteCodeUseCase = JoinHouseholdByInviteCodeUseCase(householdRepository, syncRepository)
        switchActiveHouseholdUseCase = SwitchActiveHouseholdUseCase(householdRepository, syncRepository)
        manageHouseholdUseCase = ManageHouseholdUseCase(householdRepository, syncRepository)
        syncHouseholdDataUseCase = SyncHouseholdDataUseCase(householdRepository, syncRepository)
    }

    @Test
    fun registerUserUseCase_validatesBlankName() = runTest {
        val result = registerUserUseCase(
            name = "   ",
            email = "user@test.com",
            password = "password123",
        )
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_USER_NAME_BLANK, result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUserUseCase_validatesNameExceeds80Chars() = runTest {
        val longName = "A".repeat(81)
        val result = registerUserUseCase(
            name = longName,
            email = "user@test.com",
            password = "password123",
        )
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_USER_NAME_TOO_LONG, result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUserUseCase_validatesInvalidEmail() = runTest {
        val result = registerUserUseCase(
            name = "Valid Name",
            email = "invalid-email",
            password = "password123",
        )
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_EMAIL_INVALID, result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUserUseCase_validatesPasswordTooShort() = runTest {
        val result = registerUserUseCase(
            name = "Valid Name",
            email = "user@test.com",
            password = "123",
        )
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_PASSWORD_TOO_SHORT, result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUserUseCase_successfulRegistration_enqueuesLocalEntitiesForDataLossPrevention() = runTest {
        val result = registerUserUseCase(
            name = "Valid Name",
            email = "USER@test.com",
            password = "password123",
            keepLocalData = true,
        )
        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals("user@test.com", session.user.email)
        assertTrue(syncRepository.enqueueAllLocalEntitiesCalled)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun registerUserUseCase_withDiscardLocalData_clearsLocalDataAndSyncs() = runTest {
        val result = registerUserUseCase(
            name = "Valid Name",
            email = "discard@test.com",
            password = "password123",
            keepLocalData = false,
        )
        assertTrue(result.isSuccess)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertFalse(syncRepository.enqueueAllLocalEntitiesCalled)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun loginUserUseCase_validatesEmailAndPassword() = runTest {
        val emailFail = loginUserUseCase("invalid", "password123")
        assertTrue(emailFail.isFailure)

        val passFail = loginUserUseCase("valid@test.com", "   ")
        assertTrue(passFail.isFailure)
    }

    @Test
    fun loginUserUseCase_successfulLogin_enqueuesLocalEntitiesAndSyncs() = runTest {
        val result = loginUserUseCase("valid@test.com", "password123", keepLocalData = true)
        assertTrue(result.isSuccess)
        assertTrue(syncRepository.enqueueAllLocalEntitiesCalled)
        assertFalse(syncRepository.clearLocalDataCalled)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun loginUserUseCase_withDiscardLocalData_clearsLocalDataAndSyncs() = runTest {
        val result = loginUserUseCase("valid@test.com", "password123", keepLocalData = false)
        assertTrue(result.isSuccess)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertFalse(syncRepository.enqueueAllLocalEntitiesCalled)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun logoutUserUseCase_clearsSession() = runTest {
        loginUserUseCase("valid@test.com", "password123")
        assertNotNull(authRepository.getCurrentSession())

        val logoutResult = logoutUserUseCase()
        assertTrue(logoutResult.isSuccess)
        assertEquals(null, authRepository.getCurrentSession())
    }

    @Test
    fun joinHouseholdByInviteCodeUseCase_validatesLength() = runTest {
        val result = joinHouseholdByInviteCodeUseCase("12345")
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_INVITE_CODE_INVALID, result.exceptionOrNull()?.message)
    }

    @Test
    fun joinHouseholdByInviteCodeUseCase_successfulJoin_withOrWithoutPrefix() = runTest {
        // Without prefix (8 chars) -> normalized to CASA-ABCDEFGH
        val resultWithoutPrefix = joinHouseholdByInviteCodeUseCase("ABCDEFGH")
        assertTrue(resultWithoutPrefix.isSuccess)
        assertEquals("CASA-ABCDEFGH", resultWithoutPrefix.getOrNull()?.inviteCode)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertEquals(1, syncRepository.syncCallCount)

        // Reset fake counters
        syncRepository.clearLocalDataCalled = false
        syncRepository.syncCallCount = 0

        // With prefix (13 chars)
        val resultWithPrefix = joinHouseholdByInviteCodeUseCase("CASA-ABCDEFGH")
        assertTrue(resultWithPrefix.isSuccess)
        assertEquals("CASA-ABCDEFGH", resultWithPrefix.getOrNull()?.inviteCode)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertEquals(1, syncRepository.syncCallCount)

        // Lowercase (13 chars)
        val resultLowercase = joinHouseholdByInviteCodeUseCase("casa-abcdefgh")
        assertTrue(resultLowercase.isSuccess)
        assertEquals("CASA-ABCDEFGH", resultLowercase.getOrNull()?.inviteCode)
    }

    @Test
    fun switchActiveHouseholdUseCase_handlesUnsyncedChangesGracefully() = runTest {
        val created = manageHouseholdUseCase.createHousehold("Minha Casa").getOrThrow()
        syncRepository.hasUnsyncedChangesOnSwitch = true
        syncRepository.pendingCountFlow.value = 3

        val checkResult = switchActiveHouseholdUseCase(created.id, forceDiscardPending = false).getOrThrow()
        assertIs<WorkspaceSwitchCheckResult.HasUnsyncedChanges>(checkResult)
        assertEquals(3, checkResult.count)

        // Force switch clears data and executes switch
        val forceResult = switchActiveHouseholdUseCase(created.id, forceDiscardPending = true).getOrThrow()
        assertIs<WorkspaceSwitchCheckResult.Ready>(forceResult)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertEquals(created.id, householdRepository.getActiveHousehold()?.id)
    }

    @Test
    fun manageHouseholdUseCase_createValidatesConstraints() = runTest {
        val blankResult = manageHouseholdUseCase.createHousehold("   ")
        assertTrue(blankResult.isFailure)

        val longResult = manageHouseholdUseCase.createHousehold("A".repeat(81))
        assertTrue(longResult.isFailure)

        val successResult = manageHouseholdUseCase.createHousehold("Casa Principal")
        assertTrue(successResult.isSuccess)
        val created = successResult.getOrThrow()
        assertEquals("Casa Principal", created.name)
        assertTrue(syncRepository.clearLocalDataCalled)
        assertFalse(syncRepository.enqueueAllLocalEntitiesCalled)
        assertTrue(syncRepository.syncCallCount > 0)
        assertEquals(created.id, householdRepository.getActiveHousehold()?.id)
    }

    @Test
    fun manageHouseholdUseCase_regenerateCodeAndManageMembers() = runTest {
        val household = manageHouseholdUseCase.createHousehold("Casa da Familia").getOrThrow()

        val newCodeResult = manageHouseholdUseCase.regenerateInviteCode(household.id)
        assertTrue(newCodeResult.isSuccess)
        assertEquals("NEWCODE1", newCodeResult.getOrNull())

        val removeMemberResult = manageHouseholdUseCase.removeMember(household.id, "user-123")
        assertTrue(removeMemberResult.isSuccess)

        val updateResult = manageHouseholdUseCase.updateHousehold(household.id, "Nova Casa")
        assertTrue(updateResult.isSuccess)
        assertEquals("Nova Casa", updateResult.getOrNull()?.name)

        val leaveResult = manageHouseholdUseCase.leaveHousehold(household.id)
        assertTrue(leaveResult.isSuccess)
    }

    @Test
    fun syncHouseholdDataUseCase_failsWhenNoActiveHousehold() = runTest {
        val result = syncHouseholdDataUseCase()
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_NO_ACTIVE_HOUSEHOLD, result.exceptionOrNull()?.message)
    }

    @Test
    fun syncHouseholdDataUseCase_succeedsWithActiveHousehold() = runTest {
        val household = manageHouseholdUseCase.createHousehold("Casa").getOrThrow()
        val result = syncHouseholdDataUseCase()
        assertTrue(result.isSuccess)
        assertEquals(2, syncRepository.syncCallCount)
    }
}

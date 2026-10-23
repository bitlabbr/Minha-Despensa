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

package com.bitlabbr.minhadespensa.core.domain.util

import com.bitlabbr.minhadespensa.core.domain.fakes.FakeHouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.fakes.FakeSyncRepository
import com.bitlabbr.minhadespensa.core.domain.model.SyncState
import com.bitlabbr.minhadespensa.core.domain.usecase.ManageHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SyncHouseholdDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCoordinatorTest {

    private lateinit var householdRepository: FakeHouseholdRepository
    private lateinit var syncRepository: FakeSyncRepository
    private lateinit var syncHouseholdDataUseCase: SyncHouseholdDataUseCase
    private lateinit var coordinator: SyncCoordinator

    @BeforeTest
    fun setup() {
        householdRepository = FakeHouseholdRepository()
        syncRepository = FakeSyncRepository()
        val manageHouseholdUseCase = ManageHouseholdUseCase(householdRepository, syncRepository)
        syncHouseholdDataUseCase = SyncHouseholdDataUseCase(householdRepository, syncRepository)
        coordinator = SyncCoordinator(
            syncHouseholdDataUseCase = syncHouseholdDataUseCase,
            syncRepository = syncRepository,
        )
    }

    @Test
    fun triggerSync_updatesStatusToSuccessOnCleanExecution() = runTest {
        val household = householdRepository.createHousehold("Casa Principal").getOrThrow()
        householdRepository.switchActiveHousehold(household.id)

        assertEquals(SyncState.IDLE, coordinator.status.value)

        val result = coordinator.triggerSync()
        assertTrue(result.isSuccess)

        assertEquals(SyncState.SUCCESS, coordinator.status.value)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun triggerSync_updatesStatusToErrorOnFailure() = runTest {
        val result = coordinator.triggerSync()
        assertTrue(result.isFailure)

        assertEquals(SyncState.ERROR, coordinator.status.value)
    }

    @Test
    fun autoSync_debouncesPendingMutationsTrigger() = runTest {
        val household = householdRepository.createHousehold("Casa Principal").getOrThrow()
        householdRepository.switchActiveHousehold(household.id)

        coordinator.startAutoSync(backgroundScope, debounceMillis = 500L)

        syncRepository.pendingCountFlow.value = 1
        runCurrent()
        assertEquals(0, syncRepository.syncCallCount)

        advanceTimeBy(300L)
        runCurrent()
        assertEquals(0, syncRepository.syncCallCount)

        advanceTimeBy(300L)
        runCurrent()
        assertEquals(1, syncRepository.syncCallCount)
    }
}

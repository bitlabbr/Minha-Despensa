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

package com.bitlabbr.minhadespensa.core.domain.fakes

import com.bitlabbr.minhadespensa.core.domain.model.WorkspaceSwitchCheckResult
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSyncRepository : SyncRepository {
    val pendingCountFlow = MutableStateFlow(0)
    var syncCallCount = 0
    var enqueueAllLocalEntitiesCalled = false
    var clearLocalDataCalled = false
    var hasLocalDataResult: Boolean = false
    var shouldFail: Boolean = false
    var failureException: Throwable = RuntimeException("Sync failed")
    var hasUnsyncedChangesOnSwitch: Boolean = false

    override fun observePendingChangesCount(): Flow<Int> = pendingCountFlow.asStateFlow()

    override suspend fun getPendingChangesCount(): Int = pendingCountFlow.value

    override suspend fun sync(): Result<Unit> {
        syncCallCount++
        if (shouldFail) return Result.failure(failureException)
        pendingCountFlow.value = 0
        return Result.success(Unit)
    }

    override suspend fun enqueueAllLocalEntitiesForInitialSync(): Result<Int> {
        enqueueAllLocalEntitiesCalled = true
        return Result.success(5)
    }

    override suspend fun flushOutboxBeforeSwitch(forceDiscard: Boolean): WorkspaceSwitchCheckResult {
        if (hasUnsyncedChangesOnSwitch && !forceDiscard) {
            return WorkspaceSwitchCheckResult.HasUnsyncedChanges(pendingCountFlow.value.coerceAtLeast(1))
        }
        return WorkspaceSwitchCheckResult.Ready
    }

    override suspend fun clearLocalDataForHouseholdSwitch() {
        clearLocalDataCalled = true
        pendingCountFlow.value = 0
    }

    override suspend fun hasLocalData(): Boolean = hasLocalDataResult
}

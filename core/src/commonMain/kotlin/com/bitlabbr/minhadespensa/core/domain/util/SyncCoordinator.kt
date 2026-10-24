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

import com.bitlabbr.minhadespensa.core.domain.model.SyncState
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.SyncHouseholdDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex

class SyncCoordinator(
    private val syncHouseholdDataUseCase: SyncHouseholdDataUseCase,
    private val syncRepository: SyncRepository,
    private val logger: AppLogger? = null,
) {
    private val tag = "SyncCoordinator"
    private val syncMutex = Mutex()
    private val _status = MutableStateFlow<SyncState>(SyncState.IDLE)
    val status: StateFlow<SyncState> = _status.asStateFlow()

    @OptIn(FlowPreview::class)
    fun startAutoSync(scope: CoroutineScope, debounceMillis: Long = 2000L): Job {
        return syncRepository.observePendingChangesCount()
            .distinctUntilChanged()
            .filter { it > 0 }
            .debounce(debounceMillis)
            .onEach {
                triggerSync()
            }
            .launchIn(scope)
    }

    suspend fun triggerSync(): Result<Unit> {
        if (!syncMutex.tryLock()) {
            logger?.d(tag, "Sync already in progress; skipping concurrent trigger")
            return Result.success(Unit)
        }

        return try {
            _status.value = SyncState.SYNCING
            logger?.i(tag, "Sync cycle triggered by coordinator...")
            syncHouseholdDataUseCase().getOrThrow()
            _status.value = SyncState.SUCCESS
            logger?.i(tag, "Sync cycle completed successfully (status=SUCCESS)")
            Result.success(Unit)
        } catch (e: Throwable) {
            val isAuthExpired = e.message?.contains("401") == true || e.message?.contains("SESSION_EXPIRED") == true
            _status.value = if (isAuthExpired) SyncState.SESSION_EXPIRED else SyncState.ERROR
            logger?.w(tag, "Sync cycle failed: ${e.message} (newStatus=${_status.value})", e)
            Result.failure(e)
        } finally {
            syncMutex.unlock()
        }
    }
}

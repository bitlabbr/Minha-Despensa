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

package com.bitlabbr.minhadespensa.uisystem.fakes

import com.bitlabbr.minhadespensa.core.domain.model.DeviceDiagnostics
import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.repository.SupportRepository

class FakeSupportRepository(
    var submitResult: Result<SupportReceipt> = Result.success(
        SupportReceipt(
            id = "mock-id-123",
            protocol = "BUG-20261025-TEST",
            category = SupportCategory.BUG,
            title = "Teste de bug",
            status = "OPEN",
            hasLogs = true,
            createdAt = 1729876543210L,
        )
    ),
    var shareResult: Result<Unit> = Result.success(Unit),
    var pendingCrash: String? = null,
) : SupportRepository {

    var submitReportCalled = false
    var lastSubmittedReport: SupportReport? = null
    var shareDiagnosticsLogCalled = false
    var clearPendingCrashReportCalled = false

    override suspend fun submitReport(report: SupportReport): Result<SupportReceipt> {
        submitReportCalled = true
        lastSubmittedReport = report
        return submitResult
    }

    override suspend fun getDeviceDiagnostics(includeLogs: Boolean): DeviceDiagnostics {
        return DeviceDiagnostics(
            appVersion = "2.1.0",
            deviceModel = "Pixel 8",
            osVersion = "Android 14",
            userId = "user-1",
            householdId = "hh-1",
            deviceLogs = if (includeLogs) "Fake logs" else null,
        )
    }

    override suspend fun shareDiagnosticsLog(report: SupportReport?): Result<Unit> {
        shareDiagnosticsLogCalled = true
        return shareResult
    }

    override suspend fun getPendingCrashReport(): String? {
        return pendingCrash
    }

    override suspend fun clearPendingCrashReport() {
        clearPendingCrashReportCalled = true
        pendingCrash = null
    }
}

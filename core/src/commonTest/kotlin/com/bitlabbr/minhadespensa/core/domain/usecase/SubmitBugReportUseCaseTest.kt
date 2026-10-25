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

import com.bitlabbr.minhadespensa.core.domain.model.DeviceDiagnostics
import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.repository.SupportRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubmitBugReportUseCaseTest {

    private class FakeSupportRepository : SupportRepository {
        var lastSubmittedReport: SupportReport? = null
        var lastIncludeLogsRequested: Boolean? = null
        var pendingCrash: String? = "Simulated uncaught exception"

        override suspend fun submitReport(report: SupportReport): Result<SupportReceipt> {
            lastSubmittedReport = report
            return Result.success(
                SupportReceipt(
                    id = "report-123",
                    protocol = "BUG-20261025-ABCD",
                    category = report.category,
                    title = report.title,
                    status = "OPEN",
                    hasLogs = report.diagnostics.deviceLogs != null,
                    createdAt = 1729850000000L
                )
            )
        }

        override suspend fun getDeviceDiagnostics(includeLogs: Boolean): DeviceDiagnostics {
            lastIncludeLogsRequested = includeLogs
            return DeviceDiagnostics(
                appVersion = "1.2.0-alpha",
                deviceModel = "Pixel 7 Pro",
                osVersion = "Android 14",
                userId = "user-123",
                householdId = "house-456",
                deviceLogs = if (includeLogs) "2026-10-25 10:00:00 [I] [Minha Despensa] Started" else null
            )
        }

        override suspend fun shareDiagnosticsLog(report: SupportReport?): Result<Unit> = Result.success(Unit)

        override suspend fun getPendingCrashReport(): String? = pendingCrash

        override suspend fun clearPendingCrashReport() {
            pendingCrash = null
        }
    }

    @Test
    fun `invoke should fail when title is shorter than 3 characters`() = runTest {
        val repository = FakeSupportRepository()
        val useCase = SubmitBugReportUseCase(repository)

        val result = useCase(
            category = SupportCategory.BUG,
            title = "ab",
            description = "Valid description here"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("3 caracteres") == true)
        assertNull(repository.lastSubmittedReport)
    }

    @Test
    fun `invoke should fail when description is shorter than 5 characters`() = runTest {
        val repository = FakeSupportRepository()
        val useCase = SubmitBugReportUseCase(repository)

        val result = useCase(
            category = SupportCategory.SUGGESTION,
            title = "Valid title",
            description = "1234"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("5 caracteres") == true)
        assertNull(repository.lastSubmittedReport)
    }

    @Test
    fun `invoke with includeLogs true should collect logs and submit report`() = runTest {
        val repository = FakeSupportRepository()
        val useCase = SubmitBugReportUseCase(repository)

        val result = useCase(
            category = SupportCategory.BUG,
            title = "Erro de sincronizacao",
            description = "Ao clicar em sincronizar recebo erro 500",
            userContact = "  contato@teste.com  ",
            includeLogs = true
        )

        assertTrue(result.isSuccess)
        val receipt = result.getOrNull()!!
        assertEquals("BUG-20261025-ABCD", receipt.protocol)
        assertEquals(SupportCategory.BUG, receipt.category)
        assertTrue(receipt.hasLogs)

        val submitted = repository.lastSubmittedReport!!
        assertEquals("Erro de sincronizacao", submitted.title)
        assertEquals("contato@teste.com", submitted.userContact)
        assertEquals(true, repository.lastIncludeLogsRequested)
        assertTrue(submitted.diagnostics.deviceLogs != null)
    }

    @Test
    fun `invoke with includeLogs false should submit report without logs`() = runTest {
        val repository = FakeSupportRepository()
        val useCase = SubmitBugReportUseCase(repository)

        val result = useCase(
            category = SupportCategory.SUGGESTION,
            title = "Tema escuro personalizado",
            description = "Adicionar opcoes extras de gradiente de vidro",
            includeLogs = false
        )

        assertTrue(result.isSuccess)
        val receipt = result.getOrNull()!!
        assertEquals("BUG-20261025-ABCD", receipt.protocol)
        assertEquals(false, receipt.hasLogs)

        assertEquals(false, repository.lastIncludeLogsRequested)
        assertNull(repository.lastSubmittedReport!!.diagnostics.deviceLogs)
        assertNull(repository.lastSubmittedReport!!.userContact)
    }
}

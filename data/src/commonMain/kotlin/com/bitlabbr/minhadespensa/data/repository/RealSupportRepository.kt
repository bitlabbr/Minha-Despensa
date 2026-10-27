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

package com.bitlabbr.minhadespensa.data.repository

import com.bitlabbr.minhadespensa.core.domain.model.DeviceDiagnostics
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SupportRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.DiagnosticLogRecorder
import com.bitlabbr.minhadespensa.data.remote.SupportRemoteDataSource
import com.bitlabbr.minhadespensa.data.util.SupportPlatformProvider
import kotlinx.coroutines.flow.firstOrNull

class RealSupportRepository(
    private val remoteDataSource: SupportRemoteDataSource,
    private val platformProvider: SupportPlatformProvider,
    private val logRecorder: DiagnosticLogRecorder,
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val logger: AppLogger? = null,
) : SupportRepository {

    private val tag = "Support"

    override suspend fun submitReport(report: SupportReport): Result<SupportReceipt> {
        logger?.i(tag, "[Support:Submit] Submitting report: category=${report.category}, title='${report.title}'")
        return remoteDataSource.submitReport(report)
    }

    override suspend fun getDeviceDiagnostics(includeLogs: Boolean): DeviceDiagnostics {
        val currentUser = authRepository.getCurrentSession()?.user
        val activeHousehold = householdRepository.getActiveHousehold()
        val logs = if (includeLogs) logRecorder.dumpToString() else null

        return DeviceDiagnostics(
            appVersion = platformProvider.getAppVersionName(),
            deviceModel = platformProvider.getDeviceModel(),
            osVersion = platformProvider.getOsVersion(),
            userId = currentUser?.id,
            householdId = activeHousehold?.id,
            deviceLogs = logs
        )
    }

    override suspend fun shareDiagnosticsLog(report: SupportReport?): Result<Unit> {
        val diagnostics = report?.diagnostics ?: getDeviceDiagnostics(includeLogs = true)
        val title = report?.title ?: "Minha Despensa - Diagnóstico"
        val header = """
            ================================================================================
            MINHA DESPENSA - RELATÓRIO DE SUPORTE E DIAGNÓSTICO
            ================================================================================
            Categoria: ${report?.category?.name ?: "DIAGNOSTICS"}
            Título: $title
            Descrição: ${report?.description ?: "N/A"}
            Contato: ${report?.userContact ?: "N/A"}
            Versão do App: ${diagnostics.appVersion}
            Dispositivo: ${diagnostics.deviceModel ?: "N/A"}
            Sistema Operacional: ${diagnostics.osVersion ?: "N/A"}
            Household ID: ${diagnostics.householdId ?: "N/A"}
            User ID: ${diagnostics.userId ?: "N/A"}
            ================================================================================
            HISTÓRICO DE LOGS DO APARELHO
            ================================================================================
        """.trimIndent()
        val body = diagnostics.deviceLogs ?: "(Nenhum log registrado no buffer)"
        val fullContent = "$header\n\n$body"

        logger?.i(tag, "[Support:Share] Invoking native share sheet for diagnostics log (${fullContent.length} chars)")
        return platformProvider.shareLogFile(title = title, content = fullContent)
    }

    override suspend fun getPendingCrashReport(): String? {
        return platformProvider.getPendingCrash()
    }

    override suspend fun clearPendingCrashReport() {
        platformProvider.clearPendingCrash()
    }
}

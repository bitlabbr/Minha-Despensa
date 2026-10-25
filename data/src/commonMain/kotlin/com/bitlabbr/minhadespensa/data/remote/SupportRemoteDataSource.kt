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

package com.bitlabbr.minhadespensa.data.remote

import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.LogSanitizer
import com.bitlabbr.minhadespensa.data.remote.dto.ApiErrorResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.ApiResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.BugReportResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

interface SupportRemoteDataSource {
    suspend fun submitReport(report: SupportReport): Result<SupportReceipt>
}

class KtorSupportRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncServerConfig,
    private val authTokenProvider: AuthTokenProvider = AuthTokenProvider { null },
    private val logger: AppLogger? = null,
) : SupportRemoteDataSource {

    private val tag = "SupportRemote"

    override suspend fun submitReport(report: SupportReport): Result<SupportReceipt> = runCatching {
        val initialToken = authTokenProvider.getAccessToken()
        val url = "${config.baseUrl}/api/v1/support/bug-reports"
        val hasLogs = !report.diagnostics.deviceLogs.isNullOrBlank()

        logger?.d(
            tag,
            "--> POST $url [category=${report.category}, hasLogs=$hasLogs, token=${LogSanitizer.maskToken(initialToken)}]"
        )

        var response = executeMultipartPost(url, report, initialToken)

        // Se falhar com 401 (token expirado), tenta novamente de forma anônima (sem o header Authorization)
        if (response.status == HttpStatusCode.Unauthorized && !initialToken.isNullOrBlank()) {
            logger?.w(tag, "Received 401 Unauthorized with token. Retrying anonymously to ensure delivery...")
            response = executeMultipartPost(url, report, null)
        }

        if (!response.status.isSuccess()) {
            val errorMsg = extractErrorMessage(response, "Falha ao registrar relato de suporte: HTTP ${response.status.value}")
            logger?.w(tag, "<-- POST $url FAILED: $errorMsg")
            throw RuntimeException(errorMsg)
        }

        val envelope = response.body<ApiResponseDto<BugReportResponseDto>>()
        val data = envelope.data ?: throw RuntimeException(envelope.message ?: "Resposta vazia do servidor")

        logger?.i(tag, "<-- POST $url SUCCESS: protocol=${data.protocol}, status=${data.status}")

        SupportReceipt(
            id = data.id,
            protocol = data.protocol,
            category = SupportCategory.fromRaw(data.category) ?: report.category,
            title = data.title,
            status = data.status,
            hasLogs = data.hasLogs,
            createdAt = data.createdAt
        )
    }

    private suspend fun executeMultipartPost(
        url: String,
        report: SupportReport,
        token: String?
    ): HttpResponse {
        return httpClient.submitFormWithBinaryData(
            url = url,
            formData = formData {
                append("category", report.category.name)
                append("title", report.title)
                append("description", report.description)
                append("appVersion", report.diagnostics.appVersion)

                report.diagnostics.deviceModel?.let { append("deviceModel", it) }
                report.diagnostics.osVersion?.let { append("osVersion", it) }
                report.userContact?.let { append("userContact", it) }
                report.diagnostics.householdId?.let { append("householdId", it) }

                val logs = report.diagnostics.deviceLogs
                if (!logs.isNullOrBlank()) {
                    append(
                        key = "logFile",
                        value = logs.encodeToByteArray(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "text/plain; charset=utf-8")
                            append(HttpHeaders.ContentDisposition, "filename=\"device.log\"")
                        }
                    )
                }
            }
        ) {
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            report.diagnostics.householdId?.let {
                header("X-Household-Id", it)
            }
        }
    }

    private suspend fun extractErrorMessage(response: HttpResponse, fallback: String): String {
        return runCatching {
            val errorDto = response.body<ApiErrorResponseDto>()
            errorDto.message ?: fallback
        }.getOrDefault(fallback)
    }
}

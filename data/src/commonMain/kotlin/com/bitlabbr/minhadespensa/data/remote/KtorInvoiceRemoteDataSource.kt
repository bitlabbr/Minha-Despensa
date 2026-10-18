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

import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceParseRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.InvoiceResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorInvoiceRemoteDataSource(
    private val httpClient: HttpClient,
    private val serverConfig: InvoiceServerConfig,
) : InvoiceRemoteDataSource {

    override suspend fun parseInvoice(qrCodeContent: String): InvoiceResponseDto {
        val endpoint = "${serverConfig.baseUrl}/api/v1/invoice/parse"
        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(InvoiceParseRequestDto(qrCodeContent = qrCodeContent))
        }

        if (!response.status.isSuccess()) {
            val serverMessage = runCatching {
                val rawBody = response.body<String>()
                val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(rawBody)
                match?.groupValues?.getOrNull(1)
            }.getOrNull()

            val errorMessage = serverMessage ?: "Servidor retornou erro ${response.status.value}: ${response.status.description}"
            throw IllegalStateException(errorMessage)
        }

        return response.body<InvoiceResponseDto>()
    }
}

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

package com.bitlabbr.minhadespensa.data

import com.bitlabbr.minhadespensa.data.remote.InvoiceServerConfig
import com.bitlabbr.minhadespensa.data.remote.KtorInvoiceRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KtorInvoiceRemoteDataSourceTest {

    @Test
    fun `parseInvoice should post to server and return parsed InvoiceResponseDto`() = runTest {
        val jsonResponse = """
            {
              "accessKey": "262410123456",
              "storeName": "Supermercado Legal",
              "items": [
                {
                  "ean": "7891234567890",
                  "name": "Sabão em Pó 1kg",
                  "quantity": 1.0,
                  "unit": "UN",
                  "unitPriceInCents": 1290,
                  "totalPriceInCents": 1290
                }
              ]
            }
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/invoice/parse", request.url.encodedPath)
            respond(
                content = jsonResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val config = InvoiceServerConfig("http://mock-server:8080")
        val dataSource = KtorInvoiceRemoteDataSource(client, config)

        val result = dataSource.parseInvoice("http://nfce.sefaz.pe.gov.br/consulta?p=123")

        assertEquals("262410123456", result.accessKey)
        assertEquals("Supermercado Legal", result.storeName)
        assertEquals(1, result.items.size)
        assertEquals("Sabão em Pó 1kg", result.items[0].name)
        assertEquals(1290L, result.items[0].unitPriceInCents)
    }

    @Test
    fun `parseInvoice should throw on HTTP error response`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val config = InvoiceServerConfig("http://mock-server:8080")
        val dataSource = KtorInvoiceRemoteDataSource(client, config)

        assertFailsWith<IllegalStateException> {
            dataSource.parseInvoice("http://nfce.sefaz.pe.gov.br/consulta?p=123")
        }
    }
}

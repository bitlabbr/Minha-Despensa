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

import com.bitlabbr.minhadespensa.data.remote.KtorAuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.KtorHouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.KtorSyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncServerConfig
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushRequestDto
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
import kotlin.test.assertTrue

class KtorAuthAndSyncRemoteDataSourceTest {

    private fun createMockClient(handler: (requestUrl: String) -> Pair<HttpStatusCode, String>): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    val (status, content) = handler(request.url.encodedPath)
                    respond(
                        content = content,
                        status = status,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        }
    }

    @Test
    fun registerShouldUnwrapApiResponseEnvelopeAndParseBackendPayload() = runTest {
        val serverJson = """
            {
              "success": true,
              "message": "User registered successfully.",
              "data": {
                "accessToken": "jwt_token_123",
                "refreshToken": "refresh_token_456",
                "tokenType": "Bearer",
                "expiresInSeconds": 86400,
                "user": {
                  "id": "u-1",
                  "name": "Willian Santos",
                  "email": "willian@test.com",
                  "defaultHouseholdId": "hh-1"
                },
                "activeHousehold": {
                  "id": "hh-1",
                  "name": "Minha Casa",
                  "inviteCode": "CASA-1234",
                  "role": "OWNER"
                }
              },
              "timestamp": "2026-10-22T19:34:24.654Z"
            }
        """.trimIndent()

        val client = createMockClient { HttpStatusCode.Created to serverJson }
        val dataSource = KtorAuthRemoteDataSource(client, SyncServerConfig("https://mock.domus.arpa"))

        val result = dataSource.register("Willian Santos", "willian@test.com", "Secret@123")
        assertTrue(result.isSuccess)
        val auth = result.getOrThrow()
        assertEquals("jwt_token_123", auth.accessToken)
        assertEquals("refresh_token_456", auth.refreshToken)
        assertEquals(86400L, auth.expiresIn)
        assertEquals("Willian Santos", auth.user.name)
        assertEquals("hh-1", auth.household?.id)
        assertEquals("OWNER", auth.household?.role)
    }

    @Test
    fun registerShouldExtractBackendErrorMessageOnConflict() = runTest {
        val conflictJson = """
            {
              "timestamp": "2026-10-22T19:35:20.118Z",
              "status": 409,
              "error": "Conflict",
              "message": "Email already registered",
              "path": "/api/v1/auth/register"
            }
        """.trimIndent()

        val client = createMockClient { HttpStatusCode.Conflict to conflictJson }
        val dataSource = KtorAuthRemoteDataSource(client, SyncServerConfig("https://mock.domus.arpa"))

        val result = dataSource.register("Willian Santos", "willian@test.com", "Secret@123")
        assertTrue(result.isFailure)
        assertEquals("Email already registered", result.exceptionOrNull()?.message)
    }

    @Test
    fun listHouseholdsShouldUnwrapApiResponseEnvelope() = runTest {
        val householdsJson = """
            {
              "success": true,
              "message": null,
              "data": [
                {
                  "id": "hh-1",
                  "name": "Casa Central",
                  "inviteCode": "CASA-ABCD",
                  "ownerId": "u-1",
                  "syncVersion": 5,
                  "members": [],
                  "createdAt": 1700000000,
                  "updatedAt": 1700000000
                }
              ],
              "timestamp": "2026-10-22T19:35:20.118Z"
            }
        """.trimIndent()

        val client = createMockClient { HttpStatusCode.OK to householdsJson }
        val dataSource = KtorHouseholdRemoteDataSource(client, SyncServerConfig("https://mock.domus.arpa"))

        val result = dataSource.listHouseholds()
        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("Casa Central", list[0].name)
    }

    @Test
    fun syncPullShouldUnwrapApiResponseAndParseEntities() = runTest {
        val pullJson = """
            {
              "success": true,
              "message": "Pull successful.",
              "data": {
                "serverTimestamp": 1729618000000,
                "serverVersion": 42,
                "hasMore": false,
                "categories": [
                  {
                    "id": "cat-1",
                    "name": "Bebidas",
                    "isDefault": false,
                    "updatedAt": 1729617000000,
                    "isDeleted": false
                  }
                ],
                "products": [
                  {
                    "id": "prod-1",
                    "name": "Suco de Uva",
                    "category": "Bebidas",
                    "measureUnit": "LITER",
                    "netWeight": 1.0,
                    "updatedAt": 1729617000000,
                    "isDeleted": false
                  }
                ]
              },
              "timestamp": "2026-10-22T19:35:20.118Z"
            }
        """.trimIndent()

        val client = createMockClient { HttpStatusCode.OK to pullJson }
        val dataSource = KtorSyncRemoteDataSource(client, SyncServerConfig("https://mock.domus.arpa"))

        val result = dataSource.pull("hh-1", 0L)
        assertTrue(result.isSuccess)
        val pull = result.getOrThrow()
        assertEquals(42L, pull.currentVersion)
        assertEquals(1, pull.effectiveEntities.categories.size)
        assertEquals("Bebidas", pull.effectiveEntities.categories[0].name)
        assertEquals(1, pull.effectiveEntities.products.size)
        assertEquals("Suco de Uva", pull.effectiveEntities.products[0].name)
    }

    @Test
    fun syncPushShouldUnwrapApiResponseEnvelope() = runTest {
        val pushJson = """
            {
              "success": true,
              "message": "Push completed.",
              "data": {
                "serverTimestamp": 1729618000000,
                "serverVersion": 43,
                "totalProcessed": 2,
                "productIdMappings": {
                  "old-p1": "canonical-p1"
                },
                "categoryIdMappings": {}
              },
              "timestamp": "2026-10-22T19:35:20.118Z"
            }
        """.trimIndent()

        val client = createMockClient { HttpStatusCode.OK to pushJson }
        val dataSource = KtorSyncRemoteDataSource(client, SyncServerConfig("https://mock.domus.arpa"))

        val result = dataSource.push("hh-1", SyncPushRequestDto())
        assertTrue(result.isSuccess)
        val push = result.getOrThrow()
        assertEquals(43L, push.serverVersion)
        assertEquals(2, push.processedCount)
        assertEquals("canonical-p1", push.productIdMappings["old-p1"])
    }
}

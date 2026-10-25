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

import com.bitlabbr.minhadespensa.core.domain.model.AuthSession
import com.bitlabbr.minhadespensa.core.domain.model.DeviceDiagnostics
import com.bitlabbr.minhadespensa.core.domain.model.Household
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.model.SupportCategory
import com.bitlabbr.minhadespensa.core.domain.model.SupportReceipt
import com.bitlabbr.minhadespensa.core.domain.model.SupportReport
import com.bitlabbr.minhadespensa.core.domain.model.UserAccount
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.util.DiagnosticLogRecorder
import com.bitlabbr.minhadespensa.core.domain.util.RingBufferLogRecorder
import com.bitlabbr.minhadespensa.data.remote.AuthTokenProvider
import com.bitlabbr.minhadespensa.data.remote.KtorSupportRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SupportRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncServerConfig
import com.bitlabbr.minhadespensa.data.util.SupportPlatformProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RealSupportRepositoryTest {

    private class FakeSupportRemoteDataSource : SupportRemoteDataSource {
        var lastReport: SupportReport? = null
        var shouldFail: Boolean = false

        override suspend fun submitReport(report: SupportReport): Result<SupportReceipt> {
            lastReport = report
            if (shouldFail) return Result.failure(RuntimeException("Network error"))
            return Result.success(
                SupportReceipt(
                    id = "receipt-1",
                    protocol = "BUG-20261025-ABCD",
                    category = report.category,
                    title = report.title,
                    status = "OPEN",
                    hasLogs = report.diagnostics.deviceLogs != null,
                    createdAt = 1729850000000L
                )
            )
        }
    }

    private class FakeSupportPlatformProvider : SupportPlatformProvider {
        var crashContent: String? = "NullPointerException at Example.kt:42"
        var sharedTitle: String? = null
        var sharedContent: String? = null

        override fun getDeviceModel(): String = "Pixel 7 Pro"
        override fun getOsVersion(): String = "Android 14"
        override fun getAppVersionName(): String = "1.2.0-alpha"

        override suspend fun savePendingCrash(stackTrace: String) {
            crashContent = stackTrace
        }

        override suspend fun getPendingCrash(): String? = crashContent

        override suspend fun clearPendingCrash() {
            crashContent = null
        }

        override suspend fun shareLogFile(title: String, content: String): Result<Unit> {
            sharedTitle = title
            sharedContent = content
            return Result.success(Unit)
        }
    }

    private class FakeAuthRepository : AuthRepository {
        var currentSession: AuthSession? = AuthSession(
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token",
            expiresInSeconds = 3600L,
            user = UserAccount(id = "user-abc", name = "Test User", email = "test@example.com"),
            activeHousehold = Household(id = "house-xyz", name = "Minha Casa", inviteCode = "CASA-1234", role = HouseholdRole.OWNER)
        )
        override fun observeCurrentSession(): Flow<AuthSession?> = flowOf(currentSession)
        override suspend fun getCurrentSession(): AuthSession? = currentSession
        override suspend fun register(name: String, email: String, password: String) = throw UnsupportedOperationException()
        override suspend fun login(email: String, password: String) = throw UnsupportedOperationException()
        override suspend fun logout(forceDiscard: Boolean) = Result.success(Unit)
        override suspend fun refreshSession() = throw UnsupportedOperationException()
        override suspend fun clearSession() {}
    }

    private class FakeHouseholdRepository : HouseholdRepository {
        var activeHousehold: Household? = Household(id = "house-xyz", name = "Minha Casa", inviteCode = "CASA-1234", role = HouseholdRole.OWNER)
        override fun observeActiveHousehold(): Flow<Household?> = flowOf(activeHousehold)
        override suspend fun getActiveHousehold(): Household? = activeHousehold
        override suspend fun getUserHouseholds() = throw UnsupportedOperationException()
        override suspend fun createHousehold(name: String) = throw UnsupportedOperationException()
        override suspend fun updateHousehold(id: String, name: String) = throw UnsupportedOperationException()
        override suspend fun joinHousehold(inviteCode: String) = throw UnsupportedOperationException()
        override suspend fun leaveHousehold(id: String) = throw UnsupportedOperationException()
        override suspend fun regenerateInviteCode(id: String) = throw UnsupportedOperationException()
        override suspend fun removeMember(householdId: String, memberUserId: String) = throw UnsupportedOperationException()
        override suspend fun switchActiveHousehold(householdId: String) = Result.success(Unit)
    }

    @Test
    fun `getDeviceDiagnostics should include logs when true and platform info`() = runTest {
        val recorder = RingBufferLogRecorder()
        recorder.record("Log line 1")
        recorder.record("Log line 2")

        val repository = RealSupportRepository(
            remoteDataSource = FakeSupportRemoteDataSource(),
            platformProvider = FakeSupportPlatformProvider(),
            logRecorder = recorder,
            authRepository = FakeAuthRepository(),
            householdRepository = FakeHouseholdRepository()
        )

        val diagnosticsWithLogs = repository.getDeviceDiagnostics(includeLogs = true)
        assertEquals("1.2.0-alpha", diagnosticsWithLogs.appVersion)
        assertEquals("Pixel 7 Pro", diagnosticsWithLogs.deviceModel)
        assertEquals("Android 14", diagnosticsWithLogs.osVersion)
        assertEquals("user-abc", diagnosticsWithLogs.userId)
        assertEquals("house-xyz", diagnosticsWithLogs.householdId)
        assertEquals("Log line 1\nLog line 2", diagnosticsWithLogs.deviceLogs)

        val diagnosticsWithoutLogs = repository.getDeviceDiagnostics(includeLogs = false)
        assertNull(diagnosticsWithoutLogs.deviceLogs)
    }

    @Test
    fun `shareDiagnosticsLog should delegate formatted content to platformProvider`() = runTest {
        val platformProvider = FakeSupportPlatformProvider()
        val recorder = RingBufferLogRecorder()
        recorder.record("2026-10-25 10:00:00 [I] [App] Started")

        val repository = RealSupportRepository(
            remoteDataSource = FakeSupportRemoteDataSource(),
            platformProvider = platformProvider,
            logRecorder = recorder,
            authRepository = FakeAuthRepository(),
            householdRepository = FakeHouseholdRepository()
        )

        val report = SupportReport(
            category = SupportCategory.BUG,
            title = "Falha ao escanear",
            description = "Camera travou",
            diagnostics = repository.getDeviceDiagnostics(includeLogs = true),
            userContact = "contato@test.com"
        )

        val result = repository.shareDiagnosticsLog(report)
        assertTrue(result.isSuccess)
        assertEquals("Falha ao escanear", platformProvider.sharedTitle)
        val content = platformProvider.sharedContent!!
        assertTrue(content.contains("Categoria: BUG"))
        assertTrue(content.contains("Título: Falha ao escanear"))
        assertTrue(content.contains("Contato: contato@test.com"))
        assertTrue(content.contains("2026-10-25 10:00:00 [I] [App] Started"))
    }

    @Test
    fun `getPendingCrashReport and clearPendingCrashReport should manipulate platform crash file`() = runTest {
        val platformProvider = FakeSupportPlatformProvider()
        val repository = RealSupportRepository(
            remoteDataSource = FakeSupportRemoteDataSource(),
            platformProvider = platformProvider,
            logRecorder = RingBufferLogRecorder(),
            authRepository = FakeAuthRepository(),
            householdRepository = FakeHouseholdRepository()
        )

        assertEquals("NullPointerException at Example.kt:42", repository.getPendingCrashReport())
        repository.clearPendingCrashReport()
        assertNull(repository.getPendingCrashReport())
    }

    @Test
    fun `KtorSupportRemoteDataSource submitReport should parse success response from server`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                        "success": true,
                        "message": "Criado com sucesso",
                        "data": {
                            "id": "rep-99",
                            "protocol": "BUG-20261025-TEST",
                            "category": "BUG",
                            "title": "Bug test",
                            "status": "OPEN",
                            "hasLogs": true,
                            "createdAt": 1729851000000
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val dataSource = KtorSupportRemoteDataSource(
            httpClient = httpClient,
            config = SyncServerConfig("https://mock.domus.arpa"),
            authTokenProvider = { "valid-jwt-token" }
        )

        val report = SupportReport(
            category = SupportCategory.BUG,
            title = "Bug test",
            description = "Detalhes do bug",
            diagnostics = DeviceDiagnostics(appVersion = "1.2.0-alpha", deviceLogs = "sample log content")
        )

        val result = dataSource.submitReport(report)
        assertTrue(result.isSuccess)
        val receipt = result.getOrNull()!!
        assertEquals("BUG-20261025-TEST", receipt.protocol)
        assertEquals(SupportCategory.BUG, receipt.category)
        assertTrue(receipt.hasLogs)
    }

    @Test
    fun `KtorSupportRemoteDataSource should retry anonymously when receiving 401 Unauthorized`() = runTest {
        var attempts = 0
        val mockEngine = MockEngine { request ->
            attempts++
            val authHeader = request.headers[HttpHeaders.Authorization]
            if (authHeader != null) {
                // First request with token: simulate expired token returning 401
                respond(
                    content = """{"status":401,"error":"Unauthorized","message":"Jwt expired"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                // Second request without token: success
                respond(
                    content = """
                        {
                            "success": true,
                            "data": {
                                "id": "rep-anon",
                                "protocol": "BUG-20261025-ANON",
                                "category": "BUG",
                                "title": "Bug retry",
                                "status": "OPEN",
                                "hasLogs": false,
                                "createdAt": 1729852000000
                            }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.Created,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val dataSource = KtorSupportRemoteDataSource(
            httpClient = httpClient,
            config = SyncServerConfig("https://mock.domus.arpa"),
            authTokenProvider = { "expired-jwt-token" }
        )

        val report = SupportReport(
            category = SupportCategory.BUG,
            title = "Bug retry",
            description = "Detalhes do bug com token expirado",
            diagnostics = DeviceDiagnostics(appVersion = "1.2.0-alpha")
        )

        val result = dataSource.submitReport(report)
        assertTrue(result.isSuccess)
        assertEquals("BUG-20261025-ANON", result.getOrNull()!!.protocol)
        assertEquals(2, attempts)
    }
}

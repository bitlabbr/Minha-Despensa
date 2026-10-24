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

import io.ktor.client.engine.okhttp.OkHttpEngine
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AndroidHttpClientEngineTest {

    @Test
    fun `isTrustedDevHostname should correctly identify domus and local development hostnames`() {
        assertTrue(isTrustedDevHostname("despensa.domus.arpa"))
        assertTrue(isTrustedDevHostname("sub.internal.domus.arpa"))
        assertTrue(isTrustedDevHostname("localhost"))
        assertTrue(isTrustedDevHostname("10.0.2.2"))
        assertTrue(isTrustedDevHostname("100.98.199.97"))
        assertTrue(isTrustedDevHostname("100.66.150.125"))

        assertFalse(isTrustedDevHostname("google.com"))
        assertFalse(isTrustedDevHostname("api.external.com"))
        assertFalse(isTrustedDevHostname("domus.com"))
    }

    @Test
    fun `createTrustManager should return non-null SSLSocketFactory and X509TrustManager`() {
        val (socketFactory, trustManager) = createTrustManager()
        assertNotNull(socketFactory)
        assertNotNull(trustManager)
        assertTrue(trustManager.acceptedIssuers.isNotEmpty())
    }

    @Test
    fun `createPlatformHttpClientEngine should return an OkHttpEngine instance`() {
        val engine = createPlatformHttpClientEngine()
        assertNotNull(engine)
        assertTrue(engine is OkHttpEngine)
        engine.close()
    }
}

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

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

actual fun createPlatformHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            val (sslSocketFactory, trustManager) = createTrustManager()
            sslSocketFactory(sslSocketFactory, trustManager)
            val defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
            hostnameVerifier { hostname, session ->
                if (isTrustedDevHostname(hostname)) {
                    true
                } else {
                    defaultVerifier.verify(hostname, session)
                }
            }
        }
    }
}

internal fun createTrustManager(): Pair<SSLSocketFactory, X509TrustManager> {
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(null as java.security.KeyStore?)
    val standardTrustManager = tmf.trustManagers
        .filterIsInstance<X509TrustManager>()
        .firstOrNull() ?: throw IllegalStateException("Default X509TrustManager not found")

    val customTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            standardTrustManager.checkClientTrusted(chain, authType)
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            try {
                standardTrustManager.checkServerTrusted(chain, authType)
            } catch (originalException: CertificateException) {
                val isDevOrDomusCert = chain?.any { cert ->
                    val subject = cert.subjectX500Principal?.name.orEmpty()
                    val issuer = cert.issuerX500Principal?.name.orEmpty()
                    val sanList = try {
                        cert.subjectAlternativeNames?.mapNotNull { it?.getOrNull(1)?.toString() } ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }

                    subject.contains("domus.arpa") ||
                        issuer.contains("mkcert") ||
                        issuer.contains("domus") ||
                        sanList.any { it.contains("domus.arpa") || it.contains("localhost") }
                } == true

                if (!isDevOrDomusCert) {
                    throw originalException
                }
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return standardTrustManager.acceptedIssuers
        }
    }

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(customTrustManager), SecureRandom())
    return Pair(sslContext.socketFactory, customTrustManager)
}

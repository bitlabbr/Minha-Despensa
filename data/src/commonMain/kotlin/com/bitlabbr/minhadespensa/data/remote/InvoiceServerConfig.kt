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

class InvoiceServerConfig(
    val defaultBaseUrl: String = DEFAULT_BASE_URL,
) {
    private var customBaseUrl: String? = null

    val baseUrl: String
        get() = (customBaseUrl ?: defaultBaseUrl).trimEnd('/')

    fun setCustomBaseUrl(url: String?) {
        customBaseUrl = url?.trim()?.takeIf { it.isNotBlank() }
    }

    fun resetToDefault() {
        customBaseUrl = null
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
    }
}

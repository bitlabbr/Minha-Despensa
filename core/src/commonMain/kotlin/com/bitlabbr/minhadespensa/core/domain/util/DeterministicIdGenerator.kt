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

package com.bitlabbr.minhadespensa.core.domain.util

import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import okio.Buffer

object DeterministicIdGenerator {

    private const val NAMESPACE_PRODUCT_STR = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
    private const val NAMESPACE_CATEGORY_STR = "6ba7b811-9dad-11d1-80b4-00c04fd430c8"

    private val NAMESPACE_PRODUCT_BYTES = uuidStringToBytes(NAMESPACE_PRODUCT_STR)
    private val NAMESPACE_CATEGORY_BYTES = uuidStringToBytes(NAMESPACE_CATEGORY_STR)

    private val WHITESPACE_REGEX = "\\s+".toRegex()

    fun normalizeText(input: String): String {
        return input.unaccent()
            .lowercase()
            .trim()
            .replace(WHITESPACE_REGEX, " ")
    }

    fun uuid5(namespaceBytes: ByteArray, name: String): String {
        val data = Buffer().write(namespaceBytes).writeUtf8(name).readByteString()
        val sha1 = data.sha1().toByteArray()

        // RFC 4122 version 5 (bits 12-15 = 0101)
        sha1[6] = ((sha1[6].toInt() and 0x0f) or 0x50).toByte()
        // RFC 4122 variant (bits 6-7 = 10)
        sha1[8] = ((sha1[8].toInt() and 0x3f) or 0x80).toByte()

        val sb = StringBuilder(36)
        for (i in 0 until 16) {
            val byteInt = sha1[i].toInt() and 0xFF
            if (byteInt < 16) sb.append('0')
            sb.append(byteInt.toString(16))
            if (i == 3 || i == 5 || i == 7 || i == 9) {
                sb.append('-')
            }
        }
        return sb.toString()
    }

    fun forProduct(ean: String?, name: String, measureUnit: MeasureUnit = MeasureUnit.UNIT): String {
        return if (!ean.isNullOrBlank()) {
            productIdForEan(ean)
        } else {
            productIdForNameAndUnit(name, measureUnit.name)
        }
    }

    fun productIdForEan(ean: String): String {
        require(ean.isNotBlank()) { "EAN cannot be blank for deterministic ID generation" }
        return uuid5(NAMESPACE_PRODUCT_BYTES, "ean:${ean.trim()}")
    }

    fun productIdForNameAndUnit(name: String, measureUnit: String): String {
        require(name.isNotBlank()) { "Product name cannot be blank for deterministic ID generation" }
        val normalizedName = normalizeText(name)
        val unit = measureUnit.trim().uppercase()
        return uuid5(NAMESPACE_PRODUCT_BYTES, "name:$normalizedName:$unit")
    }

    fun forCategory(name: String): String {
        require(name.isNotBlank()) { "Category name cannot be blank for deterministic ID generation" }
        val normalizedName = normalizeText(name)
        return uuid5(NAMESPACE_CATEGORY_BYTES, normalizedName)
    }

    fun forMonthlyBudget(year: Int, month: Int): String {
        require(year in 2000..2100) { "Invalid year: $year" }
        require(month in 1..12) { "Invalid month: $month" }
        return "$year-$month"
    }

    private fun uuidStringToBytes(uuidStr: String): ByteArray {
        val clean = uuidStr.replace("-", "")
        require(clean.length == 32) { "Invalid UUID length: $uuidStr" }
        val bytes = ByteArray(16)
        for (i in 0 until 16) {
            bytes[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return bytes
    }
}

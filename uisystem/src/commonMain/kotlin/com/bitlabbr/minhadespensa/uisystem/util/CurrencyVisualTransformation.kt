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

package com.bitlabbr.minhadespensa.uisystem.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation that formats numbers with Brazilian thousands separator (dots)
 * and preserves decimals (comma), e.g. "2549,98" -> "2.549,98".
 *
 * Operates purely visually so underlying text state contains no formatting dots,
 * preventing cursor jumps and backward typing issues.
 */
class CurrencyThousandsVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val hasComma = original.contains(',')
        val integerPart = original.substringBefore(',')
        val decimalPart = if (hasComma) "," + original.substringAfter(',') else ""

        val formattedInt = if (integerPart.isEmpty()) {
            ""
        } else {
            integerPart.reversed().chunked(3).joinToString(".").reversed()
        }
        val transformedString = formattedInt + decimalPart

        val origToTrans = IntArray(original.length + 1)
        val transToOrig = IntArray(transformedString.length + 1)

        var origIdx = 0
        for (transIdx in 0 until transformedString.length) {
            transToOrig[transIdx] = origIdx.coerceAtMost(original.length)
            if (transformedString[transIdx] == '.') {
                // Dot is visual only, does not consume original character
            } else {
                if (origIdx <= original.length) {
                    origToTrans[origIdx] = transIdx
                }
                origIdx++
            }
        }
        origToTrans[original.length] = transformedString.length
        transToOrig[transformedString.length] = original.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, original.length)
                return origToTrans[clamped].coerceIn(0, transformedString.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformedString.length)
                return transToOrig[clamped].coerceIn(0, original.length)
            }
        }

        return TransformedText(AnnotatedString(transformedString), offsetMapping)
    }
}

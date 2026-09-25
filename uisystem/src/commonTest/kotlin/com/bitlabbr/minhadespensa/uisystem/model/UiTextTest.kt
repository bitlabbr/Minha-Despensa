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

package com.bitlabbr.minhadespensa.uisystem.model

import kotlinx.coroutines.test.runTest
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.search
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UiTextTest {

    @Test
    fun dynamicString_equalityAndValueResolution() = runTest {
        val text1 = UiText.DynamicString("Teste")
        val text2 = UiText.DynamicString("Teste")
        val text3 = UiText.DynamicString("Outro")

        assertEquals(text1, text2)
        assertNotEquals(text1, text3)
        assertEquals("Teste", text1.value)
        assertEquals("Teste", text1.asStringAsync())
    }

    @Test
    fun resource_dataClassStructuralEquality() {
        val res1 = UiText.Resource(Res.string.search, listOf("arg1"))
        val res2 = UiText.Resource(Res.string.search, listOf("arg1"))
        val res3 = UiText.Resource(Res.string.search, listOf("arg2"))

        assertEquals(res1, res2)
        assertNotEquals(res1, res3)
        assertEquals(res1.hashCode(), res2.hashCode())
    }
}

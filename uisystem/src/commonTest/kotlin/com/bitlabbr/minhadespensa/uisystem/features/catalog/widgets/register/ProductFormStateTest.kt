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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register

import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ProductFormStateTest {

    @Test
    fun defaultState_isNotValidDueToBlankName() {
        val state = ProductFormState()
        assertFalse(state.isFormValid)
    }

    @Test
    fun isFormValid_returnsTrueWhenFieldsValidAndNoErrors() {
        val state = ProductFormState(
            name = "Feijão Preto",
            netWeight = "1.0",
        )
        assertTrue(state.isFormValid)
    }

    @Test
    fun isFormValid_returnsFalseWhenAnyErrorPresent() {
        val validBase = ProductFormState(name = "Feijão Preto")

        assertFalse(validBase.copy(nameError = UiText.DynamicString("Erro")).isFormValid)
        assertFalse(validBase.copy(netWeightError = UiText.DynamicString("Erro")).isFormValid)
        assertFalse(validBase.copy(eanError = UiText.DynamicString("Erro")).isFormValid)
        assertFalse(validBase.copy(categoryError = UiText.DynamicString("Erro")).isFormValid)
        assertFalse(validBase.copy(measureUnitError = UiText.DynamicString("Erro")).isFormValid)
    }

    @Test
    fun isFormValid_returnsFalseWhenCheckingEanOrSaving() {
        val validBase = ProductFormState(name = "Feijão Preto")

        assertFalse(validBase.copy(isCheckingEan = true).isFormValid)
        assertFalse(validBase.copy(isSaving = true).isFormValid)
    }

    @Test
    fun structuralEquality_withImageBytesAndErrors() {
        val bytes1 = byteArrayOf(1, 2, 3)
        val bytes2 = byteArrayOf(1, 2, 3)
        val state1 = ProductFormState(
            name = "Arroz",
            imageBytes = bytes1,
            nameError = UiText.DynamicString("Obrigatório"),
        )
        val state2 = ProductFormState(
            name = "Arroz",
            imageBytes = bytes2,
            nameError = UiText.DynamicString("Obrigatório"),
        )
        val state3 = state1.copy(name = "Feijão")

        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
        assertNotEquals(state1, state3)
    }
}

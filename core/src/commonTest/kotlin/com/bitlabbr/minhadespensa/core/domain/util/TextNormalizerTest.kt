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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextNormalizerTest {

    @Test
    fun unaccent_replacesPortugueseDiacriticsCorrectly() {
        assertEquals("a", "á".unaccent())
        assertEquals("a", "à".unaccent())
        assertEquals("a", "â".unaccent())
        assertEquals("a", "ã".unaccent())
        assertEquals("e", "é".unaccent())
        assertEquals("e", "ê".unaccent())
        assertEquals("i", "í".unaccent())
        assertEquals("o", "ó".unaccent())
        assertEquals("o", "ô".unaccent())
        assertEquals("o", "õ".unaccent())
        assertEquals("u", "ú".unaccent())
        assertEquals("u", "ü".unaccent())
        assertEquals("c", "ç".unaccent())

        assertEquals("a", "Á".unaccent())
        assertEquals("a", "À".unaccent())
        assertEquals("a", "Â".unaccent())
        assertEquals("a", "Ã".unaccent())
        assertEquals("e", "É".unaccent())
        assertEquals("e", "Ê".unaccent())
        assertEquals("i", "Í".unaccent())
        assertEquals("o", "Ó".unaccent())
        assertEquals("o", "Ô".unaccent())
        assertEquals("o", "Õ".unaccent())
        assertEquals("u", "Ú".unaccent())
        assertEquals("c", "Ç".unaccent())
    }

    @Test
    fun unaccent_preservesRegularAsciiCharacters() {
        val input = "Arroz Branco 1kg - Camil"
        assertEquals(input, input.unaccent())
    }

    @Test
    fun unaccent_handlesComplexSentencesWithMultipleAccents() {
        val input = "Pão de Açúcar com Café e Maçã"
        val expected = "Pao de Acucar com Cafe e Maca"
        assertEquals(expected, input.unaccent())
    }

    @Test
    fun normalizeForSearch_removesAccentsLowercasesAndTrims() {
        assertEquals("acucar organico", "  Açúcar Orgânico  ".normalizeForSearch())
        assertEquals("pao de forma", "PÃO DE FORMA".normalizeForSearch())
    }

    @Test
    fun matchesSearchQuery_matchesWhenAccentsAreOmittedInQuery() {
        assertTrue("Pão de Forma".matchesSearchQuery("pao"))
        assertTrue("Maçã Fuji".matchesSearchQuery("maca"))
        assertTrue("Açúcar Cristal".matchesSearchQuery("acucar"))
        assertTrue("Café Especial".matchesSearchQuery("cafe"))
        assertTrue("Três Corações".matchesSearchQuery("tres coracoes"))
    }

    @Test
    fun matchesSearchQuery_matchesWhenAccentsArePresentInQueryButTargetIsPlain() {
        assertTrue("Pao Frances".matchesSearchQuery("pão"))
        assertTrue("Maca Verde".matchesSearchQuery("maçã"))
    }

    @Test
    fun matchesSearchQuery_handlesMultiWordQueriesOutOfOrderOrWithGaps() {
        assertTrue("Pão de Forma Visconti".matchesSearchQuery("pao visconti"))
        assertTrue("Café Torrado e Moído Melitta".matchesSearchQuery("cafe melitta"))
        assertTrue("Feijão Carioca Camil Tipo 1".matchesSearchQuery("feijao tipo 1"))
    }

    @Test
    fun matchesSearchQuery_returnsTrueForBlankQuery() {
        assertTrue("Qualquer Produto".matchesSearchQuery(""))
        assertTrue("Qualquer Produto".matchesSearchQuery("   "))
    }

    @Test
    fun matchesSearchQuery_returnsFalseWhenNoMatch() {
        assertFalse("Leite Desnatado".matchesSearchQuery("cafe"))
        assertFalse("Açúcar Refinado".matchesSearchQuery("sal"))
    }
}

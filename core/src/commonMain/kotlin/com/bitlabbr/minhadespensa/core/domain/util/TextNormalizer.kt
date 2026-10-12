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

/**
 * Remove acentos e caracteres diacríticos comuns da língua portuguesa e do alfabeto latino,
 * preservando os demais caracteres.
 */
fun String.unaccent(): String {
    val sb = StringBuilder(length)
    for (ch in this) {
        val replacement = when (ch) {
            'á', 'à', 'â', 'ã', 'ä', 'å', 'Á', 'À', 'Â', 'Ã', 'Ä', 'Å' -> 'a'
            'é', 'è', 'ê', 'ë', 'É', 'È', 'Ê', 'Ë' -> 'e'
            'í', 'ì', 'î', 'ï', 'Í', 'Ì', 'Î', 'Ï' -> 'i'
            'ó', 'ò', 'ô', 'õ', 'ö', 'ø', 'Ó', 'Ò', 'Ô', 'Õ', 'Ö', 'Ø' -> 'o'
            'ú', 'ù', 'û', 'ü', 'Ú', 'Ù', 'Û', 'Ü' -> 'u'
            'ç', 'Ç' -> 'c'
            'ñ', 'Ñ' -> 'n'
            'ý', 'ÿ', 'Ý', 'Ÿ' -> 'y'
            else -> ch
        }
        sb.append(replacement)
    }
    return sb.toString()
}

/**
 * Normaliza a string para comparação em pesquisas, removendo acentos, convertendo para minúsculas
 * e aparando espaços em branco nas extremidades.
 */
fun String.normalizeForSearch(): String = this.unaccent().lowercase().trim()

/**
 * Verifica se a string candidata corresponde a um termo de busca [query], ignorando
 * acentuação, maiúsculas/minúsculas e suportando buscas multi-palavra divididas por espaço.
 *
 * Se [query] for em branco, retorna `true`.
 */
fun String.matchesSearchQuery(query: String): Boolean {
    val normalizedQuery = query.normalizeForSearch()
    if (normalizedQuery.isBlank()) return true

    val normalizedTarget = this.normalizeForSearch()
    if (normalizedTarget.contains(normalizedQuery)) return true

    val tokens = normalizedQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return tokens.isNotEmpty() && tokens.all { token -> normalizedTarget.contains(token) }
}

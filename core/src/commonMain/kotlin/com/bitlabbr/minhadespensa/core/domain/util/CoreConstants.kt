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

object CoreConstants {

    object Product {
        val DEFAULT_MEASURE_UNITY = MeasureUnit.UNIT
        const val DEFAULT_CATEGORY = "Outros"
        const val NAME_MAX_LENGTH = 30
        const val BRAND_MAX_LENGTH = 30
        const val CATEGORY_MAX_LENGTH = 30
        const val NOTES_MAX_LENGTH = 255
        val EAN_VALID_LENGTHS = setOf(8, 12, 13, 14)
        const val DEFAULT_NET_WEIGHT = 1.0
        const val WEIGHT_MAX_LENGTH = 8
    }

    object Media {
        const val MAX_IMAGE_SIZE_KB = 100
    }

    object ShoppingList {
        const val NAME_MAX_LENGTH = 30
    }

    object Pantry {
        const val EXPIRING_THRESHOLD_DAYS = 7
    }

    object Validation {
        const val ERROR_PRODUCT_CATEGORY_TOO_LONG = "A categoria ultrapassa o limite máximo de :"
        const val ERROR_PRODUCT_ID_REQUIRED = "O ID do produto é obrigatório"
        const val ERROR_PANTRY_QUANTITY_POSITIVE = "A quantidade adicionada deve ser maior que zero"
        const val ERROR_PRODUCT_NAME_BLANK = "O nome do produto não pode ser vazio"
        const val ERROR_PRODUCT_NAME_TOO_LONG = "O nome do produto excede o limite máximo de caracteres"
        const val ERROR_PRODUCT_NET_WEIGHT_POSITIVE = "O peso ou quantidade líquida deve ser maior que zero"
        const val ERROR_EAN_INVALID_FORMAT = "Código de barras inválido. Deve conter 8, 13 ou 14 dígitos numéricos"
    }

    object CatalogCategories {
        val DEFAULT_CATEGORIES = listOf(
            "Cereais e Grãos",
            "Carnes e Proteínas",
            "Laticínios e Ovos",
            "Hortifrúti",
            "Bebidas",
            "Produtos de Limpeza",
            "Higiene Pessoal",
            "Temperos e Condimentos",
            Product.DEFAULT_CATEGORY,
        )
    }

}
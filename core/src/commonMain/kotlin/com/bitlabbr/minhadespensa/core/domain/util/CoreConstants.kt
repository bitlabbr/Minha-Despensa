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
        val DEFAULT_MEASURE_UNIT = MeasureUnit.UNIT
        @Deprecated("Use DEFAULT_MEASURE_UNIT instead", ReplaceWith("DEFAULT_MEASURE_UNIT"))
        val DEFAULT_MEASURE_UNITY = DEFAULT_MEASURE_UNIT
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

    object Auth {
        const val NAME_MAX_LENGTH = 80
        const val PASSWORD_MIN_LENGTH = 8
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    object Household {
        const val NAME_MAX_LENGTH = 80
        const val INVITE_CODE_LENGTH = 8
    }

    object Validation {
        const val ERROR_PRODUCT_CATEGORY_TOO_LONG = "A categoria ultrapassa o limite máximo de"
        const val ERROR_PRODUCT_ID_REQUIRED = "O ID do produto é obrigatório"
        const val ERROR_PANTRY_ITEM_ID_REQUIRED = "O ID do item da despensa é obrigatório"
        const val ERROR_PANTRY_QUANTITY_POSITIVE = "A quantidade adicionada deve ser maior que zero"
        const val ERROR_PANTRY_QUANTITY_NON_NEGATIVE = "A quantidade não pode ser negativa"
        const val ERROR_PRODUCT_NAME_BLANK = "O nome do produto não pode ser vazio"
        const val ERROR_PRODUCT_NAME_TOO_LONG = "O nome do produto excede o limite máximo de caracteres"
        const val ERROR_PRODUCT_NET_WEIGHT_POSITIVE = "O peso ou quantidade líquida deve ser maior que zero"
        const val ERROR_EAN_INVALID_FORMAT = "Código de barras inválido. Deve conter 8, 12, 13 ou 14 dígitos numéricos"
        const val ERROR_PANTRY_UNIT_PRICE_NON_NEGATIVE = "O preço unitário não pode ser negativo"
        const val ERROR_CATEGORY_NAME_BLANK = "O nome da categoria não pode ser vazio"
        const val ERROR_CATEGORY_ALREADY_EXISTS = "Já existe uma categoria com este nome"
        const val ERROR_CATEGORY_DEFAULT_IMMUTABLE = "A categoria padrão 'Outros' não pode ser alterada ou excluída"
        const val ERROR_CATEGORY_ID_REQUIRED = "O ID da categoria é obrigatório"
        const val ERROR_INVOICE_QR_CODE_BLANK = "O conteúdo do QR Code da nota fiscal não pode ser vazio"
        const val ERROR_INVOICE_ITEMS_EMPTY = "Nenhum item selecionado para importação da nota fiscal"
        const val ERROR_INVOICE_STORE_NAME_BLANK = "O nome do estabelecimento não pode ser vazio"
        const val ERROR_USER_NAME_BLANK = "O nome do usuário não pode ser vazio"
        const val ERROR_USER_NAME_TOO_LONG = "O nome do usuário não pode exceder 80 caracteres"
        const val ERROR_EMAIL_INVALID = "E-mail inválido"
        const val ERROR_PASSWORD_TOO_SHORT = "A senha deve ter no mínimo 8 caracteres"
        const val ERROR_HOUSEHOLD_NAME_BLANK = "O nome da despensa compartilhada não pode ser vazio"
        const val ERROR_HOUSEHOLD_NAME_TOO_LONG = "O nome da despensa compartilhada não pode exceder 80 caracteres"
        const val ERROR_INVITE_CODE_INVALID = "Código de convite deve conter exatamente 8 caracteres alfanuméricos"
        const val ERROR_NO_ACTIVE_HOUSEHOLD = "Nenhuma despensa ativa selecionada"
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
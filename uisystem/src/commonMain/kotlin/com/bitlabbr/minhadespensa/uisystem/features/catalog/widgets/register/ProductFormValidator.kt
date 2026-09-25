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

import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.EanStatus
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import minhadespensa.uisystem.generated.resources.*

/**
 * Valida alterações nos campos do formulário de cadastro de produto (nome, peso líquido, etc.).
 */
fun validateFormFields(
    current: ProductFormState,
    updated: ProductFormState,
): ProductFormState {
    val isEanChanged = updated.ean != current.ean
    val preservedEanError = if (isEanChanged) null else current.eanError
    val preservedIsChecking = if (isEanChanged) false else current.isCheckingEan

    return updated.copy(
        nameError = when {
            updated.name.isBlank() && current.name.isNotEmpty() ->
                UiText.Resource(Res.string.error_name_required)

            updated.name.length > CoreConstants.Product.NAME_MAX_LENGTH ->
                UiText.Resource(Res.string.error_name_max_length, listOf(CoreConstants.Product.NAME_MAX_LENGTH))

            else -> null
        },
        netWeightError = when {
            updated.netWeight.isNotBlank() &&
                    updated.netWeight.replace(',', '.').toDoubleOrNull() == null ->
                UiText.Resource(Res.string.error_invalid_number)

            else -> null
        },
        eanError = preservedEanError,
        isCheckingEan = preservedIsChecking,
    )
}

/**
 * Valida o formato e a unicidade de um código de barras no catálogo.
 * Retorna null se o código for válido e estiver disponível para cadastro.
 */
suspend fun checkEanValidity(
    ean: String,
    checkEanStatusUseCase: CheckEanStatusUseCase,
): UiText? {
    val trimmed = ean.trim()
    if (trimmed.isBlank()) return null

    if (trimmed.length !in CoreConstants.Product.EAN_VALID_LENGTHS) {
        return UiText.Resource(Res.string.error_ean_invalid_length)
    }

    return when (val status = checkEanStatusUseCase(trimmed)) {
        is EanStatus.Found -> UiText.Resource(
            Res.string.error_ean_already_exists,
            listOf(status.product.name),
        )
        is EanStatus.NotFound, EanStatus.Empty -> null
        is EanStatus.InvalidFormat -> UiText.Resource(Res.string.error_ean_invalid_length)
    }
}

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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.usecase.SaveCatalogProductUseCase
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.register_bottom_sheet_invalid_quantity
import minhadespensa.uisystem.generated.resources.register_bottom_sheet_name_is_required
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductBottomSheet(
    isOpen: Boolean,
    prefilledEan: String? = null,
    availableCategories: List<String> = emptyList(),
    onProductCreated: (CatalogProduct) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    saveProductUseCase: SaveCatalogProductUseCase = koinInject(),
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var formState by remember(isOpen, prefilledEan) {
        mutableStateOf(
            ProductFormState(
                ean = prefilledEan.orEmpty(),
                availableCategories = availableCategories.ifEmpty { listOf(CoreConstants.Product.DEFAULT_CATEGORY) },
            )
        )
    }

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        RegisterProductFormContent(
            state = formState,
            onStateChange = { formState = it },
            onCancelClick = onDismiss,
            onSaveClick = {
                val weight = formState.netWeight.replace(',', '.').toDoubleOrNull()
                if (formState.name.isBlank()) {
                    formState =
                        formState.copy(nameError = UiText.Resource(Res.string.register_bottom_sheet_name_is_required))
                    return@RegisterProductFormContent
                }
                if (weight == null || weight <= 0.0) {
                    formState =
                        formState.copy(netWeightError = UiText.Resource(Res.string.register_bottom_sheet_invalid_quantity))
                    return@RegisterProductFormContent
                }

                coroutineScope.launch {
                    formState = formState.copy(isSaving = true)
                    val result = saveProductUseCase(
                        name = formState.name,
                        brand = formState.brand.takeIf { it.isNotBlank() },
                        category = formState.category,
                        measureUnit = formState.measureUnit,
                        netWeight = weight,
                        ean = formState.ean.takeIf { it.isNotBlank() },
                        imageBytes = formState.imageBytes,
                    )

                    result.onSuccess { createdProduct ->
                        formState = formState.copy(isSaving = false)
                        onProductCreated(createdProduct)
                    }.onFailure { error ->
                        formState = formState.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Erro ao salvar",
                        )
                    }
                }
            },
        )
    }
}
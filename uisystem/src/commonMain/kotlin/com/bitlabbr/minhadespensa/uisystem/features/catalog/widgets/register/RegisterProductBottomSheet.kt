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
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.EanStatus
import com.bitlabbr.minhadespensa.core.domain.usecase.SaveCatalogProductUseCase
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.sheet.MinhaDespensaBottomSheet
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.*
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductBottomSheet(
    isOpen: Boolean,
    formState: ProductFormState,
    onFormChange: (ProductFormState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) return

    MinhaDespensaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        RegisterProductFormContent(
            state = formState,
            onStateChange = onFormChange,
            onSaveClick = onSave,
            onCancelClick = onDismiss,
        )
    }
}

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
    checkEanStatusUseCase: CheckEanStatusUseCase = koinInject(),
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
    var eanValidationJob by remember { mutableStateOf<Job?>(null) }

    fun validateEanDebounced(ean: String) {
        eanValidationJob?.cancel()
        val trimmed = ean.trim()
        if (trimmed.isBlank()) {
            formState = formState.copy(eanError = null, isCheckingEan = false)
            return
        }

        if (trimmed.length !in CoreConstants.Product.EAN_VALID_LENGTHS) {
            formState = formState.copy(
                eanError = UiText.Resource(Res.string.error_ean_invalid_length),
                isCheckingEan = false,
            )
            return
        }

        formState = formState.copy(isCheckingEan = true, eanError = null)
        eanValidationJob = coroutineScope.launch {
            delay(350.milliseconds)
            val error = checkEanValidity(trimmed, checkEanStatusUseCase)
            if (formState.ean.trim() == trimmed) {
                formState = formState.copy(
                    eanError = error,
                    isCheckingEan = false,
                )
            }
        }
    }

    LaunchedEffect(isOpen, prefilledEan) {
        if (isOpen && !prefilledEan.isNullOrBlank()) {
            validateEanDebounced(prefilledEan)
        }
    }

    val handleFormChange: (ProductFormState) -> Unit = { updatedForm ->
        val current = formState
        val isEanChanged = updatedForm.ean != current.ean
        val validatedState = validateFormFields(current, updatedForm)
        formState = validatedState

        if (isEanChanged) {
            validateEanDebounced(updatedForm.ean)
        }
    }

    val handleDismiss: () -> Unit = {
        eanValidationJob?.cancel()
        onDismiss()
    }

    MinhaDespensaBottomSheet(
        onDismissRequest = handleDismiss,
        modifier = modifier,
    ) {
        RegisterProductFormContent(
            state = formState,
            onStateChange = handleFormChange,
            onCancelClick = handleDismiss,
            onSaveClick = {
                val weight = formState.netWeight.replace(',', '.').toDoubleOrNull()
                if (formState.name.isBlank()) {
                    formState = formState.copy(nameError = UiText.Resource(Res.string.error_name_required))
                    return@RegisterProductFormContent
                }
                if (weight == null || weight <= 0.0) {
                    formState = formState.copy(netWeightError = UiText.Resource(Res.string.error_invalid_number))
                    return@RegisterProductFormContent
                }
                if (formState.eanError != null || formState.isCheckingEan) {
                    return@RegisterProductFormContent
                }

                coroutineScope.launch {
                    val candidateEan = formState.ean.filter { it.isDigit() }.takeIf { it.isNotBlank() }
                    if (candidateEan != null) {
                        when (val status = checkEanStatusUseCase(candidateEan)) {
                            is EanStatus.Found -> {
                                formState = formState.copy(isSaving = false)
                                onProductCreated(status.product)
                                return@launch
                            }
                            else -> Unit
                        }
                    }

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
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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.quicklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.usecase.CreateQuickShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickListViewModel(
    private val createQuickShoppingListUseCase: CreateQuickShoppingListUseCase,
    private val logger: AppLogger,
) : ViewModel() {

    private val TAG = "QuickListViewModel"

    private val _uiState = MutableStateFlow(QuickListUiState())
    val uiState: StateFlow<QuickListUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(rawContent = newContent, errorMessage = null) }
    }

    fun saveQuickList(onSuccess: (listId: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (!currentState.canSave) return@launch

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = createQuickShoppingListUseCase(
                rawContent = currentState.rawContent,
                customTitle = currentState.title.takeIf { it.isNotBlank() },
            )

            result.onSuccess { createdList ->
                logger.d(TAG, "Lista rápida salva com sucesso: ${createdList.id}")
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                onSuccess(createdList.id)
            }.onFailure { error ->
                logger.e(TAG, "Falha ao salvar lista rápida: ${error.message}", error)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = error.message ?: "Erro ao salvar lista")
                }
            }
        }
    }
}
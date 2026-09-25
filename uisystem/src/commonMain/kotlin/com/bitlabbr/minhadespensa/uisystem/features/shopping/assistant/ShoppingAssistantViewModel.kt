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

package com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.AddOrUpdateCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.EanStatus
import com.bitlabbr.minhadespensa.core.domain.usecase.FinalizeShoppingSessionUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.StartShoppingSessionUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.CartItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantSubFlow
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.model.ShoppingAssistantUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingAssistantViewModel(
    private val startShoppingSessionUseCase: StartShoppingSessionUseCase,
    private val addOrUpdateCartItemUseCase: AddOrUpdateCartItemUseCase,
    private val finalizeShoppingSessionUseCase: FinalizeShoppingSessionUseCase,
    private val checkEanStatusUseCase: CheckEanStatusUseCase,
    private val shoppingListRepository: ShoppingListRepository,
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val TAG = "ShoppingAssistantVM"

    private val _currentListId = MutableStateFlow<String?>(null)
    private val _activeSubFlow = MutableStateFlow<ShoppingAssistantSubFlow?>(null)
    private val _isFinalizing = MutableStateFlow(false)
    private val _isCompleted = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Agrupa os controles transitórios de UI (4 fluxos <= 5)
    private data class SessionControlState(
        val subFlow: ShoppingAssistantSubFlow?,
        val isFinalizing: Boolean,
        val isCompleted: Boolean,
        val errorMessage: String?,
    )

    private val sessionControlFlow = combine(
        _activeSubFlow,
        _isFinalizing,
        _isCompleted,
        _errorMessage,
    ) { subFlow, isFinalizing, isCompleted, error ->
        SessionControlState(subFlow, isFinalizing, isCompleted, error)
    }

    private val currentListFlow = _currentListId.flatMapLatest { id ->
        if (id == null) flowOf(null) else shoppingListRepository.getShoppingListById(id)
    }

    // Combine principal reduzido para apenas 3 fluxos
    val uiState: StateFlow<ShoppingAssistantUiState> = combine(
        currentListFlow,
        catalogRepository.getAllActiveProducts(),
        sessionControlFlow,
    ) { currentList, catalogProducts, control ->
        if (currentList == null) {
            return@combine ShoppingAssistantUiState(
                isLoading = _currentListId.value != null,
                errorMessage = control.errorMessage,
            )
        }

        val catalogMap = catalogProducts.associateBy { it.id }

        val uiItems = currentList.items.filter { !it.isDeleted }.map { item ->
            val product = item.productId?.let { catalogMap[it] }
            val displayName = product?.name ?: item.rawText ?: "Item sem nome"
            val price = item.priceAtTime ?: 0L
            val subtotal = (item.quantity * price).roundToLong()

            CartItemUiModel(
                id = item.id,
                productId = item.productId,
                displayName = displayName,
                quantity = item.quantity,
                priceAtTime = item.priceAtTime,
                isChecked = item.isChecked,
                subtotalInCents = subtotal,
            )
        }

        val totalCart: Long = uiItems.filter { it.isChecked }.sumOf { it.subtotalInCents }
        val checkedCount = uiItems.count { it.isChecked }

        ShoppingAssistantUiState(
            listId = currentList.id,
            listTitle = currentList.name,
            items = uiItems,
            totalCartValueInCents = totalCart,
            checkedCount = checkedCount,
            totalCount = uiItems.size,
            activeSubFlow = control.subFlow,
            isFinalizing = control.isFinalizing,
            isCompleted = control.isCompleted,
            errorMessage = control.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShoppingAssistantUiState(isLoading = true),
    )

    fun startSession(existingListId: String? = null) {
        viewModelScope.launch {
            val result = startShoppingSessionUseCase(existingListId)
            result.onSuccess { list ->
                _currentListId.value = list.id
                logger.d(TAG, "Sessão iniciada na lista: ${list.id}")
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun onScanBarcodeClicked() {
        _activeSubFlow.value = ShoppingAssistantSubFlow.BarcodeScanner
    }

    fun onCloseSubFlow() {
        _activeSubFlow.value = null
    }

    fun onBarcodeScanned(ean: String) {
        val sanitizedEan = ean.filter { it.isDigit() }
        viewModelScope.launch {
            when (val status = checkEanStatusUseCase(sanitizedEan)) {
                is EanStatus.Found -> {
                    _activeSubFlow.value = ShoppingAssistantSubFlow.AddItemDetails(
                        product = status.product,
                        initialQuantity = 1.0,
                    )
                }
                is EanStatus.NotFound, is EanStatus.InvalidFormat -> {
                    _activeSubFlow.value = ShoppingAssistantSubFlow.CreateProduct(initialEan = sanitizedEan)
                }
                EanStatus.Empty -> Unit
            }
        }
    }

    fun onProductCreatedFromCatalog(product: CatalogProduct) {
        _activeSubFlow.value = ShoppingAssistantSubFlow.AddItemDetails(
            product = product,
            initialQuantity = 1.0,
        )
    }

    fun onEditItemClicked(item: CartItemUiModel) {
        viewModelScope.launch {
            val product = item.productId?.let { catalogRepository.getProductById(it).firstOrNull() }
            _activeSubFlow.value = ShoppingAssistantSubFlow.AddItemDetails(
                product = product,
                rawText = if (product == null) item.displayName else null,
                existingItemId = item.id,
                initialQuantity = item.quantity,
                initialPriceInCents = item.priceAtTime,
            )
        }
    }

    fun onConfirmItemDetails(
        product: CatalogProduct?,
        rawText: String?,
        quantity: Double,
        priceInCents: Long?,
        existingItemId: String?,
    ) {
        val listId = _currentListId.value ?: return
        viewModelScope.launch {
            addOrUpdateCartItemUseCase(
                listId = listId,
                productId = product?.id,
                rawText = rawText ?: product?.name,
                quantity = quantity,
                priceAtTimeInCents = priceInCents,
                existingItemId = existingItemId,
            )
            _activeSubFlow.value = null
        }
    }

    fun onToggleItemChecked(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            shoppingListRepository.toggleItemCheck(itemId, isChecked)
        }
    }

    fun onFinalizePurchase(onFinished: () -> Unit) {
        val listId = _currentListId.value ?: return
        viewModelScope.launch {
            _isFinalizing.value = true
            val result = finalizeShoppingSessionUseCase(listId)
            result.onSuccess {
                _isFinalizing.value = false
                _isCompleted.value = true
                onFinished()
            }.onFailure { error ->
                _isFinalizing.value = false
                _errorMessage.value = error.message ?: "Erro ao finalizar compra"
            }
        }
    }
}
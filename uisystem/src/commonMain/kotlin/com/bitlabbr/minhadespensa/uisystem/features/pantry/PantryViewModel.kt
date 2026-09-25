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

package com.bitlabbr.minhadespensa.uisystem.features.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.AddPantryItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.EanStatus
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.toUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.*
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val catalogRepository: CatalogRepository,
    private val checkEanStatusUseCase: CheckEanStatusUseCase,
    private val addPantryItemUseCase: AddPantryItemUseCase,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager,
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _activeSubFlow = MutableStateFlow<PantrySubFlow?>(null)
    private val pantryIdToProductIdMap = mutableMapOf<String, String>()

    val uiState: StateFlow<PantryUiState> = combine(
        pantryRepository.getAllActivePantryItemsWithCategory(),
        pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS),
        _searchQuery,
        _selectedCategory,
        _activeSubFlow,
    ) { allItems, expiringItems, query, selectedCategory, subFlow ->

        allItems.forEach { item ->
            pantryIdToProductIdMap[item.pantryItem.id] = item.pantryItem.productId
            pantryIdToProductIdMap[item.pantryItem.productId] = item.pantryItem.productId
        }

        val allUiItems = aggregatePantryItems(allItems)
        val isPantryEmpty = allUiItems.isEmpty()

        val dynamicCategories =
            (CoreConstants.CatalogCategories.DEFAULT_CATEGORIES + allUiItems.map { it.category.trim() })
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

        val filteredProducts = allUiItems.filter { item ->
            val matchesCategory = selectedCategory == null ||
                    item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }

        val searchResults = if (query.isBlank()) {
            emptyList()
        } else {
            allUiItems.filter { it.name.contains(query, ignoreCase = true) }
        }

        PantryUiState(
            filterState = PantryFilterSubState(
                availableCategories = dynamicCategories,
                selectedCategory = selectedCategory,
            ),
            listState = PantryListSubState(
                products = filteredProducts,
                isLoading = false,
                isCatalogEmpty = isPantryEmpty,
                error = null,
            ),
            allActivePantryItems = allUiItems,
            expiringPantryItems = aggregatePantryItems(expiringItems).sortedBy { it.expirationDate ?: Long.MAX_VALUE },
            searchResults = searchResults,
            activeSubFlow = subFlow,
            isLoading = false,
            error = null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PantryUiState(
            listState = PantryListSubState(isLoading = true),
            isLoading = true,
        ),
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchResultSelected(item: PantryItemUiModel) {
        _searchQuery.value = item.name
    }

    fun getProductImage(id: String): Flow<ByteArray?> {
        val targetProductId = pantryIdToProductIdMap[id] ?: id
        return catalogRepository.getProductImage(targetProductId)
            .catch { error ->
                logger.e(TAG, "Error while loading image for id:$id: ${error.message}", error)
                emit(null)
            }
    }

    // --- MÁQUINA DE ESTADOS DOS SUBFLUXOS ---

    fun onStartScanFlow() {
        _activeSubFlow.value = PantrySubFlow.BarcodeScanner
    }

    fun onStartManualRegisterFlow(ean: String? = null) {
        _activeSubFlow.value = PantrySubFlow.CreateCatalogProduct(initialEan = ean)
    }

    fun onDismissSubFlow() {
        _activeSubFlow.value = null
    }

    fun onStartQuickListFlow() {
        _activeSubFlow.value = PantrySubFlow.QuickList
    }

    fun onBarcodeScanned(ean: String) {
        viewModelScope.launch {
            val cleanEan = ean.filter { it.isDigit() }
            when (val status = checkEanStatusUseCase(cleanEan)) {
                is EanStatus.Found -> {
                    // Produto existe: transiciona direto para configurar quantidade e validade
                    _activeSubFlow.value = PantrySubFlow.AddItemDetails(status.product.toUiModel())
                }

                is EanStatus.NotFound, is EanStatus.InvalidFormat -> {
                    // Produto não existe: abre o formulário de cadastro com o EAN lido
                    _activeSubFlow.value = PantrySubFlow.CreateCatalogProduct(initialEan = cleanEan.takeIf { it.isNotBlank() })
                }

                EanStatus.Empty -> Unit
            }
        }
    }

    fun onProductCreatedFromCatalog(product: CatalogProduct) {
        // Produto recém-criado pelo cadastro: retoma o fluxo de despensa sem perder o foco
        _activeSubFlow.value = PantrySubFlow.AddItemDetails(product.toUiModel())
    }

    fun onConfirmAddPantryItem(
        productId: String,
        quantity: Double,
        expirationDate: Long?,
        batchNumber: String?,
    ) {
        viewModelScope.launch {
            val result = addPantryItemUseCase(
                productId = productId,
                quantity = quantity,
                expirationDate = expirationDate,
                batchNumber = batchNumber,
            )

            result.onSuccess {
                _activeSubFlow.value = null
                notificationManager.showSuccess(UiText.DynamicString("Item adicionado à despensa com sucesso!"))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao adicionar item: ${error.message}", error)
                notificationManager.showError(UiText.DynamicString("Erro ao salvar na despensa: ${error.message}"))
            }
        }
    }

    private fun aggregatePantryItems(items: List<PantryItemWithCategory>): List<PantryItemUiModel> {
        val now = Clock.System.now().toEpochMilliseconds()
        return items
            .groupBy { it.pantryItem.productId }
            .map { (productId, groupedItems) ->
                val firstItem = groupedItems.first()
                val totalQuantity = groupedItems.sumOf { it.pantryItem.quantity }
                val closestExpirationDate = groupedItems
                    .mapNotNull { it.pantryItem.expirationDate }
                    .minOrNull()
                val isExpired = groupedItems.any { item ->
                    item.pantryItem.expirationDate?.let { exp -> exp < now } ?: false
                }

                PantryItemUiModel(
                    id = productId,
                    name = firstItem.name,
                    category = firstItem.category,
                    brand = null,
                    quantity = totalQuantity,
                    measureUnit = CoreConstants.Product.DEFAULT_MEASURE_UNIT,
                    netWeight = CoreConstants.Product.DEFAULT_NET_WEIGHT,
                    expirationDate = closestExpirationDate,
                    isExpired = isExpired,
                )
            }
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }
}
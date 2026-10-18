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
import com.bitlabbr.minhadespensa.core.domain.usecase.ImportInvoiceToPantryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ParseInvoiceQrCodeUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.matchesSearchQuery
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.toUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.*
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import minhadespensa.uisystem.generated.resources.*

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val catalogRepository: CatalogRepository,
    private val checkEanStatusUseCase: CheckEanStatusUseCase,
    private val addPantryItemUseCase: AddPantryItemUseCase,
    private val parseInvoiceQrCodeUseCase: ParseInvoiceQrCodeUseCase,
    private val importInvoiceToPantryUseCase: ImportInvoiceToPantryUseCase,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager,
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _activeSubFlow = MutableStateFlow<PantrySubFlow?>(null)

    val uiState: StateFlow<PantryUiState> = combine(
        pantryRepository.getAllActivePantryItemsWithCategory(),
        pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS),
        combine(
            catalogRepository.getAllActiveProducts(),
            catalogRepository.getCategories(),
        ) { products, categories -> products to categories },
        combine(_searchQuery, _selectedCategory) { query, category -> query to category },
        _activeSubFlow,
    ) { allItems, expiringItems, (catalogProducts, repoCategories), (query, selectedCategory), subFlow ->

        val catalogMap = catalogProducts.associateBy { it.id }
        val allUiItems = aggregatePantryItems(allItems, catalogMap)
        val isPantryEmpty = allUiItems.isEmpty()

        logger.d(
            TAG,
            "uiState recomputed: allItems=${allItems.size}, allUiItems=${allUiItems.size}, expiring=${expiringItems.size}, query='$query', category=$selectedCategory"
        )

        val baseCategories = if (repoCategories.isNotEmpty()) {
            repoCategories.map { it.trim() }
        } else {
            CoreConstants.CatalogCategories.DEFAULT_CATEGORIES
        }

        val dynamicCategories =
            (baseCategories + allUiItems.map { it.category.trim() })
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

        val filteredProducts = allUiItems.filter { item ->
            val matchesCategory = selectedCategory == null ||
                    item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.name.matchesSearchQuery(query) ||
                    (item.brand?.matchesSearchQuery(query) == true) ||
                    "${item.name} ${item.brand.orEmpty()}".matchesSearchQuery(query)

            matchesCategory && matchesQuery
        }

        val searchResults = if (query.isBlank()) {
            emptyList()
        } else {
            allUiItems.filter { item ->
                item.name.matchesSearchQuery(query) ||
                        (item.brand?.matchesSearchQuery(query) == true) ||
                        "${item.name} ${item.brand.orEmpty()}".matchesSearchQuery(query)
            }
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
            expiringPantryItems = aggregatePantryItems(expiringItems, catalogMap).sortedBy { it.expirationDate ?: Long.MAX_VALUE },
            searchResults = searchResults,
            availableCatalogProducts = catalogProducts,
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

    fun getProductImage(productId: String): Flow<ByteArray?> {
        return catalogRepository.getProductImage(productId)
            .catch { error ->
                logger.e(TAG, "Error while loading image for productId:$productId: ${error.message}", error)
                emit(null)
            }
    }

    // --- MÁQUINA DE ESTADOS DOS SUBFLUXOS ---

    fun onStartAddItemFlow() {
        _activeSubFlow.value = PantrySubFlow.AddItemOptions
    }

    fun onStartSearchCatalogFlow() {
        _activeSubFlow.value = PantrySubFlow.SearchCatalog
    }

    fun onCatalogProductSelected(product: CatalogProduct) {
        _activeSubFlow.value = PantrySubFlow.AddItemDetails(product.toUiModel())
    }

    fun onStartScanFlow() {
        _activeSubFlow.value = PantrySubFlow.BarcodeScanner
    }

    fun onStartManualRegisterFlow(ean: String? = null) {
        _activeSubFlow.value = PantrySubFlow.CreateCatalogProduct(initialEan = ean)
    }

    fun onDismissSubFlow() {
        _activeSubFlow.value = null
    }

    fun onStartInvoiceScanFlow() {
        _activeSubFlow.value = PantrySubFlow.InvoiceScanner
    }

    fun onInvoiceQrCodeScanned(qrCodeContent: String) {
        viewModelScope.launch {
            val result = parseInvoiceQrCodeUseCase(qrCodeContent)
            result.onSuccess { invoice ->
                logger.d(TAG, "onInvoiceQrCodeScanned: successfully parsed invoice from '${invoice.storeName}' with ${invoice.items.size} items")
                _activeSubFlow.value = PantrySubFlow.InvoiceReview(invoice)
            }.onFailure { error ->
                logger.e(TAG, "Falha ao consultar nota fiscal: ${error.message}", error)
                _activeSubFlow.value = null
                val userMessage = error.message?.takeIf { it.isNotBlank() }
                if (userMessage != null) {
                    notificationManager.showError(UiText.DynamicString(userMessage))
                } else {
                    notificationManager.showError(UiText.Resource(Res.string.invoice_parse_error))
                }
            }
        }
    }

    fun onToggleInvoiceItem(itemId: String, isSelected: Boolean) {
        val currentSubFlow = _activeSubFlow.value
        if (currentSubFlow is PantrySubFlow.InvoiceReview) {
            val updatedItems = currentSubFlow.invoice.items.map { item ->
                if (item.id == itemId) item.copy(isSelected = isSelected) else item
            }
            _activeSubFlow.value = currentSubFlow.copy(
                invoice = currentSubFlow.invoice.copy(items = updatedItems)
            )
        }
    }

    fun onConfirmInvoiceImport() {
        val currentSubFlow = _activeSubFlow.value as? PantrySubFlow.InvoiceReview ?: return
        val selectedItems = currentSubFlow.invoice.items.filter { it.isSelected }
        if (selectedItems.isEmpty()) {
            notificationManager.showWarning(UiText.Resource(Res.string.invoice_no_items_selected))
            return
        }

        _activeSubFlow.value = currentSubFlow.copy(isImporting = true)
        viewModelScope.launch {
            val result = importInvoiceToPantryUseCase(
                storeName = currentSubFlow.invoice.storeName,
                items = selectedItems,
            )
            result.onSuccess { count ->
                logger.d(TAG, "onConfirmInvoiceImport: successfully imported $count items")
                _activeSubFlow.value = null
                notificationManager.showSuccess(
                    UiText.Resource(Res.string.invoice_import_success, listOf(count))
                )
            }.onFailure { error ->
                logger.e(TAG, "Falha ao importar nota fiscal: ${error.message}", error)
                _activeSubFlow.value = currentSubFlow.copy(isImporting = false)
                notificationManager.showError(
                    UiText.Resource(Res.string.invoice_import_error)
                )
            }
        }
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
        unitPriceInCents: Long? = null,
    ) {
        logger.d(TAG, "onConfirmAddPantryItem: productId=$productId, quantity=$quantity, unitPriceInCents=$unitPriceInCents")
        viewModelScope.launch {
            val result = addPantryItemUseCase(
                productId = productId,
                quantity = quantity,
                expirationDate = expirationDate,
                batchNumber = batchNumber,
                unitPriceInCents = unitPriceInCents,
            )

            result.onSuccess {
                logger.d(TAG, "onConfirmAddPantryItem: successfully added item with productId=$productId")
                _activeSubFlow.value = null
                notificationManager.showSuccess(UiText.Resource(Res.string.pantry_item_added_success))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao adicionar item: ${error.message}", error)
                notificationManager.showError(UiText.DynamicString("Erro ao salvar na despensa: ${error.message}"))
            }
        }
    }

    private fun aggregatePantryItems(
        items: List<PantryItemWithCategory>,
        catalogMap: Map<String, CatalogProduct> = emptyMap(),
    ): List<PantryItemUiModel> {
        val now = Clock.System.now().toEpochMilliseconds()
        return items
            .filter { it.pantryItem.quantity > 0.0 }
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
                val product = catalogMap[productId]

                PantryItemUiModel(
                    id = productId,
                    name = firstItem.name,
                    category = firstItem.category,
                    brand = product?.brand,
                    quantity = totalQuantity,
                    measureUnit = product?.measureUnit ?: CoreConstants.Product.DEFAULT_MEASURE_UNIT,
                    netWeight = product?.netWeight ?: CoreConstants.Product.DEFAULT_NET_WEIGHT,
                    expirationDate = closestExpirationDate,
                    isExpired = isExpired,
                )
            }
            .filter { it.quantity > 0.0 }
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }
}
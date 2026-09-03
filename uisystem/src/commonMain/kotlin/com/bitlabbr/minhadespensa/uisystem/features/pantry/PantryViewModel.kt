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
import com.bitlabbr.minhadespensa.core.domain.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.domain.usecase.EanStatus
import com.bitlabbr.minhadespensa.core.domain.model.CatalogCategories
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.toUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryFilterSubState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryListSubState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.model.PantryUiState
import com.bitlabbr.minhadespensa.uisystem.features.pantry.widgets.add.PantryItemFormState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val catalogRepository: CatalogRepository,
    private val checkEanStatusUseCase: CheckEanStatusUseCase,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager,
) : ViewModel() {

    private val TAG = "PantryViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val pantryIdToProductIdMap = mutableMapOf<String, String>()

    private val _itemFormState = MutableStateFlow(PantryItemFormState())
    val itemFormState: StateFlow<PantryItemFormState> = _itemFormState.asStateFlow()

    private val _isItemSheetOpen = MutableStateFlow(false)
    val isItemSheetOpen: StateFlow<Boolean> = _isItemSheetOpen.asStateFlow()

    private var startSheetWithScanner = false
    val isStartWithScanner: Boolean get() = startSheetWithScanner

    // Canal reativo para busca de EAN
    private val _eanFlow = MutableStateFlow("")

    val uiState: StateFlow<PantryUiState> = combine(
        pantryRepository.getAllActivePantryItemsWithCategory(),
        pantryRepository.getExpiringPantryItems(EXPIRATION_THRESHOLD_DAYS),
        _searchQuery,
        _selectedCategory,
    ) { allActiveWithCategory, expiringItems, searchQuery, selectedCategory ->

        allActiveWithCategory.forEach { item ->
            pantryIdToProductIdMap[item.pantryItem.id] = item.pantryItem.productId
        }

        val allUiItems = allActiveWithCategory.map { it.toPantryItemUiModel() }
        val isPantryEmpty = allUiItems.isEmpty()

        val dynamicCategories = (CatalogCategories.DEFAULT_CATEGORIES + allUiItems.map { it.category.trim() })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val filteredProducts = allUiItems.filter { item ->
            val matchesCategory = selectedCategory == null ||
                    item.category.equals(selectedCategory, ignoreCase = true)

            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesQuery
        }

        val searchResults = if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allUiItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
            expiringPantryItems = expiringItems.map { it.toPantryItemUiModel() },
            searchResults = searchResults,
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

    init {
        // Observador reativo de EAN com debounce centralizado
        viewModelScope.observeEanChanges(
            eanFlow = _eanFlow,
            checkEanUseCase = checkEanStatusUseCase,
            onChecking = {
                _itemFormState.update { it.copy(isSearchingCatalog = true, productNotFound = false) }
            },
            onResult = { status ->
                _itemFormState.update { state ->
                    when (status) {
                        is EanStatus.Found -> state.copy(
                            selectedProduct = status.product.toUiModel(),
                            productNotFound = false,
                            isSearchingCatalog = false,
                        )
                        is EanStatus.NotFound -> state.copy(
                            selectedProduct = null,
                            productNotFound = true,
                            isSearchingCatalog = false,
                        )
                        is EanStatus.InvalidFormat,
                        is EanStatus.Empty -> state.copy(
                            selectedProduct = null,
                            productNotFound = false,
                            isSearchingCatalog = false,
                        )
                    }
                }
            },
        )
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchResultSelected(item: PantryItemUiModel) {
        _searchQuery.value = item.name
        getPantryItemDetails(item.id)
    }

    fun getProductImage(id: String): Flow<ByteArray?> {
        val targetProductId = pantryIdToProductIdMap[id] ?: id
        return catalogRepository.getProductImage(targetProductId)
            .catch { error ->
                logger.e(TAG, "Error while loading the product image $id: ${error.message}", error)
                emit(null)
            }
    }

    fun getPantryItemDetails(pantryItemId: String) {
        viewModelScope.launch {
            pantryRepository.getPantryItemWithCategoryByID(pantryItemId)
                .map { it?.toPantryItemUiModel() }
                .collect { item ->
                    // Tratamento do item selecionado
                }
        }
    }

    fun openAddPantryItemSheet(startWithScanner: Boolean = false) {
        startSheetWithScanner = startWithScanner
        _eanFlow.value = ""
        _itemFormState.value = PantryItemFormState()
        _isItemSheetOpen.value = true
    }

    fun closeAddPantryItemSheet() {
        _isItemSheetOpen.value = false
        _eanFlow.value = ""
        _itemFormState.value = PantryItemFormState()
    }

    fun onItemFormChange(updated: PantryItemFormState) {
        _itemFormState.value = updated
        if (updated.ean != _eanFlow.value) {
            _eanFlow.value = updated.ean
        }
    }

    fun onEanScannedOrTyped(ean: String) {
        _itemFormState.update { it.copy(ean = ean) }
        _eanFlow.value = ean
    }

    @OptIn(ExperimentalUuidApi::class)
    fun savePantryItem() {
        viewModelScope.launch {
            val form = _itemFormState.value
            val product = form.selectedProduct ?: return@launch
            if (!form.isFormValid) return@launch

            _itemFormState.update { it.copy(isSaving = true) }
            try {
                val qty = form.quantity.replace(',', '.').toDoubleOrNull() ?: 1.0
                val now = getCurrentTime()

                val pantryItem = PantryItem(
                    id = Uuid.random().toString(),
                    productId = product.id,
                    quantity = qty,
                    expirationDate = form.expirationDate,
                    batchNumber = form.batchNumber.trim().takeIf { it.isNotBlank() },
                    updatedAt = now,
                    isDeleted = false,
                )

                pantryRepository.insertPantryItem(pantryItem)
                closeAddPantryItemSheet()

                notificationManager.showSuccess(UiText.DynamicString("${product.name} adicionado à despensa!"))
            } catch (e: Exception) {
                logger.e(TAG, "Falha ao adicionar item na despensa: ${e.message}", e)
                _itemFormState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun PantryItemWithCategory.toPantryItemUiModel(): PantryItemUiModel {
        val now = Clock.System.now().toEpochMilliseconds()
        val isExpired = this.pantryItem.expirationDate?.let { it < now } ?: false

        return PantryItemUiModel(
            id = this.pantryItem.id,
            name = this.name,
            category = this.category,
            brand = null,
            quantity = this.pantryItem.quantity,
            measureUnit = MeasureUnit.UNIT,
            netWeight = 0,
            expirationDate = this.pantryItem.expirationDate,
            isExpired = isExpired,
        )
    }

    companion object {
        private const val EXPIRATION_THRESHOLD_DAYS = 7
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun CoroutineScope.observeEanChanges(
    eanFlow: Flow<String>,
    checkEanUseCase: CheckEanStatusUseCase,
    onChecking: () -> Unit,
    onResult: (EanStatus) -> Unit,
): Job = eanFlow
    .map { it.trim() }
    .distinctUntilChanged()
    .onEach { if (it.isNotBlank()) onChecking() }
    .debounce(350.milliseconds)
    .flatMapLatest { ean ->
        flow { emit(checkEanUseCase(ean)) }
    }
    .onEach { status -> onResult(status) }
    .launchIn(this)
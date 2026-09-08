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

package com.bitlabbr.minhadespensa.uisystem.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.*
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import minhadespensa.uisystem.generated.resources.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CatalogViewModel(
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager
) : ViewModel() {

    private val TAG = "CatalogViewModel"
    private var eanValidationJob: Job? = null

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _formState = MutableStateFlow(ProductFormState())
    private val _isFormOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<UiText?>(null)
    val userMessage: StateFlow<UiText?> = _userMessage.asStateFlow()

    val uiState: StateFlow<CatalogUiState> = combine(
        catalogRepository.getAllActives(),
        _searchQuery,
        _selectedCategory,
        _formState,
        _isFormOpen,
    ) { products, query, selectedCategory, form, isFormOpen ->

        val uiProducts = products.map { it.toUiModel() }
        val isCatalogEmpty = products.isEmpty()
        val dynamicCategories = (CoreConstants.CatalogCategories.DEFAULT_CATEGORIES + products.map { it.category.trim() })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val filteredList = uiProducts.filter { item ->
            val matchesCategory = selectedCategory == null ||
                    item.category.equals(selectedCategory, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    (item.brand?.contains(query, ignoreCase = true) == true)

            matchesCategory && matchesQuery
        }

        val searchSuggestions = if (query.isBlank()) {
            emptyList()
        } else {
            uiProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.brand?.contains(query, ignoreCase = true) == true)
            }
        }

        CatalogUiState(
            searchState = SearchSubState(
                query = query,
                searchResults = searchSuggestions,
                isSearching = query.isNotBlank(),
            ),
            filterState = CategoryFilterSubState(
                availableCategories = dynamicCategories,
                selectedCategory = selectedCategory,
            ),
            listState = ProductListSubState(
                products = filteredList,
                isLoading = false,
                isCatalogEmpty = isCatalogEmpty,
                error = null,
            ),
            formState = form.copy(availableCategories = dynamicCategories),
            isFormOpen = isFormOpen,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState(listState = ProductListSubState(isLoading = true)),
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun onProductSelected(product: CatalogProductUiModel) {
        logger.d(TAG, "Produto selecionado: ${product.name} (${product.id})")
    }

    fun openAddProductSheet() {
        eanValidationJob?.cancel()
        _formState.value = ProductFormState()
        _isFormOpen.value = true
    }

    fun closeAddProductSheet() {
        eanValidationJob?.cancel()
        _isFormOpen.value = false
        _formState.value = ProductFormState()
    }

    fun onFormChange(updatedForm: ProductFormState) {
        val current = _formState.value
        val isEanChanged = updatedForm.ean != current.ean
        val preservedEanError = if (isEanChanged) null else current.eanError
        val preservedIsChecking = if (isEanChanged) false else current.isCheckingEan
        val validatedState = updatedForm.copy(
            nameError = when {
                updatedForm.name.isBlank() && current.name.isNotEmpty() ->
                    UiText.Resource(Res.string.error_name_required)

                updatedForm.name.length > CoreConstants.Product.NAME_MAX_LENGTH ->
                    UiText.Resource(Res.string.error_name_max_length, listOf(CoreConstants.Product.NAME_MAX_LENGTH))

                else -> null
            },
            netWeightError = when {
                updatedForm.netWeight.isNotBlank() &&
                        updatedForm.netWeight.replace(',', '.').toDoubleOrNull() == null ->
                    UiText.Resource(Res.string.error_invalid_number)

                else -> null
            },
            eanError = preservedEanError,
            isCheckingEan = preservedIsChecking,
        )

        _formState.value = validatedState

        if (isEanChanged) {
            validateEanDebounced(updatedForm.ean.trim())
        }
    }

    private fun validateEanDebounced(ean: String) {
        eanValidationJob?.cancel()

        if (ean.isBlank()) {
            _formState.update { it.copy(eanError = null, isCheckingEan = false) }
            return
        }

        if (ean.length !in CoreConstants.Product.EAN_VALID_LENGTHS) {
            _formState.update {
                it.copy(
                    eanError = UiText.Resource(Res.string.error_ean_invalid_length),
                    isCheckingEan = false,
                )
            }
            return
        }

        eanValidationJob = viewModelScope.launch {
            _formState.update { it.copy(isCheckingEan = true, eanError = null) }
            delay(350.milliseconds)

            logger.d(TAG, "Checking EAN uniqueness for: $ean")
            val existingProduct = catalogRepository.getProductByEan(ean).firstOrNull()

            _formState.update { state ->
                if (state.ean.trim() == ean) {
                    if (existingProduct != null && !existingProduct.isDeleted) {
                        logger.d(TAG, "Duplicate EAN found: ${existingProduct.name}")
                        state.copy(
                            eanError = UiText.Resource(
                                Res.string.error_ean_already_exists,
                                listOf(existingProduct.name)
                            ),
                            isCheckingEan = false,
                        )
                    } else {
                        state.copy(eanError = null, isCheckingEan = false)
                    }
                } else {
                    state
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveProduct() {
        viewModelScope.launch {
            val currentForm = _formState.value
            if (!currentForm.isFormValid) {
                logger.d(TAG, "Trying to save invalid form... skipping")
                return@launch
            }

            _formState.update { it.copy(isSaving = true) }
            try {
                val netWeight = currentForm.netWeight.replace(',', '.').toDoubleOrNull() ?: 1.0
                val unit = currentForm.measureUnit
                val category = currentForm.category.ifBlank { CoreConstants.Product.DEFAULT_CATEGORY }

                val newProduct = CatalogProduct(
                    id = Uuid.random().toString(),
                    name = currentForm.name.trim(),
                    brand = currentForm.brand.trim().takeIf { it.isNotBlank() },
                    category = category,
                    measureUnit = unit,
                    netWeight = netWeight,
                    updatedAt = getCurrentTime(),
                    isDeleted = false,
                    manuallyAdded = true,
                    ean = currentForm.ean.trim().takeIf { it.isNotBlank() },
                )

                catalogRepository.insertProduct(newProduct, currentForm.imageBytes)
                _selectedCategory.value = currentForm.category
                _searchQuery.value = ""

                closeAddProductSheet()
            } catch (e: Exception) {
                logger.e(TAG, "Error while saving product: ${e.message}", e)
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun getAllActives(): Flow<List<CatalogProduct>> {
        return catalogRepository.getAllActives()
    }

    fun getProductImage(productId: String): Flow<ByteArray?> {
        return catalogRepository.getProductImage(productId)
            .catch { error ->
                logger.e(TAG, "Error while loading the product image: $productId: ${error.message}", error)
                emit(null)
            }
    }
}
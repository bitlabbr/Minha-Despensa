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

package com.bitlabbr.minhadespensa.uisystem.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemConsumption
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.*
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.toUiModel
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.ProductFormState
import com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register.validateFormFields
import com.bitlabbr.minhadespensa.uisystem.features.details.model.*
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import minhadespensa.uisystem.generated.resources.*
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

class ProductDetailsViewModel(
    private val catalogRepository: CatalogRepository,
    private val pantryRepository: PantryRepository,
    private val priceRepository: PriceRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val addCatalogItemToShoppingListUseCase: AddCatalogItemToShoppingListUseCase,
    private val addPantryItemUseCase: AddPantryItemUseCase,
    private val logger: AppLogger,
    private val notificationManager: AppNotificationManager,
    private val clock: Clock = Clock.System,
    private val saveCatalogProductUseCase: SaveCatalogProductUseCase = SaveCatalogProductUseCase(catalogRepository),
    private val checkEanStatusUseCase: CheckEanStatusUseCase = CheckEanStatusUseCase(catalogRepository),
) : ViewModel() {

    private val TAG = "ProductDetailsViewModel"
    private var eanValidationJob: Job? = null

    private val _productId = MutableStateFlow<String?>(null)
    private val _isEditing = MutableStateFlow(false)
    private val _editForm = MutableStateFlow(ProductFormState())
    private val _activeSubFlow = MutableStateFlow<ProductDetailsSubFlow?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProductDetailsUiState> = _productId
        .filterNotNull()
        .flatMapLatest { id ->
            val baseProductFlow = combine(
                catalogRepository.getProductById(id),
                catalogRepository.getProductImage(id),
                catalogRepository.getAllActiveProducts(),
            ) { product, imageBytes, allProducts ->
                Triple(product, imageBytes, allProducts)
            }

            val priceAndPantryFlow = combine(
                pantryRepository.getPantryItemsByProductId(id),
                priceRepository.getPriceHistoryByProductId(id),
                priceRepository.getLatestPriceForProductId(id),
            ) { pantryItems, priceHistory, latestPriceEntry ->
                Triple(pantryItems, priceHistory, latestPriceEntry)
            }

            val productDataFlow = combine(
                baseProductFlow,
                priceAndPantryFlow,
            ) { (product, imageBytes, allProducts), (pantryItems, priceHistory, latestPriceEntry) ->
                ProductCombinedData(
                    product = product,
                    imageBytes = imageBytes,
                    allProducts = allProducts,
                    pantryItems = pantryItems,
                    priceHistory = priceHistory,
                    latestPriceEntry = latestPriceEntry,
                )
            }

            combine(
                productDataFlow,
                _isEditing,
                _editForm,
                shoppingListRepository.getAllActiveShoppingLists(),
                _activeSubFlow,
            ) { productData, isEditing, editForm, shoppingLists, subFlow ->
                buildUiState(
                    product = productData.product,
                    imageBytes = productData.imageBytes,
                    allProducts = productData.allProducts,
                    isEditing = isEditing,
                    editForm = editForm,
                    pantryItems = productData.pantryItems,
                    priceHistory = productData.priceHistory,
                    latestPriceEntry = productData.latestPriceEntry,
                    shoppingLists = shoppingLists,
                    subFlow = subFlow,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductDetailsUiState(isLoading = true),
        )

    fun loadProduct(productId: String) {
        if (_productId.value != productId) {
            _productId.value = productId
            _isEditing.value = false
            _editForm.value = ProductFormState()
        }
    }

    fun onStartEdit() {
        val current = uiState.value.product ?: return
        val available = uiState.value.availableCategories
        val formattedWeight = if (current.netWeight % 1.0 == 0.0) {
            current.netWeight.toLong().toString()
        } else {
            current.netWeight.toString().replace('.', ',')
        }
        _editForm.value = ProductFormState(
            name = current.name,
            brand = current.brand.orEmpty(),
            category = current.category,
            measureUnit = current.measureUnit,
            netWeight = formattedWeight,
            ean = current.ean.orEmpty(),
            notes = current.notes.orEmpty(),
            imageBytes = current.imageBytes,
            availableCategories = available,
        )
        _isEditing.value = true
    }

    fun onCancelEdit() {
        _isEditing.value = false
        _editForm.value = ProductFormState()
        eanValidationJob?.cancel()
    }

    fun onFormChange(updated: ProductFormState) {
        val current = _editForm.value
        val validated = validateFormFields(current, updated)
        _editForm.value = validated

        if (updated.ean != current.ean) {
            eanValidationJob?.cancel()
            val trimmedEan = updated.ean.trim()
            if (trimmedEan.isBlank()) {
                _editForm.value = _editForm.value.copy(eanError = null, isCheckingEan = false)
            } else if (trimmedEan == uiState.value.product?.ean) {
                _editForm.value = _editForm.value.copy(eanError = null, isCheckingEan = false)
            } else {
                _editForm.value = _editForm.value.copy(isCheckingEan = true)
                eanValidationJob = viewModelScope.launch {
                    delay(300.milliseconds)
                    val status = checkEanStatusUseCase(trimmedEan)
                    val currentId = _productId.value
                    val error = when (status) {
                        is EanStatus.Found -> {
                            if (status.product.id == currentId) null
                            else UiText.Resource(Res.string.error_ean_already_exists, listOf(status.product.name))
                        }
                        is EanStatus.InvalidFormat -> UiText.Resource(Res.string.error_ean_invalid_length)
                        else -> null
                    }
                    _editForm.value = _editForm.value.copy(eanError = error, isCheckingEan = false)
                }
            }
        }
    }

    fun onSaveProduct() {
        val currentProduct = uiState.value.product ?: return
        val form = _editForm.value
        val weightNumber = form.netWeight.replace(',', '.').toDoubleOrNull()
        if (form.name.isBlank()) {
            _editForm.value = form.copy(nameError = UiText.Resource(Res.string.error_name_required))
            return
        }
        if (weightNumber == null || weightNumber <= 0.0) {
            _editForm.value = form.copy(netWeightError = UiText.Resource(Res.string.error_invalid_number))
            return
        }
        if (form.eanError != null) return

        _editForm.value = form.copy(isSaving = true)
        viewModelScope.launch {
            val result = saveCatalogProductUseCase(
                id = currentProduct.id,
                name = form.name,
                brand = form.brand.takeIf { it.isNotBlank() },
                category = form.category,
                measureUnit = form.measureUnit,
                netWeight = weightNumber,
                ean = form.ean.takeIf { it.isNotBlank() },
                imageBytes = form.imageBytes,
                notes = form.notes.takeIf { it.isNotBlank() },
                isEditing = true,
            )
            result.onSuccess {
                _isEditing.value = false
                _editForm.value = ProductFormState()
                notificationManager.showSuccess(UiText.Resource(Res.string.product_details_save_success))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao salvar produto editado: ${error.message}", error)
                _editForm.value = _editForm.value.copy(
                    isSaving = false,
                    errorMessage = error.message,
                )
                notificationManager.showError(UiText.DynamicString(error.message ?: "Erro ao salvar produto"))
            }
        }
    }

    fun onOpenConsumeDialog() {
        if (uiState.value.pantryStock?.hasStock != true) return
        _activeSubFlow.value = ProductDetailsSubFlow.ConsumeStock
    }

    fun onOpenAddStockSheet() {
        val currentProduct = uiState.value.product ?: return
        val catalogUiModel = CatalogProduct(
            id = currentProduct.id,
            name = currentProduct.name,
            brand = currentProduct.brand,
            category = currentProduct.category,
            ean = currentProduct.ean,
            measureUnit = currentProduct.measureUnit,
            netWeight = currentProduct.netWeight,
            notes = currentProduct.notes,
            updatedAt = clock.now().toEpochMilliseconds(),
        ).toUiModel()
        _activeSubFlow.value = ProductDetailsSubFlow.AddPantryStock(catalogUiModel)
    }

    fun onOpenAddToListDialog() {
        _activeSubFlow.value = ProductDetailsSubFlow.SelectShoppingList
    }

    fun onDismissSubFlow() {
        _activeSubFlow.value = null
    }

    fun onConsumeStock(batchId: String? = null, quantity: Double = 1.0) {
        val pantryStock = uiState.value.pantryStock ?: return
        if (!pantryStock.hasStock || quantity <= 0.0) return
        val currentBatches = pantryStock.batches
        if (currentBatches.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                if (batchId != null) {
                    val targetBatch = currentBatches.find { it.id == batchId } ?: return@launch
                    val actualQty = minOf(quantity, targetBatch.quantity)
                    if (actualQty > 0.0) {
                        pantryRepository.consumePantryItem(targetBatch.id, actualQty)
                    }
                } else {
                    var remaining = quantity
                    val consumptions = mutableListOf<PantryItemConsumption>()
                    for (batch in currentBatches) {
                        if (remaining <= 0.0) break
                        val take = minOf(remaining, batch.quantity)
                        if (take > 0.0) {
                            consumptions.add(
                                PantryItemConsumption(
                                    pantryItemId = batch.id,
                                    quantityToConsume = take,
                                )
                            )
                            remaining -= take
                        }
                    }
                    if (consumptions.isNotEmpty()) {
                        pantryRepository.consumeBatch(consumptions)
                    }
                }
            }.onSuccess {
                _activeSubFlow.value = null
                notificationManager.showSuccess(UiText.Resource(Res.string.product_details_pantry_consume_success))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao consumir item: ${error.message}", error)
                notificationManager.showError(UiText.Resource(Res.string.product_details_pantry_consume_error, listOf(error.message ?: "")))
            }
        }
    }

    fun onAddPantryStock(quantity: Double, expirationDate: Long?, batchNumber: String?) {
        val prodId = _productId.value ?: return
        viewModelScope.launch {
            addPantryItemUseCase(
                productId = prodId,
                quantity = quantity,
                expirationDate = expirationDate,
                batchNumber = batchNumber,
            ).onSuccess {
                _activeSubFlow.value = null
                notificationManager.showSuccess(UiText.Resource(Res.string.product_details_add_pantry_success))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao adicionar estoque: ${error.message}", error)
                notificationManager.showError(UiText.Resource(Res.string.product_details_add_pantry_error, listOf(error.message ?: "")))
            }
        }
    }

    fun onAddToShoppingList(listId: String, quantity: Double = 1.0) {
        val prodId = _productId.value ?: return
        viewModelScope.launch {
            addCatalogItemToShoppingListUseCase(
                listId = listId,
                productId = prodId,
                quantity = quantity,
            ).onSuccess {
                _activeSubFlow.value = null
                notificationManager.showSuccess(UiText.Resource(Res.string.product_details_add_to_list_success))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao adicionar à lista: ${error.message}", error)
                notificationManager.showError(UiText.Resource(Res.string.product_details_add_to_list_error, listOf(error.message ?: "")))
            }
        }
    }

    private fun buildUiState(
        product: CatalogProduct?,
        imageBytes: ByteArray?,
        allProducts: List<CatalogProduct>,
        isEditing: Boolean,
        editForm: ProductFormState,
        pantryItems: List<com.bitlabbr.minhadespensa.core.domain.model.PantryItem>,
        priceHistory: List<com.bitlabbr.minhadespensa.core.domain.model.PriceEntry>,
        latestPriceEntry: com.bitlabbr.minhadespensa.core.domain.model.PriceEntry?,
        shoppingLists: List<com.bitlabbr.minhadespensa.core.domain.model.ShoppingList>,
        subFlow: ProductDetailsSubFlow?,
    ): ProductDetailsUiState {
        if (product == null) {
            return ProductDetailsUiState(
                isLoading = false,
                product = null,
                error = UiText.Resource(Res.string.product_details_not_found),
            )
        }

        val dynamicCategories = (CoreConstants.CatalogCategories.DEFAULT_CATEGORIES + allProducts.map { it.category.trim() })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val productInfo = ProductDetailsInfoUiModel(
            id = product.id,
            name = product.name,
            brand = product.brand,
            category = product.category,
            ean = product.ean,
            measureUnit = product.measureUnit,
            netWeight = product.netWeight,
            notes = product.notes,
            imageBytes = imageBytes,
        )

        val now = clock.now().toEpochMilliseconds()
        val activePantryItems = pantryItems.filter { !it.isDeleted && it.quantity > 0.0 }
        val totalQuantity = activePantryItems.sumOf { it.quantity }

        val batches = activePantryItems.map { batch ->
            val isExpired = batch.expirationDate?.let { it < now } ?: false
            val daysUntil = batch.expirationDate?.let { (it - now) / (1000L * 60 * 60 * 24) }
            PantryBatchUiModel(
                id = batch.id,
                quantity = batch.quantity,
                expirationDate = batch.expirationDate,
                batchNumber = batch.batchNumber,
                isExpired = isExpired,
                daysUntilExpiration = daysUntil,
            )
        }.sortedBy { it.expirationDate ?: Long.MAX_VALUE }

        val closestExpirationDate = batches.mapNotNull { it.expirationDate }.minOrNull()
        val isExpired = batches.any { it.isExpired }

        val pantryStock = ProductPantryStockUiModel(
            totalQuantity = totalQuantity,
            measureUnit = if (product.measureUnit == MeasureUnit.PACKAGE) MeasureUnit.PACKAGE else MeasureUnit.UNIT,
            closestExpirationDate = closestExpirationDate,
            isExpired = isExpired,
            batches = batches,
            hasStock = totalQuantity > 0.0,
        )

        val activePrices = priceHistory.filter { !it.isDeleted }
            .map {
                ProductPriceEntryUiModel(
                    id = it.id,
                    priceInCents = it.priceInCents,
                    storeName = it.storeName.orEmpty(),
                    date = it.updatedAt,
                )
            }
            .sortedByDescending { it.date }

        val latestPrice = latestPriceEntry?.priceInCents ?: activePrices.firstOrNull()?.priceInCents
        val averagePrice = if (activePrices.isNotEmpty()) {
            activePrices.map { it.priceInCents }.average().roundToLong()
        } else {
            null
        }
        val lowestPrice = activePrices.minOfOrNull { it.priceInCents }
        val highestPrice = activePrices.maxOfOrNull { it.priceInCents }

        val priceDifference = if (highestPrice != null && lowestPrice != null && highestPrice > lowestPrice) {
            highestPrice - lowestPrice
        } else null

        val priceVariationPercentage = if (lowestPrice != null && highestPrice != null && lowestPrice > 0L) {
            ((highestPrice - lowestPrice).toDouble() / lowestPrice.toDouble()) * 100.0
        } else null

        val activeLists = shoppingLists.filter { !it.isDeleted }
            .map {
                ShoppingListOptionUiModel(
                    id = it.id,
                    name = it.name,
                    itemsCount = it.totalActiveItems,
                )
            }

        val effectiveForm = if (isEditing) {
            if (editForm.availableCategories.isEmpty()) {
                editForm.copy(availableCategories = dynamicCategories)
            } else {
                editForm
            }
        } else {
            editForm
        }

        return ProductDetailsUiState(
            isLoading = false,
            isEditing = isEditing,
            product = productInfo,
            editForm = effectiveForm,
            availableCategories = dynamicCategories,
            pantryStock = pantryStock,
            priceHistory = activePrices,
            latestPrice = latestPrice,
            averagePrice = averagePrice,
            lowestPrice = lowestPrice,
            highestPrice = highestPrice,
            priceVariationPercentage = priceVariationPercentage,
            priceDifference = priceDifference,
            activeShoppingLists = activeLists,
            activeSubFlow = subFlow,
            error = null,
        )
    }
}

private data class ProductCombinedData(
    val product: CatalogProduct?,
    val imageBytes: ByteArray?,
    val allProducts: List<CatalogProduct>,
    val pantryItems: List<com.bitlabbr.minhadespensa.core.domain.model.PantryItem>,
    val priceHistory: List<com.bitlabbr.minhadespensa.core.domain.model.PriceEntry>,
    val latestPriceEntry: com.bitlabbr.minhadespensa.core.domain.model.PriceEntry?,
)

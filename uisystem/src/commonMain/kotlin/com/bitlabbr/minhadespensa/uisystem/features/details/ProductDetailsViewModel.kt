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
import com.bitlabbr.minhadespensa.core.domain.usecase.AddCatalogItemToShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.AddPantryItemUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.uisystem.features.catalog.model.toUiModel
import com.bitlabbr.minhadespensa.uisystem.features.details.model.*
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.product_details_add_to_list_success
import minhadespensa.uisystem.generated.resources.product_details_pantry_consume_success
import kotlin.math.roundToLong

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
) : ViewModel() {

    private val TAG = "ProductDetailsViewModel"

    private val _productId = MutableStateFlow<String?>(null)
    private val _activeSubFlow = MutableStateFlow<ProductDetailsSubFlow?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProductDetailsUiState> = _productId
        .filterNotNull()
        .flatMapLatest { id ->
            val productDataFlow = combine(
                catalogRepository.getProductById(id),
                catalogRepository.getProductImage(id),
                pantryRepository.getPantryItemsByProductId(id),
                priceRepository.getPriceHistoryByProductId(id),
                priceRepository.getLatestPriceForProductId(id),
            ) { product, imageBytes, pantryItems, priceHistory, latestPriceEntry ->
                ProductCombinedData(
                    product = product,
                    imageBytes = imageBytes,
                    pantryItems = pantryItems,
                    priceHistory = priceHistory,
                    latestPriceEntry = latestPriceEntry,
                )
            }

            combine(
                productDataFlow,
                shoppingListRepository.getAllActiveShoppingLists(),
                _activeSubFlow,
            ) { productData, shoppingLists, subFlow ->
                buildUiState(
                    product = productData.product,
                    imageBytes = productData.imageBytes,
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
                notificationManager.showError(UiText.DynamicString("Erro ao consumir: ${error.message}"))
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
                notificationManager.showSuccess(UiText.DynamicString("Estoque adicionado com sucesso!"))
            }.onFailure { error ->
                logger.e(TAG, "Falha ao adicionar estoque: ${error.message}", error)
                notificationManager.showError(UiText.DynamicString("Erro ao salvar estoque: ${error.message}"))
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
                notificationManager.showError(UiText.DynamicString("Erro ao adicionar à lista: ${error.message}"))
            }
        }
    }

    private fun buildUiState(
        product: CatalogProduct?,
        imageBytes: ByteArray?,
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
                error = UiText.DynamicString("Produto não encontrado"),
            )
        }

        val productInfo = ProductDetailsInfoUiModel(
            id = product.id,
            name = product.name,
            brand = product.brand,
            category = product.category,
            ean = product.ean,
            measureUnit = product.measureUnit,
            netWeight = product.netWeight,
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
                    storeName = it.storeName ?: "Compra",
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

        val activeLists = shoppingLists.filter { !it.isDeleted }
            .map {
                ShoppingListOptionUiModel(
                    id = it.id,
                    name = it.name,
                    itemsCount = it.totalActiveItems,
                )
            }

        return ProductDetailsUiState(
            isLoading = false,
            product = productInfo,
            pantryStock = pantryStock,
            priceHistory = activePrices,
            latestPrice = latestPrice,
            averagePrice = averagePrice,
            lowestPrice = lowestPrice,
            highestPrice = highestPrice,
            activeShoppingLists = activeLists,
            activeSubFlow = subFlow,
            error = null,
        )
    }
}

private data class ProductCombinedData(
    val product: CatalogProduct?,
    val imageBytes: ByteArray?,
    val pantryItems: List<com.bitlabbr.minhadespensa.core.domain.model.PantryItem>,
    val priceHistory: List<com.bitlabbr.minhadespensa.core.domain.model.PriceEntry>,
    val latestPriceEntry: com.bitlabbr.minhadespensa.core.domain.model.PriceEntry?,
)

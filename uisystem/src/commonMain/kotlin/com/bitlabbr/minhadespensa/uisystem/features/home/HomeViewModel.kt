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

package com.bitlabbr.minhadespensa.uisystem.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MonthlyFinancialSummary
import com.bitlabbr.minhadespensa.core.domain.model.PantryItemWithCategory
import com.bitlabbr.minhadespensa.core.domain.model.PantryMovement
import com.bitlabbr.minhadespensa.core.domain.model.MovementType
import com.bitlabbr.minhadespensa.core.domain.model.PantryStockValue
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryMovementRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMonthlyFinancialSummaryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMostConsumedProductsUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetPantryTotalValueUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetYearlyFinancialSummaryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SetMonthlyBudgetUseCase
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.features.home.model.ConsumptionTrendItemUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeFinancialUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeMonthlyBarUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomePantryStockUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeUiState
import com.bitlabbr.minhadespensa.uisystem.features.home.model.HomeYearlyAnalyticsUiModel
import com.bitlabbr.minhadespensa.uisystem.features.home.widgets.ExpiringItemCard
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.mapper.toAbbreviation
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import com.bitlabbr.minhadespensa.uisystem.util.formatPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.error_invalid_number
import minhadespensa.uisystem.generated.resources.home_expiring_in_days
import minhadespensa.uisystem.generated.resources.home_expiring_no_date
import minhadespensa.uisystem.generated.resources.home_expiring_overdue_1
import minhadespensa.uisystem.generated.resources.home_expiring_overdue_days
import minhadespensa.uisystem.generated.resources.home_expiring_today
import minhadespensa.uisystem.generated.resources.home_expiring_tomorrow
import minhadespensa.uisystem.generated.resources.home_financial_budget_saved_error
import minhadespensa.uisystem.generated.resources.home_financial_budget_saved_success
import kotlin.math.roundToLong

class HomeViewModel(
    private val getMonthlyFinancialSummaryUseCase: GetMonthlyFinancialSummaryUseCase,
    private val setMonthlyBudgetUseCase: SetMonthlyBudgetUseCase,
    private val getMostConsumedProductsUseCase: GetMostConsumedProductsUseCase,
    private val getPantryTotalValueUseCase: GetPantryTotalValueUseCase,
    private val getYearlyFinancialSummaryUseCase: GetYearlyFinancialSummaryUseCase,
    private val pantryRepository: PantryRepository,
    private val pantryMovementRepository: PantryMovementRepository,
    private val catalogRepository: CatalogRepository,
    private val logger: AppLogger? = null,
    private val notificationManager: AppNotificationManager? = null,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val currentDate: LocalDate
        get() = clock.now().toLocalDateTime(timeZone).date

    val currentYear: Int
        get() = currentDate.year

    val currentMonth: Int
        get() = currentDate.monthNumber

    private val _isSetBudgetDialogOpen = MutableStateFlow(false)
    private val _isSavingBudget = MutableStateFlow(false)
    private val _isFinancialAnalyticsDialogOpen = MutableStateFlow(false)

    private fun getStartOfMonthMillis(year: Int, month: Int): Long {
        return LocalDate(year, month, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun getEndOfMonthMillis(year: Int, month: Int): Long {
        val nextMonthDate = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        return nextMonthDate.atStartOfDayIn(timeZone).toEpochMilliseconds() - 1
    }

    private fun getStartOfYearMillis(year: Int): Long {
        return LocalDate(year, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun getEndOfYearMillis(year: Int): Long {
        return LocalDate(year + 1, 1, 1).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1
    }

    private data class HomeDomainData(
        val financial: MonthlyFinancialSummary,
        val expiring: List<PantryItemWithCategory>,
        val mostConsumed: List<Pair<String, Double>>,
        val products: List<CatalogProduct>,
    )

    private data class HomeAnalyticsDomainData(
        val stockValue: PantryStockValue,
        val yearlySummaries: List<MonthlyFinancialSummary>,
        val yearlyMovements: List<PantryMovement>,
    )

    private val homeDataFlow = combine(
        getMonthlyFinancialSummaryUseCase(currentYear, currentMonth),
        pantryRepository.getExpiringPantryItems(CoreConstants.Pantry.EXPIRING_THRESHOLD_DAYS),
        getMostConsumedProductsUseCase(
            startTimestamp = getStartOfMonthMillis(currentYear, currentMonth),
            endTimestamp = getEndOfMonthMillis(currentYear, currentMonth),
            limit = 5,
        ),
        catalogRepository.getAllActiveProducts(),
    ) { financial, expiring, mostConsumed, products ->
        HomeDomainData(
            financial = financial,
            expiring = expiring,
            mostConsumed = mostConsumed,
            products = products,
        )
    }

    private val homeAnalyticsFlow = combine(
        getPantryTotalValueUseCase(),
        getYearlyFinancialSummaryUseCase(currentYear),
        pantryMovementRepository.getMovementsForPeriod(
            startTimestamp = getStartOfYearMillis(currentYear),
            endTimestamp = getEndOfYearMillis(currentYear),
        ),
    ) { stockValue, yearlySummaries, yearlyMovements ->
        HomeAnalyticsDomainData(
            stockValue = stockValue,
            yearlySummaries = yearlySummaries,
            yearlyMovements = yearlyMovements,
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        homeDataFlow,
        homeAnalyticsFlow,
        _isSetBudgetDialogOpen,
        _isSavingBudget,
        _isFinancialAnalyticsDialogOpen,
    ) { data, analyticsData, isDialogOpen, isSaving, isAnalyticsDialogOpen ->
        val productsMap = data.products.associateBy { it.id }

        val budgetCents = data.financial.budgetInCents
        val spentCents = data.financial.totalSpentInCents
        val hasBudget = budgetCents != null && budgetCents > 0L

        val progress = if (hasBudget) {
            spentCents.toFloat() / budgetCents!!.toFloat()
        } else {
            0f
        }

        val financialModel = HomeFinancialUiModel(
            totalSpentInCents = spentCents,
            budgetInCents = budgetCents,
            consumptionProgress = progress,
            currentConsumption = spentCents.formatPrice(includeCurrencySymbol = true),
            budget = budgetCents?.formatPrice(includeCurrencySymbol = true) ?: "R$ 0,00",
            targetChartLabel = budgetCents?.formatPrice(includeCurrencySymbol = true) ?: "",
            hasBudget = hasBudget,
        )

        val expiringList = data.expiring.map { itemWithCat ->
            mapExpiringItem(itemWithCat, productsMap)
        }

        val trendList = data.mostConsumed.map { (productId, qty) ->
            mapConsumptionTrendItem(productId, qty, productsMap)
        }

        val stockModel = mapPantryStockValue(analyticsData.stockValue)
        val yearlyAnalyticsModel = mapYearlyAnalytics(
            year = currentYear,
            yearlySummaries = analyticsData.yearlySummaries,
            yearlyMovements = analyticsData.yearlyMovements,
            stockValue = analyticsData.stockValue,
        )

        HomeUiState(
            financialSummary = financialModel,
            pantryStockValue = stockModel,
            yearlyAnalytics = yearlyAnalyticsModel,
            expiringItems = expiringList,
            consumptionTrends = trendList,
            isSetBudgetDialogOpen = isDialogOpen,
            isSavingBudget = isSaving,
            isFinancialAnalyticsDialogOpen = isAnalyticsDialogOpen,
            isLoading = false,
            error = null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun mapExpiringItem(
        itemWithCat: PantryItemWithCategory,
        productsMap: Map<String, CatalogProduct>,
    ): ExpiringItemCard {
        val item = itemWithCat.pantryItem
        val expMillis = item.expirationDate
        val (label, targetLabel, gaugeProgress, isExpired) = if (expMillis != null) {
            val expDate = Instant.fromEpochMilliseconds(expMillis).toLocalDateTime(timeZone).date
            val daysDiff = (expDate.toEpochDays() - currentDate.toEpochDays())
            val dayStr = expDate.dayOfMonth.toString().padStart(2, '0')
            val monthStr = expDate.monthNumber.toString().padStart(2, '0')
            val target = "$dayStr/$monthStr"
            val textLabel: UiText = when {
                daysDiff < 0 -> if (daysDiff == -1) {
                    UiText.Resource(Res.string.home_expiring_overdue_1)
                } else {
                    UiText.Resource(Res.string.home_expiring_overdue_days, listOf(-daysDiff))
                }
                daysDiff == 0 -> UiText.Resource(Res.string.home_expiring_today)
                daysDiff == 1 -> UiText.Resource(Res.string.home_expiring_tomorrow)
                else -> UiText.Resource(Res.string.home_expiring_in_days, listOf(daysDiff))
            }
            val p = ((CoreConstants.Pantry.EXPIRING_THRESHOLD_DAYS - daysDiff).toFloat() / CoreConstants.Pantry.EXPIRING_THRESHOLD_DAYS.toFloat())
            ExpiringItemData(textLabel, target, p, daysDiff < 0)
        } else {
            ExpiringItemData(UiText.Resource(Res.string.home_expiring_no_date), "--/--", 0f, false)
        }

        val catalogProduct = productsMap[item.productId]
        return ExpiringItemCard(
            expirationLabel = label,
            isExpired = isExpired,
            productName = itemWithCat.name.ifBlank { catalogProduct?.name ?: "Produto" },
            productCategory = itemWithCat.category.ifBlank { catalogProduct?.category ?: "Geral" },
            anchoredTargetLabel = targetLabel,
            anchoredGaugeProgress = gaugeProgress,
            iconPainterURI = catalogProduct?.thumbnailUrl,
        )
    }

    private data class ExpiringItemData(
        val label: UiText,
        val targetLabel: String,
        val gaugeProgress: Float,
        val isExpired: Boolean,
    )

    private fun mapConsumptionTrendItem(
        productId: String,
        quantity: Double,
        productsMap: Map<String, CatalogProduct>,
    ): ConsumptionTrendItemUiModel {
        val product = productsMap[productId]
        val name = product?.name ?: "Produto"
        val category = product?.category ?: "Geral"
        val unit = product?.measureUnit?.toAbbreviation() ?: "un"
        val formattedQty = if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            quantity.toString().replace('.', ',')
        }
        return ConsumptionTrendItemUiModel(
            productName = name,
            productCategory = category,
            productMeasureUnity = unit,
            consumptionAmount = formattedQty,
            iconPainterURI = product?.thumbnailUrl,
        )
    }

    fun openSetBudgetDialog() {
        _isSetBudgetDialogOpen.value = true
    }

    fun dismissSetBudgetDialog() {
        _isSetBudgetDialogOpen.value = false
    }

    fun saveMonthlyBudget(budgetInCents: Long) {
        viewModelScope.launch {
            _isSavingBudget.value = true
            val result = setMonthlyBudgetUseCase(currentYear, currentMonth, budgetInCents)
            if (result.isSuccess) {
                logger?.d("HomeViewModel", "Orçamento definido com sucesso: $budgetInCents centavos")
                notificationManager?.showSuccess(UiText.Resource(Res.string.home_financial_budget_saved_success))
                _isSetBudgetDialogOpen.value = false
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                logger?.e("HomeViewModel", "Falha ao definir orçamento: $errorMsg")
                notificationManager?.showError(UiText.Resource(Res.string.home_financial_budget_saved_error))
            }
            _isSavingBudget.value = false
        }
    }

    fun saveMonthlyBudgetText(budgetText: String) {
        val clean = budgetText.trim().replace(".", "").replace(",", ".")
        val valueInDouble = clean.toDoubleOrNull()
        if (valueInDouble != null && valueInDouble >= 0.0) {
            val cents = (valueInDouble * 100).roundToLong()
            saveMonthlyBudget(cents)
        } else {
            notificationManager?.showError(UiText.Resource(Res.string.error_invalid_number))
        }
    }

    private fun mapPantryStockValue(stockValue: PantryStockValue): HomePantryStockUiModel {
        val hasPriced = stockValue.itemsWithPriceCount > 0
        return HomePantryStockUiModel(
            totalValueInCents = stockValue.totalValueInCents,
            formattedTotalValue = stockValue.totalValueInCents.formatPrice(includeCurrencySymbol = true),
            itemsWithPriceCount = stockValue.itemsWithPriceCount,
            totalActiveItemsCount = stockValue.totalActiveItemsCount,
            averageUnitPriceInCents = stockValue.averagePriceInCents,
            formattedAverageUnitPrice = stockValue.averagePriceInCents?.formatPrice(includeCurrencySymbol = true),
            hasPricedItems = hasPriced,
        )
    }

    private val monthNames = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    private fun mapYearlyAnalytics(
        year: Int,
        yearlySummaries: List<MonthlyFinancialSummary>,
        yearlyMovements: List<PantryMovement>,
        stockValue: PantryStockValue,
    ): HomeYearlyAnalyticsUiModel {
        val totalYearSpent = yearlySummaries.sumOf { it.totalSpentInCents }
        val elapsedMonths = maxOf(1, currentMonth)
        val averageMonthlySpent = (totalYearSpent.toDouble() / elapsedMonths).roundToLong()

        val purchaseMovements = yearlyMovements.filter {
            !it.isDeleted &&
                (it.type == MovementType.ENTRY_PURCHASE || it.type == MovementType.ENTRY_MANUAL) &&
                (it.unitPriceInCents != null || it.totalPriceInCents != null)
        }
        val totalPurchasesSpent = purchaseMovements.sumOf { it.computedTotalPriceInCents ?: 0L }
        val totalPurchasesQuantity = purchaseMovements.sumOf { it.quantity }
        val avgPriceInCents = if (totalPurchasesQuantity > 0.0) {
            (totalPurchasesSpent.toDouble() / totalPurchasesQuantity).roundToLong()
        } else {
            stockValue.averagePriceInCents
        }

        val bars = yearlySummaries.map { summary ->
            val budget = summary.budgetInCents
            val hasBudget = budget != null && budget > 0L
            val progress = if (hasBudget) {
                summary.totalSpentInCents.toFloat() / budget!!.toFloat()
            } else {
                0f
            }
            val monthLabel = if (summary.month in 1..12) monthNames[summary.month - 1] else "M${summary.month}"

            HomeMonthlyBarUiModel(
                month = summary.month,
                monthName = monthLabel,
                totalSpentInCents = summary.totalSpentInCents,
                formattedTotalSpent = summary.totalSpentInCents.formatPrice(includeCurrencySymbol = true),
                budgetInCents = budget,
                formattedBudget = budget?.formatPrice(includeCurrencySymbol = true) ?: "R$ 0,00",
                progress = progress,
                isOverBudget = summary.isOverBudget,
                hasBudget = hasBudget,
            )
        }

        return HomeYearlyAnalyticsUiModel(
            year = year,
            totalYearSpentInCents = totalYearSpent,
            formattedTotalYearSpent = totalYearSpent.formatPrice(includeCurrencySymbol = true),
            averageMonthlySpentInCents = averageMonthlySpent,
            formattedAverageMonthlySpent = averageMonthlySpent.formatPrice(includeCurrencySymbol = true),
            averageItemPriceInCents = avgPriceInCents,
            formattedAverageItemPrice = avgPriceInCents?.formatPrice(includeCurrencySymbol = true),
            monthlySummaries = bars,
        )
    }

    fun openFinancialAnalyticsDialog() {
        _isFinancialAnalyticsDialogOpen.value = true
    }

    fun dismissFinancialAnalyticsDialog() {
        _isFinancialAnalyticsDialogOpen.value = false
    }
}

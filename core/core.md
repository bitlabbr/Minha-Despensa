# Core Module Analysis

This document provides a comprehensive analysis of the `:core` module, outlining its purpose, architecture, dependencies, domain models, use cases, repository contracts, and utilities.

## 1. Module Purpose & Technology Stack

The `:core` module serves as the central domain layer for the **Minha Despensa** application. It encapsulates all core business logic, domain entities, use cases, repository contracts, and domain-level utilities.

Following **Clean Architecture** principles, the `:core` module has **zero dependencies on outer layers** (such as `:data` or `:uisystem`). It is completely framework-independent, with persistence and UI details abstracted behind interfaces.

Key technologies and architectural characteristics include:
*   **Kotlin Multiplatform (KMP)**: Enables full domain logic sharing between Android and iOS.
*   **Coroutines & Reactive Streams**: Utilizes `kotlinx.coroutines.core` and `Flow` for reactive, asynchronous data streams.
*   **Serialization**: Uses `kotlinx.serialization.json` for domain model serialization.
*   **Dependency Injection**: Integrates `koin-core` (exposed via `api`), allowing downstream modules to register and consume domain use cases and qualifiers.
*   **Date & Time Operations**: Uses `kotlinx.datetime` (exposed via `api`) for multiplatform timestamp management.
*   **Result-based / Exception-safe Contracts**: Domain use cases utilize Kotlin's `Result<T>` and strict domain preconditions (`require`) with localized Portuguese validation errors.

---

## 2. Dependencies

### Build Configuration (`core/build.gradle.kts`)
*   `androidLibrary`: Configures Android library target.
*   `kotlinMultiplatform`: Targets Android (`JVM 11`) and iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64` with static framework `Core`).

### Dependencies:
*   `api(libs.koin.core)`: Exposed to consumers for dependency injection definitions.
*   `api(libs.kotlinx.datetime)`: Exposed to consumers for date/time operations.
*   `implementation(libs.kotlinx.coroutines.core)`: Core coroutine primitives and Flows.
*   `implementation(libs.kotlinx.serialization.json)`: JSON serialization support.
*   `implementation(libs.kotlin.test)`: Multiplatform test framework (`commonTest`).
*   `implementation(libs.kotlinx.coroutines.test)`: Test dispatchers and `runTest` utilities.

### Android Specifics:
*   **Namespace**: `com.bitlabbr.minhadespensa.core`
*   **Compile SDK**: 36
*   **Min SDK**: 24
*   **JVM Target**: JVM 11

---

## 3. Domain Layer Structure

The code is located under `core/src/commonMain/kotlin/com/bitlabbr/minhadespensa/core/domain/`.

```
com.bitlabbr.minhadespensa.core.domain/
├── model/        # Core business models & enums
├── repository/   # Repository contracts (interfaces)
├── usecase/      # Business logic workflows & interactors
└── util/         # Logging, validation constants, timestamp helpers, image processor
```

---

## 4. Key Components

### 4.1. Domain Models (`domain/model`)

All domain models are plain Kotlin data classes or enums annotated with `@Serializable`:

*   **`CatalogProduct`**: Represents a product in the global/local catalog.
    *   *Fields*: `id`, `name`, `brand`, `measureUnit`, `netWeight`, `thumbnailUrl`, `updatedAt`, `isDeleted`, `manuallyAdded`, `ean`, `category`, `notes`.
*   **`MeasureUnit`**: Supported physical measurement units: `UNIT`, `KILOGRAM`, `GRAM`, `LITER`, `MILLILITER`, `PACKAGE`. Provides `toLabel()` extensions in UI.
*   **`PantryItem`**: Represents product stock in the user's pantry.
    *   *Fields*: `id`, `productId`, `quantity`, `expirationDate`, `batchNumber`, `unitPriceInCents`, `updatedAt`, `isDeleted`.
*   **`PantryItemConsumption`**: Data class representing a consumption entry for batch pantry deduction (`pantryItemId`, `quantityToConsume`).
*   **`PantryItemWithCategory`**: Composite read model combining `PantryItem` with product catalog details (`name`, `category`, `measureUnit`, `netWeight`).
*   **`PriceEntry`**: Historical price recording for a catalog product.
    *   *Fields*: `id`, `productId`, `priceInCents`, `storeName`, `updatedAt`, `isDeleted`.
*   **`ShoppingItem`**: An item within a shopping list. Supports both catalog-linked items (`productId`) and scratchpad free-text entries (`rawText`).
    *   *Fields*: `id`, `productId`, `listId`, `rawText`, `quantity`, `priceAtTime`, `isChecked`, `updatedAt`, `isDeleted`.
    *   *Computed Domain Property*:
        *   `subtotalInCents`: Calculated as `priceAtTime?.let { (quantity * it).roundToLong() }`.
*   **`ShoppingList`**: Represents a shopping list with status and type metadata.
    *   *Fields*: `id`, `name`, `type` (`SCRATCHPAD`, `PLANNED`, `ASSISTANT`), `status` (`DRAFT`, `SHOPPING`, `COMPLETED`, `CANCELLED`), `budgetInCents`, `items`, `updatedAt`, `isDeleted`.
    *   *Computed Domain Properties*:
        *   `totalActiveItems`: Count of non-deleted items.
        *   `totalCheckedItems`: Count of non-deleted checked items.
        *   `totalCartInCents`: Sum of `subtotalInCents` for checked items.
        *   `isOverBudget`: Boolean indicating if `totalCartInCents` exceeds `budgetInCents`.
*   **`MonthlyBudget`**: Represents the family's spending budget limit for a specific month and year.
    *   *Fields*: `year`, `month`, `budgetInCents`, `updatedAt`.
*   **`MonthlyFinancialSummary`**: Aggregate domain model representing the financial status and health for a specific month.
    *   *Fields*: `year`, `month`, `budgetInCents`, `totalSpentInCents`, `remainingInCents`, `consumptionProgress`, `isOverBudget`.
    *   *Computed Domain Properties*:
        *   `remainingInCents`: Calculated as `budgetInCents?.let { it - totalSpentInCents }`.
        *   `consumptionProgress`: Ratio of `totalSpentInCents / budgetInCents` (0f when no budget or zero budget).
        *   `isOverBudget`: Boolean indicating whether `totalSpentInCents` exceeds `budgetInCents`.
*   **`PantryMovement`**: Immutable audit ledger and inventory transaction event recording physical entries and depletions.
    *   *Fields*: `id`, `pantryItemId`, `productId`, `type` (`MovementType`), `quantity`, `unitPriceInCents`, `totalPriceInCents`, `timestamp`, `storeOrReason`, `isDeleted`.
    *   *Movement Types*: `ENTRY_MANUAL`, `ENTRY_PURCHASE`, `CONSUMPTION`, `EXPIRATION_DISCARD`, `ADJUSTMENT`.
    *   *Computed Domain Property*:
        *   `computedTotalPriceInCents`: Resolved from `totalPriceInCents` or calculated as `unitPriceInCents * quantity`.
*   **`PantryStockValue`**: Aggregate domain model representing the current financial valuation of the pantry inventory.
    *   *Fields*: `totalValueInCents`, `itemsWithPriceCount`, `totalActiveItemsCount`.
    *   *Computed Domain Property*:
        *   `averagePriceInCents`: Resolved as `totalValueInCents / itemsWithPriceCount` when `itemsWithPriceCount > 0`, or `null`.
*   **`IconKeys`**: String keys mapping domain categories to application icons (e.g., `GRAINS`, `CLEANING`, `BEVERAGES`).

---

### 4.2. Domain Use Cases (`domain/usecase`)

The `:core` module defines 15 use cases encapsulating discrete application business rules and orchestration:
*   **`SaveCatalogProductUseCase`**:
    *   Validates catalog product constraints (name length, category, EAN numeric format and length, positive net weight).
    *   Saves or updates the product via `CatalogRepository` with optional image thumbnail bytes and explicit `removeImage: Boolean` flag when editing.
*   **`CheckEanStatusUseCase`**:
    *   Validates EAN barcode format and checks if a product with this EAN already exists in `CatalogRepository`.
*   **`AddPantryItemUseCase`**:
    *   Validates pantry item fields (positive quantity, valid IDs, non-negative unit price if specified) and persists the item via `PantryRepository`.
*   **`UpdatePantryItemUseCase`**:
    *   Validates existing pantry item modifications (ID presence, non-negative quantity, non-negative unit price if specified) and updates the item via `PantryRepository.updatePantryItemIfNewer()`, marking `isDeleted = true` if quantity reaches zero.
*   **`CreatePlannedShoppingListUseCase`**:
    *   Creates a planned shopping list (`ShoppingListType.PLANNED`, `ShoppingListStatus.DRAFT`) with specified items and budget constraints, automatically consolidating duplicate product drafts by summing quantities.
*   **`CreateQuickShoppingListUseCase`**:
    *   Generates a quick scratchpad shopping list (`ShoppingListType.SCRATCHPAD`, `ShoppingListStatus.DRAFT`) parsing unformatted line items.
*   **`AddCatalogItemToShoppingListUseCase`**:
    *   Attaches a catalog product to an active shopping list as a `ShoppingItem`, accumulating quantity if the product is already present in the list.
*   **`AddOrUpdateCartItemUseCase`**:
    *   Handles in-cart item adjustments (quantity updates, price overrides, check/uncheck status) during a shopping trip. Automatically consolidates product quantity if the product is already present in the active cart when adding without an explicit `existingItemId`.
*   **`ReplaceCartItemUseCase`**:
    *   Replaces a shopping item in the cart during an active session with an alternative product or custom description, preserving quantity and overriding price.
*   **`RemoveCartItemUseCase`**:
    *   Soft-deletes a shopping item from an active shopping list.
*   **`StartShoppingSessionUseCase`**:
    *   Transitions an active list into `ShoppingListStatus.SHOPPING` (or instantiates a new `ShoppingListType.ASSISTANT` session).
*   **`FinalizeShoppingSessionUseCase`**:
    *   Executes checkout: invokes `ShoppingListRepository.finalizePurchase()`, moving checked items into `PantryRepository`, registering price history in `PriceRepository`, and marking the list as `ShoppingListStatus.COMPLETED`.
*   **`SetMonthlyBudgetUseCase`**:
    *   Validates year (2000..2100), month (1..12) and non-negative budget value, persisting or updating the family's monthly budget target via `BudgetRepository.setBudgetForMonth()`.
*   **`GetMonthlyFinancialSummaryUseCase`**:
    *   Combines the monthly budget target with aggregated expenses from `BudgetRepository.getBudgetForMonth()` and `BudgetRepository.getMonthlyExpenses()`, emitting a reactive `MonthlyFinancialSummary` with progress, remaining balance, and over-budget flag.
*   **`GetMostConsumedProductsUseCase`**:
    *   Retrieves the most consumed products within a specified time range via `PantryMovementRepository.getMostConsumedProducts()`.
*   **`GetPantryTotalValueUseCase`**:
    *   Calculates the total monetary value of current active inventory in the pantry by summing `quantity * unitPriceInCents` for priced items, emitting a reactive `PantryStockValue`.
*   **`GetYearlyFinancialSummaryUseCase`**:
    *   Validates year (2000..2100) and delegates to `BudgetRepository.getYearlySummary()`, emitting the 12-month budget vs expense history.

---

### 4.3. Repository Contracts (`domain/repository`)

Interface definitions implemented by the data persistence layer (e.g. `:data`):

*   **`BudgetRepository`**:
    *   `getBudgetForMonth(year: Int, month: Int): Flow<MonthlyBudget?>`
    *   `setBudgetForMonth(year: Int, month: Int, budgetInCents: Long)`
    *   `getMonthlyExpenses(year: Int, month: Int): Flow<Long>`
    *   `getYearlySummary(year: Int): Flow<List<MonthlyFinancialSummary>>`
*   **`PantryMovementRepository`**:
    *   `recordMovement(movement: PantryMovement)`
    *   `recordMovements(movements: List<PantryMovement>)`
    *   `getMovementsForPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<PantryMovement>>`
    *   `getMostConsumedProducts(startTimestamp: Long, endTimestamp: Long, limit: Int): Flow<List<Pair<String, Double>>>`
*   **`CatalogRepository`**:
    *   `getProductByEan(ean: String): Flow<CatalogProduct?>`
    *   `getProductById(id: String): Flow<CatalogProduct?>`
    *   `getAllActiveProducts(): Flow<List<CatalogProduct>>`
    *   `searchProductsByNameOrBrand(query: String): Flow<List<CatalogProduct>>`
    *   `insertProduct(product: CatalogProduct, imageBytes: ByteArray?)`
    *   `forceUpdateProduct(product: CatalogProduct, imageBytes: ByteArray?, removeImage: Boolean = false)`
    *   `updateProductIfNewer(product: CatalogProduct, imageBytes: ByteArray?, removeImage: Boolean = false)`
    *   `markProductAsDeleted(id: String, updatedAt: Long)`
    *   `deleteProductById(id: String)`
    *   `existsById(id: String): Flow<Boolean>`
    *   `getCategories(): Flow<List<String>>`
    *   `getProductImage(productId: String): Flow<ByteArray?>`
*   **`PantryRepository`**:
    *   `getAllActivePantryItems(): Flow<List<PantryItem>>`
    *   `getAllActivePantryItemsWithCategory(): Flow<List<PantryItemWithCategory>>`
    *   `getPantryItemWithCategoryById(pantryItemId: String): Flow<PantryItemWithCategory?>`
    *   `getExpiringPantryItems(thresholdDays: Int): Flow<List<PantryItemWithCategory>>`
    *   `getPantryItemById(pantryItemId: String): Flow<PantryItem?>`
    *   `getPantryItemsByProductId(productId: String): Flow<List<PantryItem>>`
    *   `insertPantryItem(item: PantryItem)`
    *   `forceUpdatePantryItem(item: PantryItem)`
    *   `updatePantryItemIfNewer(item: PantryItem)`
    *   `markPantryItemAsDeleted(id: String, updatedAt: Long)`
    *   `deletePantryItemById(id: String)`
    *   `consumePantryItem(pantryItemId: String, quantityToConsume: Double)`
    *   `consumeBatch(consumptions: List<PantryItemConsumption>)`
*   **`PriceRepository`**:
    *   `getPriceHistoryByProductId(productId: String): Flow<List<PriceEntry>>`
    *   `getLatestPriceForProductId(productId: String): Flow<PriceEntry?>`
    *   `insertPriceEntry(priceEntry: PriceEntry)`
    *   `forceUpdatePriceEntry(priceEntry: PriceEntry)`
    *   `updatePriceEntryIfNewer(priceEntry: PriceEntry)`
    *   `markPriceEntryAsDeleted(priceEntryId: String, updatedAt: Long)`
    *   `deletePriceEntryById(priceEntryId: String)`
*   **`ShoppingListRepository`**:
    *   `getAllActiveShoppingLists(): Flow<List<ShoppingList>>`
    *   `getShoppingListById(listId: String): Flow<ShoppingList?>`
    *   `insertShoppingList(shoppingList: ShoppingList)`
    *   `forceUpdateShoppingList(shoppingList: ShoppingList)`
    *   `updateShoppingListIfNewer(list: ShoppingList)`
    *   `markShoppingListAsDeleted(listId: String, updatedAt: Long)`
    *   `deleteShoppingListById(listId: String)`
    *   `insertShoppingItem(item: ShoppingItem)`
    *   `forceUpdateShoppingItem(item: ShoppingItem)`
    *   `updateShoppingItemIfNewer(item: ShoppingItem)`
    *   `toggleItemCheck(itemId: String, isChecked: Boolean)`
    *   `markShoppingItemAsDeleted(itemId: String, updatedAt: Long)`
    *   `deleteShoppingItemById(itemId: String)`
    *   `finalizePurchase(listId: String)`

---

### 4.4. Domain Utilities (`domain/util`)

*   **`CoreConstants`**: Centralized domain constants for validation and defaults:
    *   `Product`: `NAME_MAX_LENGTH (30)`, `BRAND_MAX_LENGTH (30)`, `CATEGORY_MAX_LENGTH (30)`, `NOTES_MAX_LENGTH (255)`, `DEFAULT_CATEGORY ("Outros")`, `DEFAULT_NET_WEIGHT (1.0)`, `EAN_VALID_LENGTHS (setOf(8, 12, 13, 14))`, `WEIGHT_MAX_LENGTH (8)`.
    *   `Media`: `MAX_IMAGE_SIZE_KB (100)`.
    *   `ShoppingList`: `NAME_MAX_LENGTH (30)`.
    *   `Pantry`: `EXPIRING_THRESHOLD_DAYS (7)`.
    *   `Validation`: Standard localized error strings (Portuguese) for domain validation messages.
    *   `CatalogCategories`: Predefined default product categories list.
*   **`AppLogger` & `ConsoleLogger`**: Logging contract and multiplatform standard output implementation.
*   **`DiQualifiers`**: Named injection qualifiers (`CORE_LOGGER`, `DATA_LOGGER`, `APP_LOGGER`, `UI_LOGGER`).
*   **`Helpers.kt`**:
    *   `getCurrentTime()`: Epoch milliseconds using `Clock.System.now().toEpochMilliseconds()`.
    *   `isValidTimestamp(timestamp: Long, toleranceMillis: Long)`: Timestamp boundary validation preventing negative values and distant future anomalies, accounting for clock skew.
    *   `getMonthTimeRange(year: Int, month: Int, timeZone: TimeZone)`: Calculates exact epoch milliseconds range `[startOfMonth, endOfMonth]` for financial and monthly aggregations using `kotlinx.datetime.LocalDate`.
*   **`ImageProcessor`**: Multiplatform `expect` interface for scaling and compressing raw image bytes to WebP thumbnails.
*   **`TextNormalizer.kt`**: Multiplatform text normalization extensions (`unaccent()`, `normalizeForSearch()`, `matchesSearchQuery()`) for accent- and case-insensitive search queries across products, brands, and categories.

---

## 5. Testing & Verification

The `:core` module contains comprehensive unit tests located in `core/src/commonTest/`:
*   `CatalogAndPantryUseCasesTest`: Validates `SaveCatalogProductUseCase`, `AddPantryItemUseCase`, and `UpdatePantryItemUseCase` business rules, constraints, and error handling.
*   `ShoppingUseCasesTest`: Validates shopping session lifecycles, list creations, cart transitions, item replacements, and deletions.
*   `CheckEanStatusUseCaseTest`: Tests barcode validation, digit checking, and repository lookup.
*   `BudgetModelsTest`: Validates `MonthlyBudget`, `MonthlyFinancialSummary` computed properties and edge cases, and `getMonthTimeRange` boundary calculations.
*   `BudgetUseCasesTest`: Validates `SetMonthlyBudgetUseCase` and `GetMonthlyFinancialSummaryUseCase` business constraints, reactive summary emission, and edge cases.
*   `PantryTotalValueUseCaseTest`: Validates pantry stock financial valuation, priced vs unpriced item filtering, fractional quantity calculations, and deleted item exclusion.
*   `YearlyFinancialSummaryUseCaseTest`: Validates 12-month budget vs expense aggregation, year boundary validation, and monthly progress computation.
*   `DomainModelsTest`: Tests model serialization, copy operations, and computed properties (`totalCartInCents`, `isOverBudget`, `subtotalInCents`).
*   `CoreConstantsTest`: Ensures domain constants remain immutable and properly bounded.
*   `TimestampToleranceTest`: Verifies clock skew tolerance and boundary enforcement.
*   `ConsoleLoggerTest`: Verifies formatting and level output.

All 102 tests in `:core` run under Gradle via `./gradlew :core:test` and `./gradlew testDebugUnitTest`.
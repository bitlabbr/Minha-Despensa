# Data Module Analysis

This document provides an in-depth analysis of the `:data` module, outlining its purpose, clean architecture implementation, Room persistence schema, mappers, DAOs, repositories, multiplatform utilities, and testing strategies.

## 1. Module Purpose

The `:data` module in **Minha Despensa** is a Kotlin Multiplatform (KMP) library responsible for data persistence and local storage. It acts as the single source of truth for all local data, implementing the repository interfaces declared in `:core:domain`.

Key capabilities:
*   **Offline-First Local Storage**: Uses Google Room Multiplatform (Version 6) backed by SQLite (`BundledSQLiteDriver`).
*   **Migration Pipeline**: Full schema evolution and migration strategy supporting incremental and multi-version jumps (`1->2`, `2->3`, `3->4`, `4->5`, `5->6`, direct `1->6`, `2->6`, `3->6`, `4->6`).
*   **Conflict Resolution**: Enforces Last-Write-Wins (LWW) conflict resolution on updates via SQL timestamp comparison (`updated_at < :updatedAt`).
*   **Atomic Transactions**: Protects multi-table operations (such as product creation with media, pantry consumption batches, and shopping checkout) using Room's `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`.
*   **Clean Architecture Separation**: Pure separation between Room database entities and core domain models via dedicated mappers in `com.bitlabbr.minhadespensa.data.local.mapper`.

---

## 2. Architecture & Directory Structure

```
data/src/commonMain/kotlin/com/bitlabbr/minhadespensa/data/
├── di/                     # Koin DataModule dependency injection
├── local/
│   ├── AppDatabase.kt      # Main Room database declaration (Version 6)
│   ├── converter/          # Room TypeConverters (Instant, MeasureUnit)
│   ├── dao/                # Room Data Access Objects (DAOs)
│   ├── dto/                # Room query projection DTOs (e.g. joins)
│   ├── entity/             # Room SQLite table entities
│   ├── mapper/             # Bidirectional Entity <-> Domain mappers
│   └── migration/          # Room Migration definitions (1->2, 2->3, 3->4, 4->5, 5->6, direct jumps to v6)
├── repository/             # Room repository implementations
└── util/                   # Common utilities (ImageProcessor expect interface)
```

Platform-specific source sets:
*   `data/src/androidMain/`: Android `actual` implementation of `ImageProcessor` utilizing Android `Bitmap` and `ExifInterface`.
*   `data/src/iosMain/`: iOS `actual` implementation of `ImageProcessor` utilizing `UIKit` and `CoreGraphics`.

---

## 3. Dependencies

### Internal:
*   `:core`: Consumes domain models (`CatalogProduct`, `PantryItem`, `PriceEntry`, `ShoppingList`, `ShoppingItem`, `MonthlyBudget`, `MonthlyFinancialSummary`, etc.), repository interfaces, domain use cases, and utilities (`AppLogger`, `CoreConstants`, `DiQualifiers`, `Helpers`).

### External:
*   **AndroidX Room Multiplatform** (`androidx.room:room-runtime`, `ksp` compiler): SQLite ORM.
*   **SQLite Bundled Driver** (`androidx.sqlite:sqlite-bundled`): Multiplatform SQLite driver.
*   **Koin Core** (`koin-core`): Dependency injection.
*   **Kotlinx Coroutines & Flow**: Reactive querying.
*   **Kotlinx DateTime**: Timestamps.
*   **Kotlinx UUID**: UUID parsing and generation.
*   **Turbine & Kotlinx Coroutines Test**: Flow and async test assertions.

---

## 4. Key Components

### 4.1. Room Database (`local/AppDatabase.kt`)
*   **Entities**: `CatalogProductEntity`, `PantryItemEntity`, `PriceEntryEntity`, `ShoppingItemEntity`, `ProductMediaEntity`, `ShoppingListEntity`, `MonthlyBudgetEntity`, `PantryMovementEntity`.
*   **Version**: `6`.
*   **Driver**: `BundledSQLiteDriver()`.
*   **Platform Initialization**: Uses `expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>` to instantiate platform SQLite drivers.

---

### 4.2. Database Entities (`local/entity/`)
*   **`CatalogProductEntity`** (`catalog_products`):
    *   *Columns*: `id` (PK), `ean` (Unique Index), `name`, `brand`, `measure_unit`, `net_weight`, `thumbnail_url`, `updated_at`, `is_deleted`, `manually_added`, `category`, `notes`.
*   **`PantryItemEntity`** (`pantry_items`):
    *   *Columns*: `id` (PK), `product_id` (FK to `catalog_products.id` on delete cascade), `quantity`, `expiration_date`, `batch_number`, `unit_price_in_cents`, `updated_at`, `is_deleted`.
*   **`PriceEntryEntity`** (`price_entries`):
    *   *Columns*: `id` (PK), `product_id` (FK to `catalog_products.id` on delete cascade), `price_in_cents`, `store_name`, `updated_at`, `is_deleted`.
*   **`ProductMediaEntity`** (`product_media`):
    *   *Columns*: `product_id` (PK and FK to `catalog_products.id` on delete cascade), `blob` (`ByteArray`), `updated_at`.
*   **`ShoppingItemEntity`** (`shopping_items`):
    *   *Columns*: `id` (PK), `list_id` (FK to `shopping_lists.id` on delete cascade), `product_id` (FK to `catalog_products.id` on delete set null), `raw_text`, `quantity`, `price_at_time`, `is_checked`, `updated_at`, `is_deleted`.
*   **`ShoppingListEntity`** (`shopping_lists`):
    *   *Columns*: `id` (PK), `name`, `list_type`, `list_status`, `budget_in_cents` (Kotlin alias `budgedInCents`), `updated_at`, `is_deleted`.
*   **`ShoppingListWithItems`**:
    *   Room relation data class holding `ShoppingListEntity` with `@Relation` child `List<ShoppingItemEntity>`.
*   **`MonthlyBudgetEntity`** (`monthly_budgets`):
    *   *Columns*: `year` (PK), `month` (PK), `budgetInCents`, `updatedAt`. Composite primary key `(year, month)`.
*   **`PantryMovementEntity`** (`pantry_movements`):
    *   *Columns*: `id` (PK), `pantry_item_id`, `productId` (FK to `catalog_products.id` on delete cascade), `type`, `quantity`, `unit_price_in_cents`, `total_price_in_cents`, `timestamp`, `store_or_reason`, `is_deleted`.
    *   *Indices*: `productId`, `(type, timestamp)`, `timestamp`.

---

### 4.3. Migrations (`local/migration/AppDatabaseMigrations.kt`)
*   **`MIGRATION_1_2`**: Adds `product_media` table with foreign key and index to `catalog_products`.
*   **`MIGRATION_2_3`**: Adds `category` and `notes` to `catalog_products`, creates `shopping_lists` table, and adds `listId` to `shopping_items`.
*   **`MIGRATION_3_4` (`migrateToSchemaV4`)**:
    *   Standardizes column casing and names (e.g. `budget_in_cents`, `list_id`, `product_id`, `raw_text`, `price_at_time`, `is_checked`, `updated_at`, `is_deleted`).
    *   Configures FK on `shopping_items.product_id` as `ON DELETE SET NULL` to support scratchpad items and preserve shopping logs if a product is physically deleted.
    *   Adds unique index on `catalog_products.ean`.
*   **`MIGRATION_4_5` (`migrateToSchemaV5`)**:
    *   Adds `unit_price_in_cents INTEGER DEFAULT NULL` column to `pantry_items`.
*   **`MIGRATION_5_6`**:
    *   Creates table `monthly_budgets` with composite primary key `(year, month)`.
*   **`MIGRATION_6_7`**:
    *   Creates audit and expense ledger table `pantry_movements` with indexes on `(type, timestamp)`, `(productId)` and `timestamp`.
*   **`MIGRATION_1_7`, `MIGRATION_2_7`, `MIGRATION_3_7`, `MIGRATION_4_7` & `MIGRATION_5_7`**: Direct upgrade paths to schema v7.
*   **`ALL_MIGRATIONS`**: Exported migration array consumed by Koin's database builder.

---

### 4.4. Data Transfer Objects (`local/dto/`)
*   **`PantryItemWithCategoryDaoResult`**:
    *   Flat Room query projection joining `pantry_items` with `catalog_products` (`name`, `category`, `measure_unit`, `net_weight`).

---

### 4.5. Domain & Entity Mappers (`local/mapper/`)

Centralized and decoupled from repositories:
*   **`BudgetMapper.kt`**:
    *   `MonthlyBudgetEntity.toDomain()`.
    *   `MonthlyBudget.toEntity()`.
*   **`CatalogMapper.kt`**:
    *   `CatalogProductEntity.toDomain()` (with safe `runCatching` fallback for `MeasureUnit`).
    *   `CatalogProduct.toEntity()`.
*   **`PantryMapper.kt`**:
    *   `PantryItemEntity.toDomain()`.
    *   `PantryItem.toEntity()`.
    *   `PantryItemWithCategoryDaoResult.toDomain()`.
*   **`PriceMapper.kt`**:
    *   `PriceEntryEntity.toDomain()`.
    *   `PriceEntry.toEntity()`.
*   **`PantryMovementMapper.kt`**:
    *   `PantryMovementEntity.toDomain()`.
    *   `PantryMovement.toEntity()`.
*   **`ShoppingMapper.kt`**:
    *   `ShoppingListWithItems.toDomain()`: Preserves soft-deleted items (`isDeleted = true`) so domain synchronization, cart totals, and LWW state machines have full visibility into the item states.
    *   `ShoppingList.toEntity()`.
    *   `ShoppingItemEntity.toDomain()`.
    *   `ShoppingItem.toEntity()`.

---

### 4.6. Data Access Objects (`local/dao/`)
*   **`PantryMovementDao`**:
    *   `insertMovement`, `insertMovements` (`REPLACE`).
    *   `getMovementsForPeriod`, `getAllMovements` (ordered by timestamp DESC).
    *   `getMostConsumedProducts` (reactive aggregation of `CONSUMPTION` movements).
    *   `getMonthlyExpenses`, `findMonthlyExpenses` (reactive calculation summing `ENTRY_MANUAL` and `ENTRY_PURCHASE`).
*   **`MonthlyBudgetDao`**:
    *   `insertOrUpdate` (`REPLACE`), `getBudget` (reactive Flow), `findBudget` (suspend), `getBudgetsForYear` (ordered by month ASC).
    *   `getMonthlyExpenses`, `findMonthlyExpenses` (suspend).
*   **`CatalogProductDao`**:
    *   `findByEan`, `findById`, `getAllActive`, `searchByNameOrBrand` (ordered by name ASC).
    *   `insert` (`ABORT`), `forceUpdateForProduct`, `updateProductIfNewer` (LWW with all product fields including notes), `markAsDeleted`, `deleteProductById`, `exists`.
*   **`PantryItemDao`**:
    *   `getAllActivePantryItems`, `getAllActivePantryItemsWithCategory`, `getPantryItemWithCategoryById`, `getExpiringPantryItems`.
    *   `getPantryItemById`, `getPantryItemsByProductId`.
    *   `insertPantryItem`, `forceUpdatePantryItem`, `updatePantryItemIfNewer`, `markPantryItemAsDeleted`, `deletePantryItemById`.
    *   *(Provides `typealias PantryRepositoryDao = PantryItemDao` for backwards compatibility).*
*   **`PriceEntryDao`**:
    *   `getPriceHistoryByProductId`, `getLatestPriceForProductId`, `findLatestPriceForProductId`, `findById`.
    *   `insertPriceEntry` (`ABORT`), `forceUpdatePriceEntry`, `updatePriceEntryIfNewer`, `markPriceEntryAsDeleted`, `deletePriceEntryById`.
*   **`ProductMediaDao`**:
    *   `getByProductIdFlow`, `insertOrUpdate`, `deleteByProductId`.
*   **`ShoppingItemDao`**:
    *   `getItemsByListId`, `findById`, `insertShoppingItem`, `forceUpdateItem`, `updateItemIfNewer`, `updateCheckStatus`, `markAsDeleted`, `deleteById`.
*   **`ShoppingListDao`**:
    *   `getAllActiveShoppingLists` (returns `@Transaction Flow<List<ShoppingListWithItems>>`), `getShoppingListById`.
    *   `insertShoppingList`, `insertItems`, `forceUpdateForShoppingList`, `updateShoppingListIfNewer` (LWW including `type` and `status`), `updateTimestamp`, `markShoppingListAsDeleted`, `deleteShoppingListById`.

---

### 4.7. Repositories (`repository/`)

Implement domain contracts with encapsulated state, logging, input validation, and atomic transactions:

*   **`RoomCatalogRepository`**:
    *   Encapsulates DAOs (`productDao`, `mediaDao`).
    *   `searchProductsByNameOrBrand`: Executes accent- and case-insensitive search by filtering active products with `matchesSearchQuery`, supporting unaccented and multi-word token queries.
    *   Executes `insertProduct`, `forceUpdateProduct`, and `updateProductIfNewer`. Handles image addition/update via `ProductMediaEntity` or physical deletion via `mediaDao.deleteByProductId` when `removeImage = true`.
    *   Aligns media timestamps directly with `product.updatedAt`.
*   **`RoomPantryRepository`**:
    *   Encapsulates database and DAO as private members.
    *   `insertPantryItem`: Persists the pantry item directly via DAO (ensuring Room's `InvalidationTracker` notifies active Flow query collectors) and automatically records a `PriceEntry` with `id = item.id` and `storeName = "Entrada na Despensa"` when `unitPriceInCents != null`, keeping product price history updated.
    *   `updatePantryItemIfNewer`: Updates pantry stock with LWW conflict resolution directly via DAO (notifying active Flow collectors) and updates the associated `PriceEntry` (`id = item.id`) in-place via `forceUpdatePriceEntry`, creating it if absent or soft-deleting it if the price was cleared, ensuring financial metrics (lowest/highest/average price) remain accurate without ghost duplicate entries.
    *   `consumePantryItem` and `consumeBatch`: Atomically validates stock, prevents consumption of soft-deleted items, and updates quantities inside an immediate transaction.
    *   `getExpiringPantryItems`: Accurately separates active non-expired items approaching the threshold from already expired items.
*   **`RoomPriceRepository`**:
    *   Encapsulates DAO as private member.
    *   Manages price history entries with LWW conflict protection.
*   **`RoomShoppingListRepository`**:
    *   Encapsulates database and DAOs as private members.
    *   `insertShoppingList`: Atomically saves list entity and its associated items.
    *   `finalizePurchase`: Atomically converts checked shopping items into new `PantryItem` inventory entries (repassing `unitPriceInCents = item.priceAtTime`), records `PriceEntry` sharing the same ID as the pantry item when prices are present, unchecks items, and updates the list timestamp to `COMPLETED`.
*   **`RoomBudgetRepository`**:
    *   Encapsulates `AppDatabase` and `MonthlyBudgetDao` as private members.
    *   `getBudgetForMonth`: Emits `MonthlyBudget` domain model mapped from `MonthlyBudgetEntity` for a given `(year, month)`.
    *   `setBudgetForMonth`: Validates year, month, and non-negative budget value, persisting via `budgetDao.insertOrUpdate` with current epoch timestamp.
    *   `getMonthlyExpenses`: Emits total cents spent in the month using reactive Flow aggregation of completed shopping lists + pantry direct entries, with `distinctUntilChanged` protection.
    *   `getYearlySummary`: Combines 12 individual reactive monthly expense flows with the annual budgets query into an immutable, reactive list of 12 `MonthlyFinancialSummary` items ordered from January (month 1) to December (month 12).

---

### 4.8. Dependency Injection (`di/DataModule.kt`)

Configures singletons and use case factories via Koin:
*   `AppDatabase`: Initialized with platform database builder, `addMigrations(*ALL_MIGRATIONS)`, `BundledSQLiteDriver`, and background `Dispatchers.IO`.
*   DAOs: `catalogDao`, `pantryDao`, `priceDao`, `shoppingListDao`, `budgetDao`, `movementDao`.
*   Repositories: `RoomCatalogRepository`, `RoomPantryRepository`, `RoomPriceRepository`, `RoomShoppingListRepository`, `RoomBudgetRepository`, `RoomPantryMovementRepository`, injected with `AppDatabase` and `DiQualifiers.DATA_LOGGER`.
*   Use Case Factories: Registers all 15 domain use cases (`SaveCatalogProductUseCase`, `CheckEanStatusUseCase`, `AddPantryItemUseCase`, `UpdatePantryItemUseCase`, `CreatePlannedShoppingListUseCase`, `CreateQuickShoppingListUseCase`, `AddCatalogItemToShoppingListUseCase`, `AddOrUpdateCartItemUseCase`, `ReplaceCartItemUseCase`, `RemoveCartItemUseCase`, `StartShoppingSessionUseCase`, `FinalizeShoppingSessionUseCase`, `SetMonthlyBudgetUseCase`, `GetMonthlyFinancialSummaryUseCase`, `GetMostConsumedProductsUseCase`).

---

## 5. Testing & Verification

The test suite resides in `data/src/commonTest/` and `data/src/androidUnitTest/` using an in-memory SQLite database (`createInMemoryDatabase(getTestDatabaseBuilder())`):
*   **`AppDatabaseMigrationTest`**: Validates schema upgrades from v1..v6 to v7 (`MIGRATION_5_6`, `MIGRATION_6_7`, and direct migrations `1->7`, `5->7`), verifying table creation (`monthly_budgets`, `pantry_movements`), composite keys, indices, and data preservation.
*   **`PantryMovementDaoTest`**: Tests CRUD operations for `PantryMovementEntity`, timestamp range filtering, top consumed products aggregation, and reactive monthly expense tracking.
*   **`MonthlyBudgetDaoTest`**: Tests CRUD operations for `MonthlyBudgetEntity`, composite key updates, annual queries, and reactive monthly expense aggregation joins across shopping lists, items, and pantry entries.
*   **`RoomBudgetRepositoryTest`**: Tests domain boundary enforcement, monthly budget retrieval/updates, reactive expense aggregation, and full 12-month annual summaries.
*   **`RoomCatalogRepositoryTest`**: Tests EAN lookups, search, CRUD, LWW updates, media blob handling, and validation limits.
*   **`RoomPantryRepositoryTest`**: Tests pantry joins with categories, expiration filtering, single/batch consumption deductions, and negative balance guards.
*   **`RoomPriceRepositoryTest`**: Tests price history queries, latest price resolution, LWW conflict handling, and store name validations.
*   **`RoomShoppingListRepositoryTest`**: Tests shopping lists, item management, scratchpad items, LWW updates, and checkout (`finalizePurchase`).
*   **`DataConsistencyTest`**: Cross-repository integration tests validating database cascade deletions, foreign key enforcement, and end-to-end multi-table transactions.
*   **`ConvertersTest`**: Tests Room type conversion for Instant and MeasureUnit.
*   **`AndroidImageProcessorTest` & `IosImageProcessorTest`**: Platform-specific thumbnail processing tests.

All tests in `:data` run under Gradle via `./gradlew :data:test` and `./gradlew testDebugUnitTest`.

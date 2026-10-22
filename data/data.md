# Data Module Analysis

This document provides an in-depth analysis of the `:data` module, outlining its purpose, clean architecture implementation, Room persistence schema, mappers, DAOs, repositories, multiplatform utilities, and testing strategies.

## 1. Module Purpose

The `:data` module in **Minha Despensa** is a Kotlin Multiplatform (KMP) library responsible for data persistence and local storage. It acts as the single source of truth for all local data, implementing the repository interfaces declared in `:core:domain`.

Key capabilities:
*   **Offline-First Local Storage**: Uses Google Room Multiplatform (Version 9) backed by SQLite (`BundledSQLiteDriver`).
*   **Migration Pipeline**: Full schema evolution and migration strategy supporting incremental and multi-version jumps (`1->2` through `8->9`, and direct jumps `1->9` through `7->9`).
*   **Conflict Resolution**: Enforces Last-Write-Wins (LWW) conflict resolution on updates via SQL timestamp comparison (`updated_at < :updatedAt`).
*   **Atomic Transactions**: Protects multi-table operations (such as product creation with media, category cascading renames/deletes, pantry consumption batches, and shopping checkout) using Room's `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`.
*   **Clean Architecture Separation**: Pure separation between Room database entities and core domain models via dedicated mappers in `com.bitlabbr.minhadespensa.data.local.mapper`.

---

## 2. Architecture & Directory Structure

```
data/src/commonMain/kotlin/com/bitlabbr/minhadespensa/data/
├── di/                     # Koin DataModule dependency injection
├── local/
│   ├── AppDatabase.kt      # Main Room database declaration (Version 9)
│   ├── converter/          # Room TypeConverters (Instant, MeasureUnit)
│   ├── dao/                # Room Data Access Objects (DAOs)
│   ├── dto/                # Room query projection DTOs (e.g. joins)
│   ├── entity/             # Room SQLite table entities
│   ├── mapper/             # Bidirectional Entity <-> Domain mappers
│   └── migration/          # Room Migration definitions (1->2..8->9, direct jumps to v9)
├── repository/             # Room repository implementations
└── util/                   # Common utilities (ImageProcessor expect interface)
```

Platform-specific source sets:
*   `data/src/androidMain/`: Android `actual` implementation of `ImageProcessor` utilizing Android `Bitmap` and `ExifInterface`.
*   `data/src/iosMain/`: iOS `actual` implementation of `ImageProcessor` utilizing `UIKit` and `CoreGraphics`.

---

## 3. Dependencies

### Internal:
*   `:core`: Consumes domain models (`CatalogProduct`, `ProductCategory`, `PantryItem`, `PriceEntry`, `ShoppingList`, `ShoppingItem`, `MonthlyBudget`, `MonthlyFinancialSummary`, etc.), repository interfaces, domain use cases, and utilities (`AppLogger`, `CoreConstants`, `DiQualifiers`, `Helpers`).

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
*   **Entities**: `CatalogProductEntity`, `ProductCategoryEntity`, `PantryItemEntity`, `PriceEntryEntity`, `ShoppingItemEntity`, `ProductMediaEntity`, `ShoppingListEntity`, `MonthlyBudgetEntity`, `PantryMovementEntity`, `PendingSyncEntryEntity`, `AppKeyValueEntity`.
*   **Version**: `10`.
*   **Driver**: `BundledSQLiteDriver()`.
*   **Platform Initialization**: Uses `expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>` to instantiate platform SQLite drivers.

---

### 4.2. Database Entities (`local/entity/`)
*   **`CatalogProductEntity`** (`catalog_products`):
    *   *Columns*: `id` (PK), `ean` (Unique Index), `name`, `brand`, `measureUnit`, `netWeight`, `thumbnailUrl`, `updatedAt`, `isDeleted`, `manuallyAdded`, `category`, `notes`, `imageHash`.
*   **`ProductCategoryEntity`** (`product_categories`):
    *   *Columns*: `id` (PK), `name` (Indexed), `isDefault`, `updatedAt`, `isDeleted`.
*   **`PantryItemEntity`** (`pantry_items`):
    *   *Columns*: `id` (PK), `productId` (FK to `catalog_products.id` on delete cascade), `quantity`, `expirationDate`, `batchNumber`, `unit_price_in_cents`, `updatedAt`, `isDeleted`.
*   **`PriceEntryEntity`** (`price_entries`):
    *   *Columns*: `id` (PK), `productId` (FK to `catalog_products.id` on delete cascade), `priceInCents`, `storeName`, `updatedAt`, `isDeleted`.
*   **`ProductMediaEntity`** (`product_media`):
    *   *Columns*: `productId` (PK and FK to `catalog_products.id` on delete cascade), `blob` (`ByteArray`), `updatedAt`.
*   **`ShoppingItemEntity`** (`shopping_items`):
    *   *Columns*: `id` (PK), `list_id` (FK to `shopping_lists.id` on delete cascade), `product_id` (FK to `catalog_products.id` on delete set null), `raw_text`, `quantity`, `price_at_time`, `is_checked`, `updated_at`, `is_deleted`.
*   **`ShoppingListEntity`** (`shopping_lists`):
    *   *Columns*: `id` (PK), `name`, `list_type`, `list_status`, `budget_in_cents`, `updated_at`, `is_deleted`.
*   **`ShoppingListWithItems`**:
    *   Room relation data class holding `ShoppingListEntity` with `@Relation` child `List<ShoppingItemEntity>`.
*   **`MonthlyBudgetEntity`** (`monthly_budgets`):
    *   *Columns*: `year` (PK), `month` (PK), `budgetInCents`, `updatedAt`, `isDeleted`. Composite primary key `(year, month)`.
*   **`PantryMovementEntity`** (`pantry_movements`):
    *   *Columns*: `id` (PK), `pantry_item_id`, `productId` (FK to `catalog_products.id` on delete cascade), `type`, `quantity`, `unit_price_in_cents`, `total_price_in_cents`, `updatedAt`, `store_or_reason`, `is_deleted`.
    *   *Indices*: `productId`, `(type, updatedAt)`, `updatedAt`.
*   **`PendingSyncEntryEntity`** (`pending_sync_entries`):
    *   Outbox queue for offline mutations awaiting push to remote backend.
    *   *Columns*: `id` (PK auto-increment), `entityType`, `entityId`, `operation` (`UPSERT`, `DELETE`), `payload` (JSON string or null), `updatedAt`, `retryCount`, `createdAt`.
    *   *Indices*: `(entityType, entityId)`, `createdAt`.
*   **`AppKeyValueEntity`** (`app_key_values`):
    *   Local key-value storage for sync cursors, session metadata, and active household tracking.
    *   *Columns*: `key` (PK), `value`, `updatedAt`.

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
*   **`MIGRATION_7_8` (`migrateToSchemaV8`)**:
    *   Creates `product_categories` table (`id`, `name`, `is_default`, `updated_at`, `is_deleted`), creates `index_product_categories_name`, seeds the 9 default categories with deterministic IDs (`00000000-0000-0000-0000-000000000001`..`009`), and backfills any custom categories already present on active `catalog_products`.
*   **`MIGRATION_8_9`**:
    *   Standardizes `pantry_movements` to use `updatedAt` instead of `timestamp` (matching all CRDT entities), renames column `timestamp` to `updatedAt`, drops old indices and recreates `index_pantry_movements_type_updatedAt` and `index_pantry_movements_updatedAt`.
*   **`MIGRATION_9_10`**:
    *   Adds `imageHash TEXT DEFAULT NULL` column to `catalog_products`.
    *   Adds `isDeleted INTEGER NOT NULL DEFAULT 0` column to `monthly_budgets`.
    *   Creates outbox table `pending_sync_entries` with composite index `(entityType, entityId)` and `createdAt` index.
    *   Creates local key-value table `app_key_values` (`key` PK, `value`, `updatedAt`).
*   **Direct Upgrade Paths**: `MIGRATION_1_7`..`MIGRATION_5_7`, `MIGRATION_1_8`..`MIGRATION_6_8`, `MIGRATION_1_9`..`MIGRATION_7_9`, and `MIGRATION_1_10`..`MIGRATION_8_10`.
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
*   **`CategoryMapper.kt`**:
    *   `ProductCategoryEntity.toDomain()`.
    *   `ProductCategory.toEntity()`.
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
*   **`InvoiceMapper.kt`**:
    *   `InvoiceResponseDto.toDomain()`.
    *   `InvoiceItemDto.toDomain()`.
    *   `parseMeasureUnit(rawUnit: String?)`: Maps Brazilian invoice unit representations (e.g. `KG`, `G`, `LT`, `ML`, `CX`, `PCT`, `UN`) to domain `MeasureUnit`.

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
    *   `findByEan`, `findSnapshotByEan`, `findById`, `getAllActive`, `getAllActiveSnapshot`, `searchByNameOrBrand` (ordered by name ASC).
    *   `insert` (`ABORT`), `forceUpdateForProduct`, `updateProductIfNewer` (LWW with all product fields including notes), `updateCategoryById`, `markAsDeleted`, `deleteProductById`, `exists`.
*   **`ProductCategoryDao`**:
    *   `getAllActive` (ordered by `name COLLATE NOCASE ASC`), `getAllActiveSnapshot`, `getAllSnapshot`, `findById`, `countAll`.
    *   `insertOrReplace`, `insertAllIgnore`, `updateIfNewer`, `markAsDeleted`.
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
*   **`PendingSyncDao`**:
    *   `insert`, `insertAll`, `getAllPending`, `observePendingCount`, `getPendingCount`, `deleteById`, `deleteByIds`, `deleteByEntity`, `clearAll`.
*   **`AppKeyValueDao`**:
    *   `get`, `observe`, `put`, `delete`, `clearAll`.

---

### 4.7. Repositories (`repository/`)

Implement domain contracts with encapsulated state, logging, input validation, and atomic transactions:

*   **`RoomCatalogRepository`**:
    *   Encapsulates DAOs (`productDao`, `mediaDao`, `categoryDao`).
    *   `searchProductsByNameOrBrand`: Executes accent- and case-insensitive search by filtering active products with `matchesSearchQuery`, supporting unaccented and multi-word token queries.
    *   Executes `insertProduct`, `forceUpdateProduct`, and `updateProductIfNewer`. Handles image addition/update via `ProductMediaEntity` or physical deletion via `mediaDao.deleteByProductId` when `removeImage = true`, and automatically registers any custom product category in `product_categories`.
    *   Category Management (`getAllActiveCategories`, `getCategories`, `saveCategory`, `renameCategory`, `deleteCategory`): Seeds `DEFAULT_CATEGORIES` once when `product_categories` is empty (`countAll() == 0`), performs accent- and case-insensitive duplicate resolution, and executes atomic cascading updates on `catalog_products` inside `useWriterConnection` transactions when renaming or deleting a category (reassigning deleted categories' products to `"Outros"` and protecting `"Outros"` from deletion).
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
*   **`RoomInvoiceRepository`**:
    *   Encapsulates `InvoiceRemoteDataSource`, `AppDatabase` and logger as private members.
    *   `parseInvoiceFromQrCode`: Communicates with external dedicated invoice parsing backend via `InvoiceRemoteDataSource.parseInvoice()` and maps DTOs to domain `Invoice` with automatic consolidation of duplicate product lines.
    *   `importInvoiceToPantry`: Executes immediate atomic SQLite transaction `useWriterConnection { connection.withTransaction(IMMEDIATE) }` performing consolidated bulk insert: resolves existing products by EAN/name or creates new `CatalogProductEntity` entries, groups items matching the same `targetProductId`, and persists a single consolidated `PantryItemEntity`, a single `PriceEntryEntity`, and a single `PantryMovementEntity` (`ENTRY_PURCHASE`) per unique product.
*   **`RoomAuthRepository`**:
    *   Implements `AuthRepository`: `register`, `login`, `logout`, `refreshToken`, `clearSession`.
    *   Persists JWT tokens and active `AuthSession` in `AppKeyValueDao`.
*   **`RoomHouseholdRepository`**:
    *   Implements `HouseholdRepository`: `observeActiveHousehold`, `getActiveHousehold`, `getUserHouseholds`, `createHousehold`, `updateHousehold`, `joinHousehold`, `leaveHousehold`, `regenerateInviteCode`, `removeMember`, `switchActiveHousehold`.
*   **`RoomSyncRepository`**:
    *   Implements `SyncRepository` with 6-step bidirectional CRDT/LWW synchronization:
        1. Outbox Push: Collects pending outbox mutations and pushes to `/api/v1/sync/push`.
        2. ID Remapping: Atomically remaps product IDs received from server (`productIdMappings`), safely repointing all child tables (`pantry_items`, `price_entries`, `shopping_items`, `pantry_movements`, `product_media`) without triggering `ON DELETE CASCADE`.
        3. Pull: Fetches delta updates since cursor via `/api/v1/sync/pull?since={cursor}`.
        4. Ingestion: Atomically upserts pulled entities using LWW timestamp checks.
        5. Cursor Advancement: Updates `sync_cursor_{householdId}`.
        6. Outbox Drain: Deletes successfully pushed mutations from `pending_sync_entries`.
    *   `enqueueAllLocalEntitiesForInitialSync()`: Backfills all local pre-existing entities into outbox for initial synchronization when user binds/creates an account.
    *   `flushOutboxBeforeSwitch()`: Detects unsynced changes to prevent silent data loss when switching active household.
    *   `clearLocalDataForHouseholdSwitch()`: Atomically wipes tenant-scoped tables while preserving global catalog products and outbox.

---

### 4.8. Dependency Injection (`di/DataModule.kt`)

Configures singletons and use case factories via Koin:
*   `AppDatabase`: Initialized with platform database builder, `addMigrations(*ALL_MIGRATIONS)`, `BundledSQLiteDriver`, and background `Dispatchers.IO`.
*   DAOs: `catalogDao`, `categoryDao`, `pantryDao`, `priceDao`, `shoppingListDao`, `budgetDao`, `movementDao`, `pendingSyncDao`, `appKeyValueDao`.
*   Repositories: `RoomCatalogRepository`, `RoomPantryRepository`, `RoomPriceRepository`, `RoomShoppingListRepository`, `RoomBudgetRepository`, `RoomPantryMovementRepository`, `RoomInvoiceRepository`, `RoomAuthRepository`, `RoomHouseholdRepository`, `RoomSyncRepository`.
*   Networking: `SyncServerConfig`, `SyncClock`, `KtorAuthRemoteDataSource`, `KtorHouseholdRemoteDataSource`, `KtorSyncRemoteDataSource`.
*   Use Case Factories: Registers all 29 domain use cases and `SyncCoordinator`.

---

## 5. Testing & Verification

The test suite resides in `data/src/commonTest/` and `data/src/androidUnitTest/` using an in-memory SQLite database (`createInMemoryDatabase(getTestDatabaseBuilder())`):
*   **`AppDatabaseMigrationTest`**: Validates schema upgrades from v1..v9 to v10, verifying table creation (`monthly_budgets`, `pantry_movements`, `product_categories`, `pending_sync_entries`, `app_key_values`), column evolution (`updatedAt`, `imageHash`, `isDeleted`), composite keys, indices, and 100% data preservation across all 9 tables.
*   **`PantryMovementDaoTest`**: Tests CRUD operations for `PantryMovementEntity`, timestamp range filtering, top consumed products aggregation, and reactive monthly expense tracking.
*   **`MonthlyBudgetDaoTest`**: Tests CRUD operations for `MonthlyBudgetEntity`, composite key updates, annual queries, and reactive monthly expense aggregation joins across shopping lists, items, and pantry entries.
*   **`RoomBudgetRepositoryTest`**: Tests domain boundary enforcement, monthly budget retrieval/updates, reactive expense aggregation, and full 12-month annual summaries.
*   **`RoomCatalogRepositoryTest`**: Tests EAN lookups, search, CRUD, LWW updates, media blob handling, validation limits, default category seeding, custom category creation, cascading category rename, cascading category delete to `"Outros"`, and `"Outros"` protection.
*   **`RoomPantryRepositoryTest`**: Tests pantry joins with categories, expiration filtering, single/batch consumption deductions, and negative balance guards.
*   **`RoomPriceRepositoryTest`**: Tests price history queries, latest price resolution, LWW conflict handling, and store name validations.
*   **`RoomShoppingListRepositoryTest`**: Tests shopping lists, item management, scratchpad items, LWW updates, and checkout (`finalizePurchase`).
*   **`RoomInvoiceRepositoryTest`**: Tests invoice QR code parsing delegation and atomic bulk pantry import with SQLite in-memory: creating new catalog products, reusing existing products by matching EAN or name, inserting pantry items, recording latest price history and purchase movements in `pantry_movements`.
*   **`KtorInvoiceRemoteDataSourceTest`**: Tests HTTP JSON request payload serialization, endpoint routing, and error status handling using Ktor's `MockEngine`.
*   **`AndroidHttpClientEngineTest`**: Tests Android platform OkHttp engine instantiation, custom `X509TrustManager` fallback for `*.domus.arpa` and development CAs, and dev hostname verification.
*   **`InvoiceMapperTest`**: Tests mapping between remote DTOs and domain models (`Invoice`, `InvoiceItem`) and measurement unit normalization (`parseMeasureUnit`).
*   **`RoomSyncAndAuthRepositoryTest`**: Tests `RoomAuthRepository` session lifecycle, `RoomHouseholdRepository` tenant management, and `RoomSyncRepository` bidirectional sync: outbox initial queueing, atomic product ID remapping protecting child foreign keys from `ON DELETE CASCADE`, LWW pulled entity application, outbox flushing before switch, and multi-tenant cache cleanup.
*   **`DataConsistencyTest`**: Cross-repository integration tests validating database cascade deletions, foreign key enforcement, and end-to-end multi-table transactions.
*   **`ConvertersTest`**: Tests Room type conversion for Instant and MeasureUnit.
*   **`AndroidImageProcessorTest` & `IosImageProcessorTest`**: Platform-specific thumbnail processing tests.

All tests in `:data` run under Gradle via `./gradlew :data:test` and `./gradlew :data:testDebugUnitTest`.

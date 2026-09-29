# Data Module Analysis

This document provides an in-depth analysis of the `:data` module, outlining its purpose, clean architecture implementation, Room persistence schema, mappers, DAOs, repositories, multiplatform utilities, and testing strategies.

## 1. Module Purpose

The `:data` module in **Minha Despensa** is a Kotlin Multiplatform (KMP) library responsible for data persistence and local storage. It acts as the single source of truth for all local data, implementing the repository interfaces declared in `:core:domain`.

Key capabilities:
*   **Offline-First Local Storage**: Uses Google Room Multiplatform (Version 4) backed by SQLite (`BundledSQLiteDriver`).
*   **Migration Pipeline**: Full schema evolution and migration strategy supporting incremental and multi-version jumps (`1->2`, `2->3`, `3->4`, `1->4`, `2->4`).
*   **Conflict Resolution**: Enforces Last-Write-Wins (LWW) conflict resolution on updates via SQL timestamp comparison (`updated_at < :updatedAt`).
*   **Atomic Transactions**: Protects multi-table operations (such as product creation with media, pantry consumption batches, and shopping checkout) using Room's `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`.
*   **Clean Architecture Separation**: Pure separation between Room database entities and core domain models via dedicated mappers in `com.bitlabbr.minhadespensa.data.local.mapper`.

---

## 2. Architecture & Directory Structure

```
data/src/commonMain/kotlin/com/bitlabbr/minhadespensa/data/
├── di/                     # Koin DataModule dependency injection
├── local/
│   ├── AppDatabase.kt      # Main Room database declaration (Version 4)
│   ├── converter/          # Room TypeConverters (Instant, MeasureUnit)
│   ├── dao/                # Room Data Access Objects (DAOs)
│   ├── dto/                # Room query projection DTOs (e.g. joins)
│   ├── entity/             # Room SQLite table entities
│   ├── mapper/             # Bidirectional Entity <-> Domain mappers
│   └── migration/          # Room Migration definitions (1->2, 2->3, 3->4, direct 1->4/2->4)
├── repository/             # Room repository implementations
└── util/                   # Common utilities (ImageProcessor expect interface)
```

Platform-specific source sets:
*   `data/src/androidMain/`: Android `actual` implementation of `ImageProcessor` utilizing Android `Bitmap` and `ExifInterface`.
*   `data/src/iosMain/`: iOS `actual` implementation of `ImageProcessor` utilizing `UIKit` and `CoreGraphics`.

---

## 3. Dependencies

### Internal:
*   `:core`: Consumes domain models (`CatalogProduct`, `PantryItem`, `PriceEntry`, `ShoppingList`, `ShoppingItem`, etc.), repository interfaces, domain use cases, and utilities (`AppLogger`, `CoreConstants`, `DiQualifiers`, `Helpers`).

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
*   **Entities**: `CatalogProductEntity`, `PantryItemEntity`, `PriceEntryEntity`, `ShoppingItemEntity`, `ProductMediaEntity`, `ShoppingListEntity`.
*   **Version**: `4`.
*   **Driver**: `BundledSQLiteDriver()`.
*   **Platform Initialization**: Uses `expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>` to instantiate platform SQLite drivers.

---

### 4.2. Database Entities (`local/entity/`)
*   **`CatalogProductEntity`** (`catalog_products`):
    *   *Columns*: `id` (PK), `ean` (Unique Index), `name`, `brand`, `measure_unit`, `net_weight`, `thumbnail_url`, `updated_at`, `is_deleted`, `manually_added`, `category`, `notes`.
*   **`PantryItemEntity`** (`pantry_items`):
    *   *Columns*: `id` (PK), `product_id` (FK to `catalog_products.id` on delete cascade), `quantity`, `expiration_date`, `batch_number`, `updated_at`, `is_deleted`.
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

---

### 4.3. Migrations (`local/migration/AppDatabaseMigrations.kt`)
*   **`MIGRATION_1_2`**: Adds `product_media` table with foreign key and index to `catalog_products`.
*   **`MIGRATION_2_3`**: Adds `category` and `notes` to `catalog_products`, creates `shopping_lists` table, and adds `listId` to `shopping_items`.
*   **`MIGRATION_3_4` (`migrateToSchemaV4`)**:
    *   Standardizes column casing and names (e.g. `budget_in_cents`, `list_id`, `product_id`, `raw_text`, `price_at_time`, `is_checked`, `updated_at`, `is_deleted`).
    *   Configures FK on `shopping_items.product_id` as `ON DELETE SET NULL` to support scratchpad items and preserve shopping logs if a product is physically deleted.
    *   Adds unique index on `catalog_products.ean`.
*   **`MIGRATION_1_4` & `MIGRATION_2_4`**: Direct upgrade paths to schema v4.
*   **`ALL_MIGRATIONS`**: Exported migration array consumed by Koin's database builder.

---

### 4.4. Data Transfer Objects (`local/dto/`)
*   **`PantryItemWithCategoryDaoResult`**:
    *   Flat Room query projection joining `pantry_items` with `catalog_products` (`name`, `category`, `measure_unit`, `net_weight`).

---

### 4.5. Domain & Entity Mappers (`local/mapper/`)

Centralized and decoupled from repositories:
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
*   **`ShoppingMapper.kt`**:
    *   `ShoppingListWithItems.toDomain()`: Preserves soft-deleted items (`isDeleted = true`) so domain synchronization, cart totals, and LWW state machines have full visibility into the item states.
    *   `ShoppingList.toEntity()`.
    *   `ShoppingItemEntity.toDomain()`.
    *   `ShoppingItem.toEntity()`.

---

### 4.6. Data Access Objects (`local/dao/`)
*   **`CatalogProductDao`**:
    *   `findByEan`, `findById`, `getAllActive`, `searchByNameOrBrand` (ordered by name ASC).
    *   `insert` (`ABORT`), `forceUpdateForProduct`, `updateProductIfNewer` (LWW with all product fields including notes), `markAsDeleted`, `deleteProductById`, `exists`.
*   **`PantryItemDao`**:
    *   `getAllActivePantryItems`, `getAllActivePantryItemsWithCategory`, `getPantryItemWithCategoryById`, `getExpiringPantryItems`.
    *   `getPantryItemById`, `getPantryItemsByProductId`.
    *   `insertPantryItem`, `forceUpdatePantryItem`, `updatePantryItemIfNewer`, `markPantryItemAsDeleted`, `deletePantryItemById`.
    *   *(Provides `typealias PantryRepositoryDao = PantryItemDao` for backwards compatibility).*
*   **`PriceEntryDao`**:
    *   `getPriceHistoryByProductId`, `getLatestPriceForProductId`, `findById`.
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
    *   Executes `insertProduct`, `forceUpdateProduct`, and `updateProductIfNewer` within immediate write transactions (`db.useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }`), guaranteeing atomicity between product catalog rows and binary media blobs.
    *   Aligns media timestamps directly with `product.updatedAt`.
*   **`RoomPantryRepository`**:
    *   Encapsulates database and DAO as private members.
    *   `consumePantryItem` and `consumeBatch`: Atomically validates stock, prevents consumption of soft-deleted items, and updates quantities inside an immediate transaction.
    *   `getExpiringPantryItems`: Accurately separates active non-expired items approaching the threshold from already expired items.
*   **`RoomPriceRepository`**:
    *   Encapsulates DAO as private member.
    *   Manages price history entries with LWW conflict protection.
*   **`RoomShoppingListRepository`**:
    *   Encapsulates database and DAOs as private members.
    *   `insertShoppingList`: Atomically saves list entity and its associated items.
    *   `finalizePurchase`: Atomically converts checked shopping items into new `PantryItem` inventory entries, records `PriceEntry` records when prices are present, unchecks items, and updates the list timestamp.

---

### 4.8. Dependency Injection (`di/DataModule.kt`)

Configures singletons and use case factories via Koin:
*   `AppDatabase`: Initialized with platform database builder, `addMigrations(*ALL_MIGRATIONS)`, `BundledSQLiteDriver`, and background `Dispatchers.IO`.
*   DAOs: `catalogDao`, `pantryDao`, `priceDao`, `shoppingListDao`.
*   Repositories: `RoomCatalogRepository`, `RoomPantryRepository`, `RoomPriceRepository`, `RoomShoppingListRepository`, injected with `AppDatabase` and `DiQualifiers.DATA_LOGGER`.
*   Use Case Factories: Registers all 11 domain use cases (`SaveCatalogProductUseCase`, `CheckEanStatusUseCase`, `AddPantryItemUseCase`, `CreatePlannedShoppingListUseCase`, `CreateQuickShoppingListUseCase`, `AddCatalogItemToShoppingListUseCase`, `AddOrUpdateCartItemUseCase`, `ReplaceCartItemUseCase`, `RemoveCartItemUseCase`, `StartShoppingSessionUseCase`, `FinalizeShoppingSessionUseCase`).

---

## 5. Testing & Verification

The test suite resides in `data/src/commonTest/` and `data/src/androidUnitTest/` using an in-memory SQLite database (`createInMemoryDatabase(getTestDatabaseBuilder())`):
*   **`AppDatabaseMigrationTest`**: Validates schema upgrades from v1, v2, v3 to v4, verifying column names, indices, foreign keys, and data preservation.
*   **`RoomCatalogRepositoryTest`**: Tests EAN lookups, search, CRUD, LWW updates, media blob handling, and validation limits.
*   **`RoomPantryRepositoryTest`**: Tests pantry joins with categories, expiration filtering, single/batch consumption deductions, and negative balance guards.
*   **`RoomPriceRepositoryTest`**: Tests price history queries, latest price resolution, LWW conflict handling, and store name validations.
*   **`RoomShoppingListRepositoryTest`**: Tests shopping lists, item management, scratchpad items, LWW updates, and checkout (`finalizePurchase`).
*   **`DataConsistencyTest`**: Cross-repository integration tests validating database cascade deletions, foreign key enforcement, and end-to-end multi-table transactions.
*   **`ConvertersTest`**: Tests Room type conversion for Instant and MeasureUnit.
*   **`AndroidImageProcessorTest` & `IosImageProcessorTest`**: Platform-specific thumbnail processing tests.

All 132 tests in `:data` run under Gradle via `./gradlew :data:test` and `./gradlew testDebugUnitTest`.

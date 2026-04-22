# Data Module Analysis

## 1. Module Purpose

The `:data` module in MinhaDespensa is a Kotlin Multiplatform (KMP) module primarily responsible for managing and persisting application data. It acts as the single source of truth for local data storage, abstracting the underlying persistence mechanism (Room database) from the domain and presentation layers. It provides concrete implementations for the repository interfaces defined in the `:core:domain` module.

## 2. Architecture

The module follows a clean architecture approach, specifically focusing on the data layer.

*   **Kotlin Multiplatform (KMP)**: The core logic, including Room database definitions, entities, DAOs, and repository implementations, resides in `commonMain`. Platform-specific implementations (e.g., `ImageProcessor`) are provided in `androidMain` and `iosMain`.
*   **Room Persistence Library**: Used for local data storage, providing an SQLite abstraction layer.
*   **Repositories**: Implementations of domain-layer repository interfaces, handling data operations (CRUD, LWW conflict resolution) and mapping between domain models and Room entities.
*   **DAOs (Data Access Objects)**: Define the methods for interacting with the Room database tables.
*   **Entities**: Represent the schema of the database tables.
*   **Dependency Injection (Koin)**: `DataModule` configures and provides instances of the database, DAOs, and repositories.

## 3. Dependencies

### Internal Dependencies:

*   `:core:domain`: Depends on interfaces defined in the domain layer (e.g., `PriceRepository`, `PantryRepository`, `CatalogRepository`, `ShoppingListRepository`, `AppLogger`, `ImageProcessor`).

### External Dependencies:

*   **Room**: For local database persistence.
*   **Koin**: For dependency injection.
*   **Kotlinx Coroutines**: For asynchronous operations and Flow.
*   **Kotlinx Datetime**: For handling timestamps.
*   **Kotlinx UUID**: For UUID generation and parsing.
*   **Android Graphics/Media (Android-specific)**: For image processing on Android.
*   **UIKit/CoreGraphics/Foundation (iOS-specific)**: For image processing on iOS.

## 4. Key Components

### 4.1. Repositories

These classes implement the repository interfaces from the `:core:domain` module and interact with the Room DAOs. They handle data mapping between domain models and Room entities, and often include data validation and conflict resolution logic (Last-Write-Wins - LWW).

*   **`RoomPriceRepository`**:
    *   **Purpose**: Manages price entry data for products.
    *   **Key Methods**: `getPriceHistoryByProductId`, `getLatestPriceForProductID`, `insertPriceEntry`, `forceUpdatePriceEntry`, `updatePriceEntryIfNewer` (LWW), `markPriceEntryAsDeletedById`, `deletePriceEntryById`.
    *   **Dependencies**: `AppDatabase`, `AppLogger`.
*   **`RoomPantryRepository`**:
    *   **Purpose**: Manages pantry item data.
    *   **Key Methods**: `getAllActivePantryItems`, `insertPantryItem`, `forceUpdatePantryItem`, `updatePantryItemIfNewer` (LWW), `markPantryItemAsDeleted`, `deletePantryItemById`, `getPantryItemsByID`, `getPantryItemsByProductID`.
    *   **Dependencies**: `AppDatabase`, `AppLogger`.
*   **`RoomCatalogRepository`**:
    *   **Purpose**: Manages product catalog data, including product details and images.
    *   **Key Methods**: `getProductByEan`, `getProductById`, `getAllActives`, `searchProductsByNameOrBrand`, `insertProduct` (with image handling), `forceUpdateForProduct` (with image handling), `updateForProductIfNewer` (LWW with image handling), `deleteProductById`, `exists`.
    *   **Dependencies**: `AppDatabase`, `AppLogger`. Uses Room transactions for atomic operations involving product and media data.
*   **`RoomShoppingListRepository`**:
    *   **Purpose**: Manages shopping lists and their items.
    *   **Key Methods**: `getAllActiveShoppingLists`, `getShoppingListById`, `insertShoppingList`, `forceUpdateForShoppingList`, `updateShoppingListIfNewer` (LWW), `markShoppingListAsDeleted`, `deleteShoppingListById`, `insertShoppingItem`, `forceUpdateForShoppingItem`, `updateShoppingItemIfNewer` (LWW), `toggleItemCheck`, `markAsDeleted`, `finalizePurchase`.
    *   **Dependencies**: `AppDatabase`, `AppLogger`. `finalizePurchase` is a complex operation that moves checked items to the pantry and records their prices.

### 4.2. Local Data (Room)

#### `AppDatabase.kt`

*   **Purpose**: The main Room database class. It defines the database version, lists all entities, and provides abstract methods to access the DAOs. It also integrates `TypeConverters` for custom type handling.
*   **Entities**: `CatalogProductEntity`, `PantryItemEntity`, `PriceEntryEntity`, `ShoppingItemEntity`, `ProductMediaEntity`, `ShoppingListEntity`.
*   **Version**: `3`.
*   **Multiplatform**: Uses `expect object AppDatabaseConstructor` for platform-specific database initialization.

#### Entities (`.kt` files in `data/src/commonMain/kotlin/com/bitlabbr/minhadespensa/data/local/entity`)

These classes define the structure of the tables in the Room database.

*   **`CatalogProductEntity`**: Represents a product in the catalog. Fields include `id`, `ean`, `name`, `brand`, `measureUnit`, `netWeight`, `thumbnailUrl`, `updatedAt`, `isDeleted`, `manuallyAdded`. Has a unique index on `ean`.
*   **`PantryItemEntity`**: Represents an item in the user's pantry. Fields include `id`, `productId` (foreign key to `CatalogProductEntity`), `quantity`, `expirationDate`, `batchNumber`, `updatedAt`, `isDeleted`.
*   **`PriceEntryEntity`**: Stores historical price information for products. Fields include `id`, `productId` (foreign key to `CatalogProductEntity`), `priceInCents`, `storeName`, `updatedAt`, `isDeleted`.
*   **`ProductMediaEntity`**: Stores binary data (e.g., images) for products. Fields include `productId` (primary key and foreign key to `CatalogProductEntity`), `blob` (ByteArray), `updatedAt`.
*   **`ShoppingItemEntity`**: Represents an item within a shopping list. Fields include `id`, `productId` (foreign key to `CatalogProductEntity`), `listId` (foreign key to `ShoppingListEntity`), `quantity`, `priceAtTime`, `isChecked`, `updatedAt`, `isDeleted`.
*   **`ShoppingListEntity`**: Represents a shopping list. Fields include `id`, `updatedAt`, `name`, `budgetInCents`, `isDeleted`.
*   **`ShoppingListWithItems`**: A data class used by Room to represent a `ShoppingListEntity` along with its associated `ShoppingItemEntity`s using `@Relation`.

#### DAOs (`.kt` files in `data/src/commonMain/kotlin/com/bitlabbr/minhadespensa/data/local/dao`)

These interfaces define the methods for database interactions.

*   **`CatalogProductDao`**: Provides methods for inserting, updating (force and LWW), searching, retrieving, marking as deleted, and deleting `CatalogProductEntity` objects.
*   **`PantryRepositoryDao`**: Provides methods for inserting, retrieving (by product ID, all active), marking as deleted, force updating, LWW updating, and deleting `PantryItemEntity` objects.
*   **`PriceEntryDao`**: Provides methods for inserting, retrieving price history, getting the latest price, marking as deleted, deleting, force updating, and LWW updating `PriceEntryEntity` objects.
*   **`ProductMediaDao`**: Provides methods for inserting/updating, retrieving, and deleting `ProductMediaEntity` objects by `productId`.
*   **`ShoppingItemDao`**: Provides methods for retrieving active items, inserting, finding by ID, force updating, LWW updating, toggling check status, logically deleting all, and marking as deleted for `ShoppingItemEntity` objects.
*   **`ShoppingListDao`**: Provides methods for retrieving active shopping lists (with items), getting a shopping list by ID (with items), inserting lists and items, force updating, LWW updating, marking as deleted, deleting, and updating the timestamp for `ShoppingListEntity` objects.

#### `Converters.kt`

*   **Purpose**: Contains `TypeConverter` methods for Room to handle custom data types, specifically `Instant` (from `kotlinx.datetime`) and `MeasureUnit` (from `:core:domain`).

### 4.3. Dependency Injection (`di/DataModule.kt`)

*   **`dataModule`**: A Koin module that configures and provides singletons for:
    *   `AppDatabase`: The Room database instance, initialized with `BundledSQLiteDriver` and `Dispatchers.IO` for background operations.
    *   All DAOs: `catalogDao`, `pantryDao`, `priceDao`, `shoppingListDao`.
    *   All Repository implementations: `RoomCatalogRepository`, `RoomPantryRepository`, `RoomPriceRepository`, `RoomShoppingListRepository`. These repositories are injected with `AppDatabase` and an `AppLogger` (qualified as `DiQualifiers.DATA_LOGGER`).

### 4.4. Utilities (`util/ImageProcessor.kt`)

*   **`ImageProcessor` (commonMain)**: An `expect` interface defining the `processForThumbnail` method.
*   **`ImageProcessorImpl` (androidMain)**: The `actual` implementation for Android, using `android.graphics.Bitmap` and `ExifInterface` to decode, fix orientation, center crop, scale, and compress images into a thumbnail.
*   **`ImageProcessorImpl` (iosMain)**: The `actual` implementation for iOS, using `UIImage` and `CoreGraphics` to resize and compress images into a thumbnail.

## 5. Usage

Other modules (e.g., `:core:domain`, `:features:`) interact with the `:data` module primarily through the repository interfaces defined in `:core:domain`. These interfaces are then provided by the Koin `dataModule` with their `Room`-based implementations.

Example: A use case in the domain layer would depend on `PriceRepository`, and at runtime, Koin would inject `RoomPriceRepository`.

## 6. Public APIs

The primary public APIs of the `:data` module are the concrete implementations of the repository interfaces:

*   `com.bitlabbr.minhadespensa.data.repository.RoomPriceRepository`
*   `com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository`
*   `com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository`
*   `com.bitlabbr.minhadespensa.data.repository.RoomShoppingListRepository`

Additionally, the `dataModule` in `com.bitlabbr.minhadespensa.data.di` is a public API for configuring dependency injection for this module.

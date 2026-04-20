# Core Module Analysis

This document provides an analysis of the `:core` module, outlining its purpose, architecture, dependencies, and key components.

## 1. Module Purpose & Technology Stack

The `:core` module serves as the central domain layer for the Minha Dispensa application.
It is built using **Kotlin Multiplatform (KMP)**, targeting both Android and iOS, and functions as an Android library.

Key technologies and architectural aspects include:
*   **Kotlin Multiplatform (KMP):** Enables code sharing across Android and iOS platforms.
*   **Serialization:** Utilizes `kotlinx.serialization` (specifically `kotlinx.serialization.json`) for efficient and type-safe data serialization and deserialization.
*   **Asynchronous Programming:** Employs `kotlinx.coroutines.core` for managing asynchronous operations, ensuring a responsive user experience.
*   **Dependency Injection:** Integrates `koin.core` for managing dependencies, promoting modularity and testability. Koin is exposed as an `api` dependency, allowing consuming modules to leverage it.
*   **Date/Time Handling:** Uses `kotlinx.datetime` for robust and multiplatform-compatible date and time operations, also exposed as an `api` dependency.
*   **Clean Architecture Principles:** The module's structure (e.g., `domain/model`, `domain/repository`, `domain/util`) strongly suggests adherence to Clean Architecture, separating concerns and promoting maintainability.

## 2. Dependencies

The `build.gradle.kts` file indicates the following primary dependencies:

*   `implementation(libs.kotlinx.coroutines.core)`: Kotlin Coroutines for asynchronous programming.
*   `implementation(libs.kotlinx.serialization.json)`: Kotlinx Serialization for JSON handling.
*   `api(libs.koin.core)`: Koin for dependency injection (exposed to consumers).
*   `api(libs.kotlinx.datetime)`: Kotlinx DateTime for date and time utilities (exposed to consumers).
*   `implementation(libs.kotlin.test)`: For common test code.
*   `implementation(libs.kotlinx.coroutines.test)`: For testing coroutines.

## 3. Android Specifics

*   **Namespace:** `com.bitlabbr.minhadespensa.core`
*   **Compile SDK:** Uses the version defined by `libs.versions.android.compileSdk` version 36.
*   **Min SDK:** Uses the version defined by `libs.versions.android.minSdk` version 24.
*   **Java Compatibility:** Configured to use Java 11 for both source and target compatibility.

## 4. Domain Layer Analysis

The `core/src/commonMain/kotlin/com/bitlabbr/minhadespensa/core/domain` package contains the core business logic, data models, and interfaces.

### 4.1. Models (`domain/model`)

This package defines the core data entities used throughout the application. All `data class` models are `Serializable` using `kotlinx.serialization`, indicating they are designed for easy persistence and network transfer.

*   **`PantryItem.kt`**: Defines `PantryItem`, representing an item stored in the user's pantry, including details like `productId`, `quantity`, `expirationDate`, and `batchNumber`.
*   **`PriceEntry.kt`**: Defines `PriceEntry`, capturing price information for a product at a specific store and time.
*   **`IconKeys.kt`**: An `object` containing `const val` string identifiers for various icons, categorized by product type (e.g., beverages, cleaning, grains). This is likely used to map product categories to specific UI icons.
*   **`MeasureUnit.kt`**: An `enum class` defining standard units of measurement such as `UNITY`, `KILOGRAM`, `LITER`, and `PACKAGE`.
*   **`ShoppingItem.kt`**: Defines `ShoppingItem`, representing an individual item within a shopping list, including `productId`, `quantity`, `listID`, and a `isChecked` status.
*   **`ShoppingList.kt`**: Defines `ShoppingList`, representing a collection of `ShoppingItem`s, with properties like `name` and an optional `budgetInCents`.
*   **`CatalogProduct.kt`**: Defines `CatalogProduct`, representing a product in a broader catalog, with attributes like `ean`, `name`, `brand`, `measureUnit`, `netWeight`, and `thumbnailUrl`.
*   **`ExpiringItemCard.kt`**: A UI-specific `data class` designed to display information about expiring pantry items, including labels, progress, and icon URIs.
*   **`ConsumptionTrendItemCard.kt`**: A UI-specific `data class` for presenting product consumption trends, including product details, consumption amount, and icon URIs.

### 4.2. Repositories (`domain/repository`)

This package defines interfaces for data access operations, adhering to the repository pattern. These interfaces abstract the data source, allowing different implementations (e.g., local database, remote API). All methods are `suspend` for asynchronous execution and many return `Flow` for reactive data streams.

*   **`PantryRepository.kt`**: Interface for managing `PantryItem` data, including methods for retrieving items by ID or product ID, getting all active items, inserting, updating, and marking items as deleted.
*   **`PriceRepository.kt`**: Interface for managing `PriceEntry` data, providing methods to get price history, the latest price for a product, insert, update, and delete price entries.
*   **`CatalogRepository.kt`**: Interface for managing `CatalogProduct` data, with functions to retrieve products by EAN or ID, get all active products, search by name/brand, insert, update, and delete products. It also handles image bytes for thumbnails.
*   **`ShoppingListRepository.kt`**: Interface for managing `ShoppingList` and `ShoppingItem` data. It includes methods for retrieving shopping lists, inserting, updating, deleting lists, and also for managing individual shopping items within a list (insert, update, toggle check, mark as deleted, finalize purchase).

### 4.3. Utilities (`domain/util`)

This package contains general utility classes and interfaces that support the domain logic.

*   **`AppLogger.kt`**: An interface defining basic logging functionalities (`d` for debug, `e` for error), promoting a consistent logging approach across platforms.
*   **`ConsoleLogger.kt`**: A concrete multiplatform implementation of `AppLogger` that prints log messages to the console, including timestamps, log levels, and module information.
*   **`DiQualifiers.kt`**: An `object` containing string constants used as qualifiers for dependency injection (likely with Koin). This allows for injecting specific instances of dependencies based on their intended use (e.g., `APP_LOGGER`, `CORE_LOGGER`).
*   **`Helpers.kt`**: Provides utility functions such as `getCurrentTime()` (returning epoch milliseconds) and `isValidTimestamp()` for validating timestamp values within a reasonable range.
*   **`ImageProcessor.kt`**: An interface for image processing, specifically for generating compressed WebP thumbnails (max 300x300px) from raw image `ByteArray` input. This suggests the module handles image manipulation for product visuals.

## 5. Main Architecture of the Module

The `:core` module is structured following a clear **Clean Architecture** approach, focusing on the domain layer.

*   **Domain Models:** Defined in `domain/model`, these are plain Kotlin data classes that represent the core business entities, independent of any specific framework or data source.
*   **Domain Repositories:** Defined as interfaces in `domain/repository`, these abstract the data access logic. They specify *what* data operations can be performed, without dictating *how* they are performed. This separation allows for different data source implementations (e.g., local database, network API) in other modules (e.g., `:data`).
*   **Domain Utilities:** Located in `domain/util`, these provide cross-cutting concerns and helper functions that are essential for the domain logic but do not belong to specific entities or repositories.

The module's multiplatform nature is evident in its `commonMain` source set, ensuring that these core definitions are available across all targeted platforms (Android and iOS). The use of `Flow` and `suspend` functions indicates a modern, reactive, and asynchronous programming paradigm.

## 6. Public APIs Calls

The public APIs of the `:core` module are primarily exposed through its **repository interfaces** (`PantryRepository`, `PriceRepository`, `CatalogRepository`, `ShoppingListRepository`) and the **utility interfaces** (`AppLogger`, `ImageProcessor`). Any module depending on `:core` can interact with the application's core business logic by injecting and using implementations of these interfaces.

The `api` dependencies on `koin.core` and `kotlinx.datetime` also mean that these libraries are part of the public API contract, and modules consuming `:core` will implicitly have access to them.

## 7. Conclusion

The `:core` module is a well-structured, multiplatform domain layer that encapsulates the essential business logic and data definitions for the Minha Dispensa application. Its adherence to Clean Architecture principles, use of modern Kotlin features (coroutines, flows, serialization), and clear separation of concerns make it a robust and maintainable foundation for the application. It defines the "what" of the application's operations, leaving the "how" to other modules.
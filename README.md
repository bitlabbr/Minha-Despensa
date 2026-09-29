# MinhaDespensa 🛒

[![CI Check](https://github.com/bitlabbr/Minha-Despensa/actions/workflows/ci.yml/badge.svg)](https://github.com/bitlabbr/Minha-Despensa/actions/workflows/ci.yml)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpack-compose&logoColor=white)
![Room](https://img.shields.io/badge/Room-KMP-3DDC84?logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

**MinhaDespensa** is a Kotlin Multiplatform application for household inventory, grocery shopping, budget control, and price management.

The project connects the main stages of the household shopping cycle — **product catalog, shopping lists, purchases, price history, and pantry inventory** — in a single local-first application for Android and iOS.

> The project originally started as a smart shopping cart calculator focused on avoiding surprises at checkout. It has since evolved into a broader household inventory and grocery management platform.

---

## 🎯 Why MinhaDespensa?

Grocery shopping often involves several disconnected decisions:

- How much am I spending?
- Is this product actually cheaper than the last time I bought it?
- Do I already have this item at home?
- What is close to expiring?
- What should I buy again soon?
- Will my shopping list stay within budget?

MinhaDespensa aims to bring these decisions together in one application.

Instead of treating the shopping list, pantry, and price history as isolated features, the project models them as parts of the same lifecycle.

```text
Product Catalog
      │
      ▼
Shopping List
      │
      ▼
Finalize Purchase
   ┌──┴──────────────┐
   ▼                 ▼
Pantry          Price History
   │
   ▼
Consumption
   │
   ▼
Future Shopping Needs
```

---

# ✨ Current Features

The project is under active development. The sections below distinguish between features already implemented in the domain/data layers and user-facing flows that are still being completed.

## 📦 Product Catalog

The application already contains a persistent local product catalog backed by Room.

Implemented capabilities include:

- Product creation and persistence
- Search by product ID
- Search by EAN/barcode
- Search by product name or brand
- Active product listing
- Product categories
- Brand information
- Measurement units
- Net weight
- Product thumbnail storage
- Soft deletion
- Physical deletion
- Timestamp-aware updates
- UUID validation
- EAN-8, EAN-13, and EAN-14 validation

Product images are stored separately from catalog metadata through a dedicated media entity.

---

## 🛒 Shopping Lists

Shopping lists are modeled as persistent entities supporting diverse household shopping workflows.

Current data/domain capabilities include:

- **List Types**: Scratchpad notes (`SCRATCHPAD`), structured planned lists (`PLANNED`), and active shopping assistant sessions (`ASSISTANT`)
- **Lifecycle Statuses**: `DRAFT`, `SHOPPING`, `COMPLETED`, and `CANCELLED`
- **Budget Tracking**: Optional budget per list (`budgetInCents`) with live cart total comparison and `isOverBudget` guard
- **Flexible Items**: Supports both catalog-linked items (`productId`) and scratchpad notes (`rawText`)
- **Dynamic Computed Metrics**:
  - `totalActiveItems`: non-deleted item count
  - `totalCheckedItems`: items checked during shopping
  - `totalCartInCents`: total monetary value of checked items
  - `subtotalInCents`: price-per-quantity calculation per item
- **Soft Deletion & LWW Synchronization**: Retains versioning for synchronization
- **Retrieval**: Reactive `Flow` queries fetching lists together with their items

---

## 💳 Purchase Finalization

MinhaDespensa implements an atomic checkout flow executed inside a SQLite write transaction (`withTransaction(IMMEDIATE)`).

When a shopping list is finalized, checked items are:

1. Added to the pantry inventory as active `PantryItem` records
2. Recorded in `PriceEntry` history if a price was entered at the time of purchase
3. Automatically unchecked in the list while preserving items for future reuse
4. Updated with the latest checkout timestamp

```text
Shopping Item (Checked)
     │
     │ finalizePurchase() [SQLite IMMEDIATE Transaction]
     ▼
 ┌───────────────┬───────────────┐
 │               │               │
 ▼               ▼               ▼
Pantry      Price History   List State Reset
(Inventory)   (Tracker)     (Uncheck Items)
```

---

## 🏠 Pantry Management & Consumption

The pantry module contains full domain, repository, DAO, ViewModel, and UI implementations.

Current capabilities include:

- **Persistent Pantry Items**: Stock tracking with quantity, batch number, and expiration date
- **Category Joins**: Reactive queries combining pantry items with catalog categories and net weight
- **Consumption Flows**:
  - `consumePantryItem`: Individual item stock reduction with negative stock guards
  - `consumeBatch`: Atomic multi-item consumption for recipes or bulk usage
- **Expiration Tracking**: Strict separation of items nearing expiration threshold (`getExpiringPantryItems`) from already expired items
- **Soft Deletion & LWW**: Conflict-safe updates preserving synchronization integrity
- **Reactive UI**: StateFlow-driven presentation for real-time pantry updates

---

## 🔍 Product Details & Price Evolution

MinhaDespensa features a dedicated product details experience accessible from both the catalog and pantry flows:

- **Product Summary**: Brand, category, measurement unit, net weight, notes, and barcode
- **Price History Visualizer**: Interactive chart plotting price changes across shopping sessions over time
- **Pantry Batches**: Visual list of all lots in stock with respective expiration dates
- **Action Shortcuts**: Quick buttons to add to an active shopping list, increment pantry stock, or edit product details

---

## ⏳ Expiration Tracking

The project already models expiration dates for pantry items and contains repository/UI support for retrieving products close to expiration.

The Pantry ViewModel maintains separate state for:

- all active pantry items;
- products close to expiration;
- the currently selected pantry item.

The complete expiration experience — including push notifications — is still under development.

---

## 💰 Price History

Price history is already part of the persistent data model.

Implemented operations include:

- Registering product prices
- Retrieving the complete price history for a product
- Retrieving the latest known price
- Associating prices with a store/source
- Soft deletion
- Timestamp-based updates

When a purchase is finalized, the price recorded in the shopping list automatically becomes a new historical price entry.

---

## ➕ Product Registration UI

A Compose Multiplatform unified product registration sheet (`RegisterProductBottomSheet`) is integrated across the app:

- Product photo capture and thumbnail preview
- Product name, brand, and category selection
- Measurement unit and numeric net weight validation
- Barcode field with live CameraX + Google ML Kit barcode scanner
- Inline form validation via `ProductFormValidator` and debounced EAN verification
- Notes and additional metadata

---

## ⚡ Domain Use Cases

The `:core` domain layer defines 11 use cases that orchestrate application logic independently of UI and database frameworks:

1. **`SaveCatalogProductUseCase`**: Validates constraints (name, category, EAN lengths/digits, positive weight) and saves catalog products with image blobs.
2. **`CheckEanStatusUseCase`**: Validates barcode formatting and verifies product existence by EAN.
3. **`AddPantryItemUseCase`**: Enforces inventory preconditions and stores active pantry stock.
4. **`CreatePlannedShoppingListUseCase`**: Builds structured shopping lists with budget thresholds and planned items (`ShoppingListType.PLANNED`).
5. **`CreateQuickShoppingListUseCase`**: Creates quick scratchpad shopping lists (`ShoppingListType.SCRATCHPAD`) parsing unformatted line items.
6. **`AddCatalogItemToShoppingListUseCase`**: Attaches catalog products to shopping lists with pre-filled defaults.
7. **`AddOrUpdateCartItemUseCase`**: Manages dynamic adjustments (quantity, price, checked state) during shopping trips.
8. **`ReplaceCartItemUseCase`**: Swaps items in the cart with alternative catalog products or custom items during active shopping.
9. **`RemoveCartItemUseCase`**: Soft-deletes items from active shopping lists.
10. **`StartShoppingSessionUseCase`**: Transitions shopping lists into the active `ShoppingListStatus.SHOPPING` state.
11. **`FinalizeShoppingSessionUseCase`**: Atomically orchestrates purchase finalization across pantry, price history, and shopping lists.

---

# 🚧 Development Status

| Feature | Status |
|---|---|
| Kotlin Multiplatform Android/iOS foundation | ✅ Implemented |
| Compose Multiplatform UI | ✅ Implemented |
| Clean Architecture + MVVM | ✅ Implemented |
| Koin dependency injection | ✅ Implemented |
| Room KMP persistence (v4 + migrations) | ✅ Implemented |
| Local product catalog | ✅ Implemented |
| Search by EAN | ✅ Implemented (Domain / Data / Usecase) |
| Search by name / brand | ✅ Implemented (Domain / Data) |
| Shopping list persistence & types | ✅ Implemented (Planned, Scratchpad, Assistant) |
| Shopping list budget & cart analytics | ✅ Implemented |
| Shopping checklist state & assistant | ✅ Implemented |
| Purchase finalization (atomic transaction) | ✅ Implemented |
| Automatic pantry entry after purchase | ✅ Implemented |
| Price history tracking & charts | ✅ Implemented |
| Product Details screen & batch view | ✅ Implemented |
| Pantry domain and persistence | ✅ Implemented |
| Pantry single & batch consumption | ✅ Implemented |
| Pantry reactive ViewModel | ✅ Implemented |
| Automated CI & Unit Tests (279 tests) | ✅ Implemented |
| Barcode scanner integration (CameraX/ML Kit) | ✅ Implemented |
| Unified product registration sheet | ✅ Implemented |
| Expiration tracking & thresholds | 🚧 In development |
| Expiration system notifications | 🚧 In development |
| Automatic shopping suggestions | 🗺️ Roadmap |
| Consumption intelligence | 🗺️ Roadmap |
| Recipe suggestions | 🗺️ Roadmap |

---

# 🧠 Architecture

MinhaDespensa follows **Clean Architecture** principles combined with **MVVM** in the presentation layer.

The project is modularized to keep presentation, domain rules, data access, and application integration separated.

```text
┌─────────────────────────────┐
│         :composeApp         │
│                             │
│ Entry point and integration │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│          :uisystem          │
│                             │
│ Screens                     │
│ ViewModels                  │
│ UI State                    │
│ Components                  │
│ Design System               │
│ Theme                       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│            :core            │
│                             │
│ Domain Models               │
│ Repository Interfaces       │
│ Business Contracts          │
│ Shared Utilities            │
└──────────────▲──────────────┘
               │
               │ implements
               │
┌──────────────┴──────────────┐
│            :data            │
│                             │
│ Room Database               │
│ Entities                    │
│ DAOs                        │
│ Repository Implementations  │
│ Platform-specific services  │
└─────────────────────────────┘
```

---

## 🧩 Module Structure

| Module | Responsibility | Layer |
|---|---|---|
| `:composeApp` | Application entry point, integration, and dependency configuration | Application |
| `:uisystem` | Screens, reusable UI components, ViewModels, UI state, theme, and design system | Presentation |
| `:core` | Domain models, use cases, repository interfaces, shared contracts, and business abstractions | Domain |
| `:data` | Room database, DAOs, entities, mappers, repository implementations, and platform-specific data utilities | Data / Infrastructure |

---

# 🔄 Reactive Data Flow

The presentation layer uses Kotlin Coroutines and Flow to observe persistent data reactively.

A typical flow looks like:

```text
Room / DAO
    │
    ▼
Repository
    │
    │ Flow<T>
    ▼
ViewModel
    │
    │ StateFlow<UiState>
    ▼
Compose UI
```

For example, the pantry ViewModel combines the stream of all active pantry items with the stream of expiring items and exposes the result through `StateFlow`.

---

# 🗃️ Local Persistence

MinhaDespensa uses **Room KMP (Schema Version 4)** backed by SQLite (`BundledSQLiteDriver`) as its local persistence layer.

The project features a defensive SQLite migration pipeline (`MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_1_4`, `MIGRATION_2_4`) ensuring seamless schema evolution across versions.

The current database model includes entities for:

```text
CatalogProductEntity
PantryItemEntity
PriceEntryEntity
ProductMediaEntity
ShoppingListEntity
ShoppingItemEntity
```

Relationships between these entities allow the application to avoid duplicating product information across shopping lists, pantry items, and price records.

For example:

```text
CatalogProduct
     │
     ├──── ShoppingItem
     │
     ├──── PantryItem
     │
     └──── PriceEntry
```

---

# 🔁 Timestamp-aware Updates

Repositories use `updatedAt` timestamps to avoid replacing newer local data with older versions.

Several entities implement update operations using a Last-Write-Wins-style strategy:

```text
Incoming data
     │
     ▼
Compare updatedAt
     │
 ┌───┴───────────┐
 │               │
newer          older
 │               │
 ▼               ▼
update          ignore
```

Soft deletion is also supported through `isDeleted`, allowing deleted records to retain version information when necessary.

---

# 🖼️ Image Processing

Product image handling is designed for Kotlin Multiplatform.

The shared domain exposes an image-processing abstraction while Android and iOS provide platform-specific implementations.

```text
commonMain
    │
    ▼
ImageProcessor
   /          \
Android       iOS
Bitmap        UIImage
```

The current image pipeline supports thumbnail-oriented processing and compressed product media storage.

---

# 🎨 UI & Design System

The UI is built with **Compose Multiplatform** and uses a shared design system for Android and iOS.

The `:uisystem` module contains:

- Reusable text components
- Navigation components
- Structural layout components
- Charts
- Theme
- Typography
- Dimensions
- Application background
- Custom glass-effect containers

Two important design-system containers are:

```text
PrimaryContainerGlassCard
└── Main structural container

SecondaryContainerGlassCard
└── Internal widget / content container
```

These components are used to maintain a consistent glass-inspired visual hierarchy throughout the application.

---

# 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Multiplatform | Kotlin Multiplatform |
| Android | Android / Jetpack |
| iOS | Kotlin Multiplatform + native iOS APIs |
| UI | Compose Multiplatform |
| Architecture | Clean Architecture + MVVM |
| Dependency Injection | Koin |
| Local Database | Room KMP / SQLite |
| Async / Reactive | Kotlin Coroutines + Flow |
| UI State | StateFlow |
| Serialization | kotlinx.serialization |
| Date & Time | kotlinx.datetime |
| UUID | kotlin.uuid |
| Logging | Custom `AppLogger` abstraction |
| Android image processing | Bitmap / ExifInterface |
| iOS image processing | UIImage / CoreGraphics |

---

# 📂 Project Structure

A simplified representation of the project:

```text
MinhaDespensa/
│
├── composeApp/
│
├── core/
│   └── src/
│       └── commonMain/
│           └── kotlin/
│               └── com/bitlabbr/minhadespensa/core/
│                   └── domain/
│                       ├── model/
│                       ├── usecase/
│                       ├── repository/
│                       └── util/
│
├── data/
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/
│       │       └── com/bitlabbr/minhadespensa/data/
│       │           ├── local/
│       │           │   ├── dao/
│       │           │   ├── entity/
│       │           │   ├── dto/
│       │           │   ├── mapper/
│       │           │   └── converter/
│       │           ├── repository/
│       │           └── di/
│       │
│       ├── androidMain/
│       └── iosMain/
│
├── uisystem/
│   └── src/
│       └── commonMain/
│           └── kotlin/
│               └── com/bitlabbr/minhadespensa/uisystem/
│                   ├── components/
│                   ├── features/
│                   │   ├── home/
│                   │   ├── list/
│                   │   ├── pantry/
│                   │   └── product/
│                   ├── theme/
│                   └── di/
│
├── iosApp/
│
└── README.md
```

---

# 🧪 Testing & Continuous Integration

MinhaDespensa maintains a comprehensive automated testing suite with **279 unit tests**:

- **`:core` (52 tests)**: Multiplatform tests verifying 11 use case business rules, barcode validation, domain metrics, clock-drift tolerances, and model serialization.
- **`:data` (132 tests)**: In-memory Room SQLite tests validating DAOs, Last-Write-Wins (LWW) conflict handling, entity-domain mappers, cascade deletes, Room v1-v4 schema migrations, and multi-table transactions (`finalizePurchase`, `consumeBatch`).
- **`:uisystem` (94 tests)**: ViewModel state machines, form validation rules, currency formatting (`R$ 1.234,56`), and notification bus events.
- **`:composeApp` (1 test)**: Multiplatform application integration tests.

The entire test suite is executed continuously on every Pull Request and branch push to `main`, `dev`, and `epic/**` via **GitHub Actions** ([`ci.yml`](.github/workflows/ci.yml)).

Run tests locally with Gradle:

```bash
# Run all unit tests across all modules
./gradlew testDebugUnitTest

# Run specific module test suites
./gradlew :core:test
./gradlew :data:test
```

---

# 🗺️ Roadmap

MinhaDespensa is being developed incrementally.

Planned directions include:

### 🧠 Smarter Shopping

- Automatic shopping list suggestions
- Replenishment suggestions based on pantry levels
- Historical purchase analysis
- Better budget forecasting

### 📊 Price Intelligence

- Price variation visualization
- Lowest / highest historical price
- Average product price
- Store comparison
- Detection of potentially misleading promotions

### 🏠 Pantry Intelligence

- Low-stock detection
- Consumption estimates
- Expiration alerts
- Product batch management improvements
- Waste reduction insights

### 🍳 Recipes

- Recipe suggestions using products currently available in the pantry
- Missing ingredient detection
- Automatic addition of missing ingredients to shopping lists

### ☁️ Future Synchronization

The current architecture uses UUIDs, timestamps, soft deletion, and timestamp-aware update semantics.

These mechanisms provide a foundation that may be used in future synchronization scenarios, although remote/cloud synchronization is not currently part of the implemented feature set.

---

# 📱 Running the Project

## Prerequisites

- Android Studio or IntelliJ IDEA with Kotlin Multiplatform support
- Xcode for iOS development
- JDK 17 or newer
- Android SDK
- Kotlin Multiplatform tooling configured

## Clone

```bash
git clone https://github.com/bitlabbr/Minha-Despensa.git
cd Minha-Despensa
```

Open the project in Android Studio or IntelliJ IDEA and wait for Gradle synchronization to complete.

---

## Android

Select the `composeApp` Android run configuration and launch the project on an emulator or physical device.

---

## iOS

Open the iOS project/workspace in Xcode or use an available Kotlin Multiplatform iOS run configuration from the IDE.

An Apple development environment is required to build and run the iOS target.

---

# 🧪 Development Philosophy

MinhaDespensa is also intended as a practical exploration of modern Kotlin Multiplatform application architecture.

The project prioritizes:

- Clear separation of concerns
- Shared business logic
- Shared Compose UI where appropriate
- Platform-specific implementations only when necessary
- Reactive state management
- Repository abstractions
- Local-first persistence
- Modular architecture
- Maintainable domain models
- Incremental feature development

---

# 🤝 Contributing

The project is currently under active development.

If you want to experiment with the code or propose improvements:

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature/my-feature
```

3. Commit your changes

```bash
git commit -m "Add my feature"
```

4. Push the branch

```bash
git push origin feature/my-feature
```

5. Open a Pull Request

Please note the project's license before reusing the source code.

---

# 📄 License

Copyright © 2026 **Willian Santos**.

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0)**.

You are free to:

- **Share** — copy and redistribute the material in any medium or format
- **Adapt** — remix, transform, and build upon the material

Under the following terms:

- **Attribution** — appropriate credit must be given
- **NonCommercial** — the material may not be used for commercial purposes without authorization

The copyright holder retains all commercial rights and may use, sell, sublicense, or relicense the project under different terms.

See the `LICENSE` file for the complete licensing terms.

---

<p align="center">
  Built with 💙, Kotlin and Compose Multiplatform.
</p>

<p align="center">
  Developed by <strong>Willian Santos</strong>.
</p>

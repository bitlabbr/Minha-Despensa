# MinhaDespensa 🛒

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

Shopping lists are modeled as persistent entities and can contain multiple shopping items.

Current data/domain capabilities include:

- Multiple shopping lists
- Custom list names
- Optional budget per list
- Product quantity
- Price recorded during shopping
- Checked / unchecked state
- Soft deletion
- Timestamp-based updates
- Retrieval of lists together with their items

Each shopping item references a product from the catalog instead of duplicating product data.

---

## 💳 Purchase Finalization

MinhaDespensa already implements a purchase finalization flow.

When a shopping list is finalized, checked items can be:

1. Added to the pantry
2. Recorded in the product price history
3. Reset in the shopping list for future reuse

This creates a direct relationship between shopping, inventory, and historical prices.

```text
Shopping Item
     │
     │ finalizePurchase()
     ▼
 ┌───────────────┐
 │               │
 ▼               ▼
Pantry       Price Entry
```

---

## 🏠 Pantry Management

The pantry module already contains domain, repository, DAO, ViewModel, and UI-state implementations.

Current capabilities include:

- Persistent pantry items
- Quantity tracking
- Expiration date
- Batch number
- Product association
- Category-aware pantry queries
- Active pantry item queries
- Pantry item detail retrieval
- Soft deletion
- Timestamp-aware updates
- Reactive UI state with Kotlin Flow / StateFlow

The pantry UI is currently being expanded and integrated with the rest of the application.

---

## ⏳ Expiration Tracking

The project already models expiration dates for pantry items and contains repository/UI support for retrieving products close to expiration.

The Pantry ViewModel maintains separate state for:

- all active pantry items;
- products close to expiration;
- the currently selected pantry item.

The complete expiration experience — including alerts and final user-facing behavior — is still under development.

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

When a purchase is finalized, the price recorded in the shopping list can automatically become a new historical price entry.

---

## ➕ Product Registration UI

A Compose Multiplatform product registration flow is currently under development.

The current UI already includes:

- Product photo area
- Product name
- Category
- Measurement unit
- Barcode field
- Barcode scanner action
- Average price
- Average shelf life
- Notes
- Expiration reminder configuration
- Configurable reminder interval
- Loading state during save
- Expandable advanced information section

Camera/gallery integration, barcode scanning, reminders, and final persistence wiring are still being completed.

---

# 🚧 Development Status

| Feature | Status |
|---|---|
| Kotlin Multiplatform Android/iOS foundation | ✅ Implemented |
| Compose Multiplatform UI | ✅ Implemented |
| Clean Architecture + MVVM | ✅ Implemented |
| Koin dependency injection | ✅ Implemented |
| Room KMP persistence | ✅ Implemented |
| Local product catalog | ✅ Implemented |
| Search by EAN | ✅ Data layer |
| Search by name / brand | ✅ Data layer |
| Shopping list persistence | ✅ Implemented |
| Shopping list budget | ✅ Domain/Data |
| Shopping checklist state | ✅ Domain/Data |
| Purchase finalization | ✅ Implemented |
| Automatic pantry entry after purchase | ✅ Implemented |
| Price history | ✅ Domain/Data |
| Pantry domain and persistence | ✅ Implemented |
| Pantry reactive ViewModel | ✅ Implemented |
| Pantry full UI experience | 🚧 In development |
| Expiration tracking | 🚧 In development |
| Product registration form | 🚧 In development |
| Product image capture/gallery | 🚧 In development |
| Barcode scanner integration | 🚧 In development |
| Expiration notifications | 🚧 In development |
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
| `:core` | Domain models, repository interfaces, shared contracts, and business abstractions | Domain |
| `:data` | Room database, DAOs, entities, repository implementations, and platform-specific data utilities | Data / Infrastructure |

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

MinhaDespensa uses **Room KMP** as its local persistence layer.

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
│       │           │   └── dto/
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
git clone https://github.com/YOUR-USERNAME/MinhaDespensa.git
cd MinhaDespensa
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

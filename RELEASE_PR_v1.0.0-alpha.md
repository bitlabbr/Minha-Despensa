# PR: Release v1.0.0-alpha — Core Architecture, Room v4, Shopping Assistant & Product Catalog

## 📌 PR Link
[Open Pull Request: `dev` → `main`](https://github.com/bitlabbr/Minha-Dispensa/compare/main...dev?expand=1)

---

## 🎯 Overview & Context

This Pull Request marks the **first alpha release (`v1.0.0-alpha`)** of **Minha Despensa**, consolidating the migration from an initial shopping calculator prototype into a complete, modular, offline-first **Kotlin Multiplatform (Android & iOS)** household inventory and grocery management application.

It integrates the complete shopping lifecycle:
```text
Catalog ──► Shopping Lists ──► Finalize Purchase (Atomic Checkout)
                                    │
                  ┌─────────────────┴─────────────────┐
                  ▼                                   ▼
           Pantry Inventory                     Price History
                  │
                  ▼
             Consumption
```

---

## 🚀 Key Deliverables by Module

### 1. `:core` (Domain Layer)
*   **Clean Architecture**: Zero dependencies on outer layers (UI, database frameworks, or Android/iOS SDKs).
*   **11 Domain Use Cases**:
    *   `SaveCatalogProductUseCase`: Catalog product persistence and validation.
    *   `CheckEanStatusUseCase`: EAN barcode numeric format & existence checks.
    *   `AddPantryItemUseCase`: Inventory stock validation and insertion.
    *   `CreatePlannedShoppingListUseCase`: Structured list creation with budget constraints.
    *   `CreateQuickShoppingListUseCase`: Scratchpad multiline text parsing into drafts.
    *   `AddCatalogItemToShoppingListUseCase`: Catalog product attachment with unit defaults.
    *   `AddOrUpdateCartItemUseCase`: In-cart item adjustments (quantity, price, checked state).
    *   `ReplaceCartItemUseCase`: Swapping cart items with alternative products during active shopping.
    *   `RemoveCartItemUseCase`: Soft-deleting shopping items from active lists.
    *   `StartShoppingSessionUseCase`: Transitioning lists to active `SHOPPING` state.
    *   `FinalizeShoppingSessionUseCase`: Atomic orchestration across pantry stock, price history, and list states.
*   **Domain Models & Computed Properties**:
    *   `ShoppingList` (`SCRATCHPAD`, `PLANNED`, `ASSISTANT` with `DRAFT`, `SHOPPING`, `COMPLETED`, `CANCELLED`).
    *   Computed properties: `totalCheckedItems`, `totalActiveItems`, `totalCartInCents`, `isOverBudget`.
    *   `ShoppingItem.subtotalInCents`.
*   **Domain Utilities**: Centralized `CoreConstants`, timestamp boundary validation (`isValidTimestamp`), and `AppLogger` contract.

### 2. `:data` (Persistence Layer)
*   **Room Multiplatform (Schema Version 4)**: Backed by SQLite via `BundledSQLiteDriver`.
*   **Defensive Migration Pipeline (`AppDatabaseMigrations.kt`)**:
    *   `MIGRATION_1_2`: Adds `product_media` entity with cascade deletes.
    *   `MIGRATION_2_3`: Adds `category` and `notes` to `catalog_products`, creates `shopping_lists` table.
    *   `MIGRATION_3_4`: Standardizes snake_case columns (`budget_in_cents`, `list_id`, `product_id`), configures foreign key `ON DELETE SET NULL` on product ID to preserve shopping items when products are removed, and creates unique EAN indices.
    *   Direct upgrade paths: `MIGRATION_1_4` and `MIGRATION_2_4`.
*   **Last-Write-Wins (LWW)**: Timestamp conflict resolution (`updated_at < :updatedAt`) preventing stale data overwrites.
*   **Atomic Multi-Table Transactions**: Uses `useWriterConnection { conn -> conn.withTransaction(IMMEDIATE) { ... } }` for checkout (`finalizePurchase`), bulk pantry consumption (`consumeBatch`), and catalog product + media creation.

### 3. `:uisystem` (Compose Multiplatform & Presentation Layer)
*   **Agnostic Design System**: Glassmorphism token hierarchy (`PrimaryContainerGlassCard`, `SecondaryContainerGlassCard`), custom typography tokens (`AppTypography`), and `UiText` string resolution.
*   **Product Details Feature (`ProductDetailsScreen`)**:
    *   Inspection of catalog products from pantry and catalog contexts.
    *   Price evolution chart visualizer.
    *   Active pantry batch breakdown with expiration dates.
    *   Quick actions (add to cart, increment pantry, edit product).
*   **Unified Product Registration (`RegisterProductBottomSheet`)**:
    *   Inline validation (`ProductFormValidator`), debounced EAN verification, and photo preview.
*   **Pantry Inventory (`PantryScreen`)**:
    *   Categorized stock visualization, threshold-based expiring items filter, and single/batch consumption.
*   **Shopping Suite**:
    *   `QuickListScreen`: Fast multiline text parsing into draft shopping lists.
    *   `CreatePlannedListScreen`: Catalog-backed planned list builder with budget limits.
    *   `ShoppingAssistantScreen`: In-store live companion with CameraX + Google ML Kit barcode scanning, real-time cart total calculations, and item replacement sheets.
*   **State Machine Subflows**: Sealed class state machines (`PantrySubFlow`, `ProductDetailsSubFlow`) eliminating modal race conditions.

### 4. `:composeApp` (Application Assembly & Packaging)
*   Root `NavHost` integration with bottom navigation and animated transitions.
*   Dependency injection bootstrap via Koin (`appModule`, `uiModule`, `dataModule`).
*   Version bump to **`1.0.0-alpha`** with signed release APK build configuration.

---

## 🧪 Testing & Verification

Comprehensive automated test suite with **279 unit tests (100% passing)**:

| Module | Test Suite Count | Focus Areas |
|---|---|---|
| **`:core`** | **52 tests** | Barcode validation, 11 use case workflows, timestamp tolerance, model serialization |
| **`:data`** | **132 tests** | Room v1-v4 schema migrations, in-memory DAOs, LWW conflict resolution, atomic transactions |
| **`:uisystem`** | **94 tests** | ViewModels, UI state machines, currency formatters (`R$ 1.234,56`), form validators |
| **`:composeApp`** | **1 test** | Integration smoke test |
| **Total** | **279 tests** | **0 failures** |

### Local Verification Command
```bash
./gradlew testDebugUnitTest
./gradlew :composeApp:assembleRelease
```

---

## 📦 Release Artifact
*   **Android Release APK**: `composeApp/build/outputs/apk/release/composeApp-release.apk`
*   **Tag to be applied on `main`**: `v1.0.0-alpha`

# `:uisystem` Module Architecture & Guide

## 1. Module Purpose & Scope

The `:uisystem` module is a Compose Multiplatform (Android & iOS) design system and presentation layer for the **Minha Despensa** application. It implements a strict **Bottom-Up Architecture** and MVVM pattern, delivering:

1. **Agnostic Design System Tokens & Atomics:** Centralized palette, typography tokens, glassmorphism containers, buttons, text fields, badges, and dialogs.
2. **Domain-Specific Molecules & Sheets:** Reusable cards, filter bars, camera-driven barcode scanners, and a unified product registration sheet.
3. **Feature Screens & State Machines:** Reactive screens managing pantry inventory, product catalog, quick & planned shopping lists, and a live in-store shopping assistant.
4. **App Notification Manager:** Decoupled transient notifications via `AppNotificationManager` and `MinhaDespensaSnackbar`.
5. **Multiplatform Test Suite:** 100% Kotlin Multiplatform unit tests for ViewModels, state machines, and utility formatters.

---

## 2. Dependencies & Boundary Contracts

- **`:core` Module:** Exposes domain models (`CatalogProduct`, `PantryItem`, `ShoppingList`, `ShoppingItem`, `MeasureUnit`), use cases (`SaveCatalogProductUseCase`, `AddPantryItemUseCase`, `CheckEanStatusUseCase`, `CreateQuickShoppingListUseCase`, etc.), and utilities (`AppLogger`, `DiQualifiers`).
- **Compose Multiplatform:** Jetpack Compose Multiplatform UI engine, runtime, and resources.
- **Koin Multiplatform:** ViewModel injection (`viewModel { ... }`) and service resolution.
- **AndroidX Lifecycle & Navigation:** `lifecycle-viewmodel-compose`, Kotlinx Serialization for type-safe navigation.
- **Hardware Integration (Android):** CameraX (`androidx.camera.*`) and Google ML Kit Barcode Scanning for camera EAN recognition.
- **Testing:** `kotlin.test`, `kotlinx-coroutines-test`, and `app.cash.turbine:turbine` for reactive Flow assertions in `commonTest`.

---

## 3. Package & Architectural Structure

The module follows a layered structure:

```
uisystem/src/
├── commonMain/kotlin/com/bitlabbr/minhadespensa/uisystem/
│   ├── components/
│   │   ├── core/                  # Level 1: Agnostic Atomics & Design System
│   │   │   ├── badge/             # Status and info badges
│   │   │   ├── buttons/           # Primary, Secondary, Circular, and Danger buttons
│   │   │   ├── cards/             # Primary, Secondary, and Item GlassCards
│   │   │   ├── dialogs/           # MinhaDespensaDialog & Confirmation Dialogs
│   │   │   ├── inputs/            # MinhaDespensaTextField, SearchTextField, AmountField
│   │   │   ├── menus/             # Context menus and popup options
│   │   │   ├── snackbar/          # MinhaDespensaSnackbar with Success, Warning, Error
│   │   │   ├── text/              # MinhaDespensaText with design tokens
│   │   │   └── topbar/            # MinhaDespensaTopBar with back navigation & actions
│   │   └── domain/                # Level 2: Domain Molecules & Composite Cards
│   │       ├── catalog/           # CatalogProductCard, CategoryChip
│   │       ├── pantry/            # PantryItemCard, ExpirationTag
│   │       └── shopping/          # ShoppingItemRow, CartTotalSummary
│   ├── features/                  # Level 3 & 4: Feature Screens & ViewModels
│   │   ├── home/                  # HomeScreen, widgets (Financial, Expiring, Trend)
│   │   │   ├── model/             # ConsumptionTrendItemUiModel
│   │   │   └── widgets/           # HomeWidget composables and HomeMockData
│   │   ├── catalog/               # CatalogScreen, CatalogViewModel
│   │   │   ├── model/             # CatalogProductUiModel, CatalogUiState
│   │   │   └── widgets/register/  # RegisterProductBottomSheet & RegisterProductFormContent
│   │   ├── pantry/                # PantryScreen, PantryViewModel
│   │   │   ├── model/             # PantryUiState, PantrySubFlow state machine
│   │   │   └── widgets/           # Subflow sheets (AddItemDetails, BarcodeScanner)
│   │   ├── shopping/              # Shopping suite
│   │   │   ├── overview/          # ShoppingListsScreen, ShoppingListsViewModel
│   │   │   ├── assistant/         # ShoppingAssistantScreen, ShoppingAssistantViewModel
│   │   │   ├── planned/           # CreatePlannedListScreen, PlannedListViewModel
│   │   │   └── quicklist/         # QuickListScreen, QuickListViewModel
│   │   └── settings/              # SettingsScreen
│   ├── manager/                   # AppNotificationManager (SharedFlow event bus)
│   ├── mapper/                    # MeasureUnitMapper (Domain enum <-> UI Resource)
│   ├── model/                     # UiText, Deprecated aliases for backwards compat
│   ├── theme/                     # MinhaDespensaTheme, Colors, Typography, Dimens
│   ├── util/                      # CurrencyFormatter, UI helpers
│   └── di/                        # UiModule (Koin definitions)
└── commonTest/kotlin/com/bitlabbr/minhadespensa/uisystem/
    ├── fakes/                     # FakeCatalogRepository, FakePantryRepository, etc.
    ├── features/                  # ViewModel unit tests (Catalog, Pantry, Shopping)
    ├── manager/                   # AppNotificationManagerTest
    ├── mapper/                    # MeasureUnitMapperTest
    ├── model/                     # UiTextTest, CatalogProductUiModelTest
    └── util/                      # CurrencyFormatterTest
```

---

## 4. Key Components & Features

### 4.1. Design Tokens & Typography
- **Colors:** Defined in `theme/Color.kt` using glassmorphism tones (`ContainerGradientStart`, `ContainerGradientEnd`, `CardBackground`, `GlassSurface`).
- **Typography:** Strictly constrained to `AppTypography` design tokens:
  - `displayLarge`: High-impact totals and balance figures.
  - `displayMedium`: Section and screen headers.
  - `bodyLarge`: Primary list item titles and form labels.
  - `bodySmall`: Subtitles, metadata, timestamps, and secondary labels.
  - `button`: Button action labels.
  - `priceLabel`: Monetary values and currency display.
- **UiText Pattern:** Eliminates raw hardcoded strings in ViewModels. Uses `UiText.DynamicString` for runtime messages and `UiText.Resource` for localized Compose Multiplatform strings.

### 4.2. Unified Product Registration (`RegisterProductBottomSheet`)
Product registration was consolidated to eliminate duplicate code across screens:
- **`RegisterProductFormContent`:** Encapsulates the form fields (Name, Brand, Category, Net Weight, Measure Unit, Barcode, Photo capture) with inline validation and debounced EAN verification.
- **`RegisterProductBottomSheet`:**
  - *Controlled Overload:* Accepts `formState: ProductFormState`, `onFormChange`, `onSave`, and `onDismiss`. Ideal for parent ViewModels (like `CatalogViewModel` or `PantryViewModel`).
  - *Self-Contained Overload:* Instantiates and binds directly to `CatalogViewModel`, simplifying one-off registration triggers.

### 4.3. Shopping Suite & Assistant
- **Quick Lists (`QuickListScreen`):** Fast text parsing of unformatted item lines into a draft shopping list.
- **Planned Lists (`CreatePlannedListScreen`):** Catalog-backed item selection with interactive quantity modifiers and budget validation.
- **Shopping Assistant (`ShoppingAssistantScreen`):** In-store companion with:
  - Dynamic cart total calculations (cents-accurate).
  - One-tap check/uncheck toggling.
  - Barcode scanning integration (camera ML Kit on Android) that matches scanned items against the local catalog and prompts for price/quantity confirmation.

### 4.4. State Machine Subflows
In `PantryViewModel` and `ShoppingAssistantViewModel`, modal subflows are managed via sealed class state machines:
```kotlin
sealed class PantrySubFlow {
    data object BarcodeScanner : PantrySubFlow()
    data class CreateCatalogProduct(val initialEan: String?) : PantrySubFlow()
    data class AddItemDetails(val product: CatalogProductUiModel) : PantrySubFlow()
    data object QuickList : PantrySubFlow()
}
```
This guarantees only one sub-flow modal (camera scanner, registration form, item quantity picker) is active at any time, eliminating race conditions.

---

## 5. Dependency Injection (`UiModule`)

Registered in `com.bitlabbr.minhadespensa.uisystem.di.UiModule`:
- **Logger:** `AppLogger` qualifier `DiQualifiers.UI_LOGGER` bound to `ConsoleLogger("UISystem")`.
- **Notification:** `AppNotificationManager` registered as a singleton.
- **ViewModels:**
  - `PantryViewModel`: Injects pantry & catalog repositories, use cases, logger, notification manager.
  - `CatalogViewModel`: Injects catalog repository, `SaveCatalogProductUseCase`, logger, notification manager.
  - `QuickListViewModel`: Injects `CreateQuickShoppingListUseCase`, logger.
  - `PlannedListViewModel`: Injects `CreatePlannedShoppingListUseCase`, catalog repository, logger.
  - `ShoppingAssistantViewModel`: Injects session use cases, catalog repository, logger.
  - `ShoppingListsViewModel`: Injects shopping list repository, logger.

---

## 6. Automated Unit Testing Suite

All unit tests reside in `uisystem/src/commonTest` and execute across multiplatform targets without Android device or emulator dependencies:

| Test Class | Focus & Covered Behaviors |
| :--- | :--- |
| `CurrencyFormatterTest` | Formats integer cents to Brazilian Real (`R$ 1.234,56`), handles zero, negatives, and large numbers. |
| `MeasureUnitMapperTest` | Maps all domain `MeasureUnit` enums to localized `StringResource` tokens. |
| `UiTextTest` | Resolves `DynamicString` and `Resource` with string format arguments. |
| `CatalogProductUiModelTest` | Formats unit weights, packaging info, and handles missing brands. |
| `ProductFormStateTest` | Form validation: required name, length restrictions, numeric weight parsing. |
| `AppNotificationManagerTest` | Emits `SUCCESS`, `WARNING`, and `ERROR` snackbar notifications through `SharedFlow`. |
| `QuickListViewModelTest` | Title and raw content updates, save delegation, item generation. |
| `ShoppingListsViewModelTest` | Active vs. completed list segregation, list deletion reactivity. |
| `PlannedListViewModelTest` | Budget input sanitization, product quantity increments/decrements, catalog filtering, save delegation. |
| `ShoppingAssistantViewModelTest` | Live cart session loading, item checking/unchecking, subtotal calculation, barcode scanner subflows, purchase finalization. |
| `CatalogViewModelTest` | Debounced EAN uniqueness validation, category filtering, search suggestions, form saving with use case. |
| `PantryViewModelTest` | Category filtering, search query filtering, barcode scanned transitions, pantry item persistence, notification dispatch. |

### Running the Tests
```bash
# Run all unit tests in the uisystem module
./gradlew :uisystem:testDebugUnitTest

# Run full project test suite
./gradlew testDebugUnitTest
```

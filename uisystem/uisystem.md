# `:uisystem` Module Architecture & Guide

## 1. Module Purpose & Scope

The `:uisystem` module is a Compose Multiplatform (Android & iOS) design system and presentation layer for the **Minha Despensa** application. It implements a strict **Bottom-Up Architecture** and MVVM pattern, delivering:

1. **Agnostic Design System Tokens & Atomics:** Centralized palette, typography tokens, glassmorphism containers, buttons, text fields, badges, and dialogs.
2. **Domain-Specific Molecules & Sheets:** Reusable cards, filter bars, camera-driven barcode scanners, and a unified product registration sheet.
3. **Feature Screens & State Machines:** Reactive screens managing pantry inventory, product catalog, product details & price history, quick & planned shopping lists, and a live in-store shopping assistant.
4. **App Notification Manager:** Decoupled transient notifications via `AppNotificationManager` and `MinhaDespensaSnackbar`.
5. **Multiplatform Test Suite:** 100% Kotlin Multiplatform unit tests for ViewModels, state machines, and utility formatters (141 tests).

---

## 2. Dependencies & Boundary Contracts

- **`:core` Module:** Exposes domain models (`CatalogProduct`, `PantryItem`, `ShoppingList`, `ShoppingItem`, `MeasureUnit`), use cases (`SaveCatalogProductUseCase`, `AddPantryItemUseCase`, `CheckEanStatusUseCase`, `CreateQuickShoppingListUseCase`, `ReplaceCartItemUseCase`, `RemoveCartItemUseCase`, etc.), and utilities (`AppLogger`, `DiQualifiers`).
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
│   │   │   ├── button/            # MinhaDespensaButton, circular, primary, secondary
│   │   │   ├── card/              # PrimaryContainerGlassCard, SecondaryContainerGlassCard
│   │   │   ├── dialog/            # MinhaDespensaDialog & Confirmation Dialogs
│   │   │   ├── input/             # MinhaDespensaTextField, AmountField, DropdownField
│   │   │   ├── search/            # MinhaDespensaSearchBar
│   │   │   ├── snackbar/          # MinhaDespensaSnackbar with Success, Warning, Error
│   │   │   ├── text/              # MinhaDespensaText with design tokens
│   │   │   └── topbar/            # MinhaDespensaTopBar with back navigation & actions
│   │   └── domain/                # Level 2: Domain Molecules & Composite Cards
│   │       ├── catalog/           # CatalogProductCard, CategoryChip, CatalogProductPickerSheet
│   │       ├── pantry/            # PantryItemCard, ExpiringItemTile, ConsumptionTrendTile
│   │       └── shopping/          # ShoppingItemRow, CartTotalSummary
│   ├── features/                  # Level 3 & 4: Feature Screens & ViewModels
│   │   ├── home/                  # HomeScreen, HomeViewModel
│   │   │   ├── model/             # HomeUiState, HomeFinancialUiModel, ConsumptionTrendItemUiModel
│   │   │   └── widgets/           # FinancialWidget, ExpiringSoonWidget, ConsumptionTrendWidget, SetMonthlyBudgetDialog
│   │   ├── catalog/               # CatalogScreen, CatalogViewModel
│   │   │   ├── model/             # CatalogProductUiModel, CatalogUiState
│   │   │   └── widgets/register/  # RegisterProductBottomSheet & RegisterProductFormContent
│   │   ├── details/               # ProductDetailsScreen, ProductDetailsViewModel
│   │   │   ├── model/             # ProductDetailsUiState, ProductDetailsSubFlow
│   │   │   └── widgets/           # Price history charts, batch listings, edit sheets
│   │   ├── pantry/                # PantryScreen, PantryViewModel
│   │   │   ├── model/             # PantryUiState, PantrySubFlow state machine
│   │   │   └── widgets/           # Subflow sheets (AddPantryItemOptionsSheet, AddPantryItemDetailsSheet, BarcodeScanner)
│   │   ├── shopping/              # Shopping suite
│   │   │   ├── overview/          # ShoppingListsScreen, ShoppingListsViewModel
│   │   │   ├── assistant/         # ShoppingAssistantScreen, ShoppingAssistantViewModel
│   │   │   │   └── widgets/       # ReplaceItemOptionsSheet, AddCartItemDetailsSheet, etc.
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
    ├── features/                  # ViewModel unit tests (Catalog, Pantry, Shopping, Details)
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
- **`RegisterProductFormContent`:** Encapsulates the form fields (Name, Brand, Category, Net Weight, Measure Unit, Barcode, Photo capture) with inline validation via `ProductFormValidator` and debounced EAN verification.
- **`RegisterProductBottomSheet`:**
  - *Controlled Overload:* Accepts `formState: ProductFormState`, `onFormChange`, `onSave`, and `onDismiss`. Ideal for parent ViewModels (like `CatalogViewModel`, `PantryViewModel`, or `ProductDetailsViewModel`).
  - *Self-Contained Overload:* Instantiates and binds directly to `CatalogViewModel`, simplifying one-off registration triggers.

### 4.3. Product Details & Price History (`ProductDetailsScreen`)
Accessible from both the Catalog and Pantry flows:
- **Inspection:** Displays brand, category, barcode, net weight, measurement unit, and notes.
- **Price History:** Dynamic chart visualizing price evolution over time and latest recorded prices.
- **Pantry Batches & Prices:** Lists all active batches in the pantry with quantity, expiration dates, unit price (`unitPriceInCents`), and computed batch total (`totalPriceInCents`). Lotes sem valor informado exibem o indicativo amigável `Sem valor informado • Toque p/ definir`.
- **Batch Editing (`EditPantryBatchSheet`):** Permite editar quantidade, data de validade com chips de atalho (`+7d`, `+15d`, etc.), número de lote e preço unitário com validação e persistência reativa.
- **Quick Actions:** Direct shortcuts to add to an active shopping list, increment pantry stock, or edit product catalog metadata.
- **Catalog Product Editing:** Supports full inline editing of catalog metadata and photo. Tracks `hasImageRemoved` in `ProductFormState` to explicitly delete media blobs from the database when the user clears an existing photo.

### 4.4. Shopping Suite & Assistant
- **Quick Lists (`QuickListScreen`):** Fast text parsing of unformatted item lines into a draft scratchpad shopping list.
- **Planned Lists (`CreatePlannedListScreen`):** Catalog-backed item selection with interactive quantity modifiers, quick search clear button, category/brand/name filtering, and budget validation.
- **Shopping Assistant (`ShoppingAssistantScreen`):** In-store companion with:
  - Real-time expandable search bar (`MinhaDespensaSearchBar`) filtering cart items by name, category, and brand without altering financial totals (`totalCartValueInCents`, `checkedCount`, `totalCount`).
  - Dynamic cart total calculations (cents-accurate).
  - One-tap check/uncheck toggling.
  - Barcode scanning integration (camera ML Kit on Android) that matches scanned items against the local catalog.
  - Duplicate item detection and warning banner in `AddCartItemDetailsSheet` showing current quantity in cart and consolidating quantities automatically on confirmation.
  - Dynamic item replacement (`ReplaceItemOptionsSheet`), manual item addition (`AddCartItemDetailsSheet`), catalog search picker (`CatalogProductPickerSheet`), and budget gauge (`ShoppingFinancialDashboard`).

### 4.5. State Machine Subflows
In `PantryViewModel`, `ShoppingAssistantViewModel`, and `ProductDetailsViewModel`, modal subflows are managed via sealed class state machines (e.g., `PantrySubFlow`, `ProductDetailsSubFlow`).
This guarantees only one sub-flow modal (camera scanner, registration form, item quantity picker) is active at any time, eliminating UI race conditions.

### 4.6. Accent-Insensitive Search & Normalization
To provide a smooth search experience for Brazilian Portuguese diacritics and special characters (`pao` -> `pão`, `maca` -> `maçã`, `acucar` -> `açúcar`), all reactive list filters in `:uisystem` utilize `TextNormalizer.matchesSearchQuery(query)`:
- **`CatalogViewModel`:** Filters catalog items reactive stream matching against product name and brand.
- **`PantryViewModel`:** Filters pantry inventory items reactive stream matching against item name, brand, or combined name and brand tokens.
- **`PlannedListViewModel`:** Filters available catalog products matching against name, brand, and category.
- **`ShoppingAssistantViewModel`:** Filters active cart items reactive stream matching against product name, category, and brand.
- **`CatalogProductPickerSheet`:** Filters catalog products matching against name and brand within the bottom sheet picker.

### 4.7. Home Overview & Real-Time Aggregation (`HomeScreen` & `HomeViewModel`)
The home screen connects the user's live inventory, movement ledger, and monthly financial budget:
- **`HomeViewModel`:** Reactively combines `GetMonthlyFinancialSummaryUseCase`, `PantryRepository.getExpiringPantryItems`, `GetMostConsumedProductsUseCase`, `CatalogRepository.getAllActiveProducts`, `GetPantryTotalValueUseCase`, `GetYearlyFinancialSummaryUseCase`, and `PantryMovementRepository.getMovementsForPeriod` into `HomeUiState`.
- **Financial Summary (`FinancialWidget`):** Displays real-time spending for the current month (`totalSpentInCents`), defined budget, and animated gauge (`InverseAnchoredGauge`). If no budget is set, renders an inviting empty state with a call-to-action button. Tapping the card triggers budget modification, while dedicated actions trigger the financial analytics dialog.
- **Pantry Stock Valuation (`HomePantryStockUiModel`):** Aggregates the total monetary value of all active pantry items (`sum(quantity * unitPriceInCents)`), formatted currency strings, item count with price, and average unit price.
- **Yearly Analytics (`HomeYearlyAnalyticsUiModel`):** Prepares 12 monthly bars comparing actual spend vs. budget ceiling, total annual spending, average monthly spend, and weighted average item/purchase price.
- **Set Monthly Budget Dialog (`SetMonthlyBudgetDialog`):** Interactive glassmorphism dialog enabling immediate setting or updating of the current month's budget via `SetMonthlyBudgetUseCase`.
- **Expiring Soon (`ExpiringSoonWidget`):** Monitors pantry stock with dynamic labels ("Vence hoje", "Vence amanhã", "Vence em X dias", "Vencido há X dias") with gauge urgency and an elegant empty state ("Tudo em dia na sua despensa!").
- **Consumption Trends (`ConsumptionTrendWidget`):** Highlights top consumed products during the current month with unit resolution and empty state.

---

## 5. Dependency Injection (`UiModule`)

Registered in `com.bitlabbr.minhadespensa.uisystem.di.UiModule`:
- **Logger:** `AppLogger` qualifier `DiQualifiers.UI_LOGGER` bound to `ConsoleLogger("UISystem")`.
- **Notification:** `AppNotificationManager` registered as a singleton.
- **ViewModels:**
  - `HomeViewModel`: Injects `GetMonthlyFinancialSummaryUseCase`, `SetMonthlyBudgetUseCase`, `GetMostConsumedProductsUseCase`, `PantryRepository`, `CatalogRepository`, logger, notification manager.
  - `PantryViewModel`: Injects pantry & catalog repositories, use cases, logger, notification manager.
  - `CatalogViewModel`: Injects catalog repository, `SaveCatalogProductUseCase`, logger, notification manager, `CheckEanStatusUseCase`.
  - `QuickListViewModel`: Injects `CreateQuickShoppingListUseCase`, logger.
  - `PlannedListViewModel`: Injects `CreatePlannedShoppingListUseCase`, catalog repository, logger.
  - `ShoppingAssistantViewModel`: Injects session use cases (`StartShoppingSessionUseCase`, `AddOrUpdateCartItemUseCase`, `FinalizeShoppingSessionUseCase`, `CheckEanStatusUseCase`, `ReplaceCartItemUseCase`, `RemoveCartItemUseCase`), repositories, logger.
  - `ShoppingListsViewModel`: Injects shopping list repository, logger.
  - `ProductDetailsViewModel`: Injects catalog, pantry, price, and shopping repositories, `AddCatalogItemToShoppingListUseCase`, `AddPantryItemUseCase`, `UpdatePantryItemUseCase`, `SaveCatalogProductUseCase`, `CheckEanStatusUseCase`, logger, notification manager.

---

## 6. Automated Unit Testing Suite

All 141 unit tests reside in `uisystem/src/commonTest` and execute across multiplatform targets without Android device or emulator dependencies:

| Test Class | Focus & Covered Behaviors |
| :--- | :--- |
| `CurrencyFormatterTest` | Formats integer cents to Brazilian Real (`R$ 1.234,56`), handles zero, negatives, and large numbers. |
| `MeasureUnitMapperTest` | Maps all domain `MeasureUnit` enums to localized `StringResource` tokens. |
| `UiTextTest` | Resolves `DynamicString` and `Resource` with string format arguments. |
| `CatalogProductUiModelTest` | Formats unit weights, packaging info, and handles missing brands. |
| `ProductFormStateTest` | Form validation: required name, length restrictions, numeric weight parsing. |
| `ProductFormValidatorTest` | Tests standalone form validation logic across name, brand, weight, category, and notes. |
| `BarcodeRoiValidatorTest` | Validates barcode bounding box containment within scanner viewports. |
| `BarcodeScanStabilizerTest` | Enforces consecutive matching barcode reads before triggering scan detection. |
| `AppNotificationManagerTest` | Emits `SUCCESS`, `WARNING`, and `ERROR` snackbar notifications through `SharedFlow`. |
| `HomeViewModelTest` | Reactive state emissions, financial summary with and without budget, gauge overbudget progress, pantry stock value calculation (with priced/unpriced/deleted items), yearly analytics aggregations (12 monthly bars, elapsed month averages, purchase/stock unit costs), dynamic expiring labels (today, tomorrow, future, overdue), consumption trend product resolution, SetMonthlyBudgetDialog & FinancialAnalyticsDialog lifecycles and validation. |
| `QuickListViewModelTest` | Title and raw content updates, save delegation, item generation. |
| `ShoppingListsViewModelTest` | Active vs. completed list segregation, list deletion reactivity. |
| `PlannedListViewModelTest` | Budget input sanitization, product quantity increments/decrements, catalog filtering by name/brand/category (with accent insensitivity), save delegation. |
| `ShoppingAssistantViewModelTest` | Live cart session loading, item checking/unchecking, subtotal calculation, accent-insensitive search by name/category/brand, duplicate item detection and consolidation, barcode scanner subflows, item replacement, purchase finalization. |
| `CatalogViewModelTest` | Debounced EAN uniqueness validation, category filtering, accent-insensitive search suggestions, form saving with use case. |
| `PantryViewModelTest` | Category filtering, accent-insensitive search query filtering by name, brand, and compound name+brand, barcode scanned transitions, pantry item persistence, notification dispatch. |
| `ProductDetailsViewModelTest` | Product detail resolution, price history stream, pantry batches join, quantity increments, batch price mapping and calculation, in-place price recalculation (min/max/average), checkout stock price display, batch editing subflow and updates, add to shopping list. |

### Running the Tests
```bash
# Run all unit tests in the uisystem module
./gradlew :uisystem:testDebugUnitTest

# Run full project test suite
./gradlew testDebugUnitTest
```

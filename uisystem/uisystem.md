# `:uisystem` Module Architecture & Guide

## 1. Module Purpose & Scope

The `:uisystem` module is a Compose Multiplatform (Android & iOS) design system and presentation layer for the **Minha Despensa** application. It implements a strict **Bottom-Up Architecture** and MVVM pattern, delivering:

1. **Agnostic Design System Tokens & Atomics:** Centralized palette, typography tokens, glassmorphism containers, buttons, text fields, badges, and dialogs.
2. **Domain-Specific Molecules & Sheets:** Reusable cards, filter bars, camera-driven barcode scanners, and a unified product registration sheet.
3. **Feature Screens & State Machines:** Reactive screens managing pantry inventory, product catalog, product details & price history, quick & planned shopping lists, and a live in-store shopping assistant.
4. **App Notification Manager:** Decoupled transient notifications via `AppNotificationManager` and `MinhaDespensaSnackbar`.
5. **Multiplatform Test Suite:** 100% Kotlin Multiplatform unit tests for ViewModels, state machines, and utility formatters (169 tests).

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
│   │   └── shopping/              # Shopping suite
│   │       ├── overview/          # ShoppingListsScreen, ShoppingListsViewModel
│   │       ├── assistant/         # ShoppingAssistantScreen, ShoppingAssistantViewModel
│   │       │   └── widgets/       # ReplaceItemOptionsSheet, AddCartItemDetailsSheet, etc.
│   │       ├── planned/           # CreatePlannedListScreen, PlannedListViewModel
│   │       └── quicklist/         # QuickListScreen, QuickListViewModel
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

### 4.2. Unified Product Registration (`RegisterProductBottomSheet`) & Category Management
Product registration and category management are unified across catalog and pantry flows:
- **`RegisterProductFormContent`:** Encapsulates the form fields (Name, Brand, Category, Net Weight, Measure Unit, Barcode, Photo capture) with inline validation via `ProductFormValidator` and debounced EAN verification.
- **Inline Category Creation (`ProductDropdownField` + `QuickCreateCategoryDialog`):** Both `RegisterProductFormContent` and `ProductEditSection` (`ProductDetailsScreen`) display a highlighted `+ Nova categoria...` action in the category dropdown, opening `QuickCreateCategoryDialog` to create and immediately select a custom category.
- **Centralized Category Management (`ManageCategoriesDialog`):** Triggered via the `Categorias` action chip in `CategorizedContainerGlassCard` on `CatalogScreen`, allowing users to create new categories, rename existing categories (with automatic cascading updates to products), and delete categories (moving linked products to the protected `"Outros"` category).
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
- **Financial Summary (`FinancialWidget`):** Displays real-time spending for the current month (`totalSpentInCents`), defined budget, and animated gauge (`InverseAnchoredGauge`). If no budget is set, renders an inviting empty state with a call-to-action button. Tapping the card triggers budget modification, while dedicated header actions and the bottom inventory valuation banner trigger the financial analytics dialog.
- **Stock Value Card (`StockValueCard`):** Dedicated valuation widget displaying total active pantry inventory value in currency (`R$ X,XX`), number of evaluated items, and unit average cost.
- **Financial Analytics Dialog (`FinancialAnalyticsDialog`):** Interactive modal showcasing pantry inventory valuation (`StockValueCard`), annual indicators (total year spent, monthly average, weighted unit item price), and a 12-month evolution list with budget status badges ("Estourado", "Na meta", "Sem meta") and progress bars.
- **Pantry Stock Valuation (`HomePantryStockUiModel`):** Aggregates the total monetary value of all active pantry items (`sum(quantity * unitPriceInCents)`), formatted currency strings, item count with price, and average unit price.
- **Yearly Analytics (`HomeYearlyAnalyticsUiModel`):** Prepares 12 monthly bars comparing actual spend vs. budget ceiling, total annual spending, average monthly spend, and weighted average item/purchase price.
- **Set Monthly Budget Dialog (`SetMonthlyBudgetDialog`):** Interactive glassmorphism dialog enabling immediate setting or updating of the current month's budget via `SetMonthlyBudgetUseCase`. Supports live real-time currency formatting mask with thousand separators and currency symbol (`R$ 1.500,00`).
- **Expiring Soon (`ExpiringSoonWidget`):** Monitors pantry stock with dynamic labels ("Vence hoje", "Vence amanhã", "Vence em X dias", "Vencido há X dias") with gauge urgency and an elegant empty state ("Tudo em dia na sua despensa!").
### 4.8. NFC-e Invoice QR Code Scanning & Bulk Import
Enables adding items to the pantry in bulk by scanning state tax invoice QR Codes (SEFAZ-PE / NFC-e):
- **Entry Point (`AddPantryItemOptionsSheet`):** Adds a dedicated "Escanear Nota Fiscal (QR Code)" action option.
- **Scanner Modal (`PantrySubFlow.InvoiceScanner`):** Configures `BarcodeScannerModal` with a square reticle (`isSquareReticle = true`) and dedicated instruction text ("Aponte para o QR Code da Nota Fiscal").
- **Backend Resolution (`ParseInvoiceQrCodeUseCase`):** Consults the dedicated backend service to parse the HTML and extract items with name, EAN, quantity, unit price, and measure unit.
- **Selective Review Sheet (`InvoiceReviewBottomSheet` / `InvoiceItemRow`):** Compact header displaying store name, emission date, dynamic selected item counter and total price (cents-accurate), bulk select/deselect all toggle (`onToggleAllItems`), and full-height scrollable product list with dynamic measure unit price labels.
- **Atomic Bulk Import (`ImportInvoiceToPantryUseCase`):** Automatically registers missing catalog products, creates pantry stock batches, logs unit price history, and creates purchase movement ledger entries in a single database transaction.

### 4.9. Household Account, Multi-User Sharing & Cloud Sync (`HouseholdAccountScreen`)
Manages family household sharing, individual authentication (JWT login/register), and bidirectional cloud synchronization:
- **`HouseholdAccountViewModel`:** Injects auth, household, and sync repositories (`SyncRepository`), `SyncCoordinator`, session use cases (`LoginUserUseCase`, `RegisterUserUseCase`, `LogoutUserUseCase`), and household management use cases (`ManageHouseholdUseCase`, `JoinHouseholdByInviteCodeUseCase`, `SwitchActiveHouseholdUseCase`). Detects pre-existing unauthenticated data via `syncRepository.hasLocalData()` and manages the Merge Decision Dialog lifecycle (`isMergeDecisionDialogOpen`).
- **Unauthenticated Flow:** Displays tabbed Login / Register form (`AuthTab.LOGIN` / `AuthTab.REGISTER`) with inline field validation, password masking, and educational banner regarding cloud backup and family sharing benefits. Prompts user with Merge Decision Dialog (Opção A: Mesclar com nuvem / Opção B: Excluir locais e baixar da nuvem) whenever local offline data exists prior to authentication.
- **Authenticated Flow:**
  - **User Profile Banner:** Displays current user name, email, and logout action with confirmation dialog.
  - **Active Household GlassCard:** Highlights current active household name, member role badge (`Proprietário` vs `Membro`), and 8-character Crockford base32 invite code with one-tap copy triggering native haptic feedback (`rememberHapticFeedbackManager().performSuccessFeedback()`) and system clipboard.
  - **Sync State & Manual Trigger:** Live sync status indicator (`IDLE`, `SYNCING`, `SUCCESS`, `ERROR`, `OFFLINE`, `SESSION_EXPIRED`) with dedicated visual warning card banner for `SESSION_EXPIRED` prompting re-login, and immediate manual sync trigger button.
  - **Household Members List:** Lists all family members with name, email, joined date, and role badge (`HouseholdMemberCard`).
  - **Multi-Household Switching:** Displays other user households with one-tap "Ativar" switcher invoking `SwitchActiveHouseholdUseCase`.
- **Dialogs (`JoinHouseholdDialog`, `CreateHouseholdDialog`):** Modals for joining a family pantry via 8-char invite code or creating a new shared household (capped at 80 characters).
- **Home Top Bar Shortcut:** `HomeScreen` features real-time cloud sync status indicator and profile shortcut navigating to `HouseholdAccountRoute`.

---

## 5. Dependency Injection (`UiModule`)

Registered in `com.bitlabbr.minhadespensa.uisystem.di.UiModule`:
- **Logger:** `AppLogger` qualifier `DiQualifiers.UI_LOGGER` bound to `ConsoleLogger("UISystem")`.
- **Notification:** `AppNotificationManager` registered as a singleton.
- **ViewModels:**
  - `HomeViewModel`: Injects `GetMonthlyFinancialSummaryUseCase`, `SetMonthlyBudgetUseCase`, `GetMostConsumedProductsUseCase`, `GetPantryTotalValueUseCase`, `GetYearlyFinancialSummaryUseCase`, `PantryRepository`, `PantryMovementRepository`, `CatalogRepository`, `SyncCoordinator`, logger, notification manager.
  - `HouseholdAccountViewModel`: Injects `AuthRepository`, `HouseholdRepository`, `SyncCoordinator`, `SyncRepository`, `ManageHouseholdUseCase`, `JoinHouseholdByInviteCodeUseCase`, `SwitchActiveHouseholdUseCase`, `LoginUserUseCase`, `RegisterUserUseCase`, `LogoutUserUseCase`, `AppUpdateRepository?`, notification manager, logger.
  - `PantryViewModel`: Injects pantry & catalog repositories, use cases (`CheckEanStatusUseCase`, `AddPantryItemUseCase`, `ParseInvoiceQrCodeUseCase`, `ImportInvoiceToPantryUseCase`), logger, notification manager.
  - `CatalogViewModel`: Injects catalog repository, `SaveCatalogProductUseCase`, `SaveProductCategoryUseCase`, `DeleteProductCategoryUseCase`, logger, notification manager, `CheckEanStatusUseCase`.
  - `QuickListViewModel`: Injects `CreateQuickShoppingListUseCase`, logger.
  - `PlannedListViewModel`: Injects `CreatePlannedShoppingListUseCase`, catalog repository, logger.
  - `ShoppingAssistantViewModel`: Injects session use cases (`StartShoppingSessionUseCase`, `AddOrUpdateCartItemUseCase`, `FinalizeShoppingSessionUseCase`, `CheckEanStatusUseCase`, `ReplaceCartItemUseCase`, `RemoveCartItemUseCase`), repositories, logger.
  - `ShoppingListsViewModel`: Injects shopping list repository, logger.
  - `ProductDetailsViewModel`: Injects catalog, pantry, price, and shopping repositories, `AddCatalogItemToShoppingListUseCase`, `AddPantryItemUseCase`, `UpdatePantryItemUseCase`, `SaveCatalogProductUseCase`, `CheckEanStatusUseCase`, logger, notification manager.

---

## 6. Automated Unit Testing Suite

All unit tests reside in `uisystem/src/commonTest` and execute across multiplatform targets without Android device or emulator dependencies (183 tests):

| Test Class | Focus & Covered Behaviors |
| :--- | :--- |
| `CurrencyFormatterTest` | Formats integer cents to Brazilian Real (`R$ 1.234,56`), handles zero, negatives, and large numbers. |
| `MeasureUnitMapperTest` | Maps all domain `MeasureUnit` enums to localized `StringResource` tokens. |
| `MonthMapperTest` | Maps `Month` enum and integer numbers to localized `StringResource` tokens and short abbreviation strings with out-of-bounds fallback. |
| `UiTextTest` | Resolves `DynamicString` and `Resource` with string format arguments. |
| `CatalogProductUiModelTest` | Formats unit weights, packaging info, and handles missing brands. |
| `ProductFormStateTest` | Form validation: required name, length restrictions, numeric weight parsing. |
| `ProductFormValidatorTest` | Tests standalone form validation logic across name, brand, weight, category, and notes. |
| `BarcodeRoiValidatorTest` | Validates barcode bounding box containment within scanner viewports. |
| `BarcodeScanStabilizerTest` | Enforces consecutive matching barcode reads before triggering scan detection. |
| `AppNotificationManagerTest` | Emits `SUCCESS`, `WARNING`, and `ERROR` snackbar notifications through `SharedFlow`. |
| `HouseholdAccountViewModelTest` | Auth state transitions (login/register/logout), form validation, invite code normalization, household creation & switching, member list synchronization, manual sync triggering, in-app update checks and download/install triggers, dialog lifecycles. |
| `HomeViewModelTest` | Reactive state emissions, financial summary with and without budget, gauge overbudget progress, pantry stock value calculation (with priced/unpriced/deleted items), yearly analytics aggregations (12 monthly bars, elapsed month averages, purchase/stock unit costs), dynamic expiring labels (today, tomorrow, future, overdue), consumption trend product resolution, SetMonthlyBudgetDialog & FinancialAnalyticsDialog lifecycles and validation. |
| `QuickListViewModelTest` | Title and raw content updates, save delegation, item generation. |
| `ShoppingListsViewModelTest` | Active vs. completed list segregation, list deletion reactivity. |
| `PlannedListViewModelTest` | Budget input sanitization, product quantity increments/decrements, catalog filtering by name/brand/category (with accent insensitivity), save delegation. |
| `ShoppingAssistantViewModelTest` | Live cart session loading, item checking/unchecking, subtotal calculation, accent-insensitive search by name/category/brand, duplicate item detection and consolidation, barcode scanner subflows, item replacement, purchase finalization. |
| `CatalogViewModelTest` | Debounced EAN uniqueness validation, category filtering, accent-insensitive search suggestions, form saving with use case, custom category creation, renaming (with active filter & product cascade), and deletion (with fallback to `"Outros"`). |
| `PantryViewModelTest` | Category filtering, accent-insensitive search query filtering by name, brand, and compound name+brand, barcode scanned transitions, pantry item persistence, invoice QR code scanning, parsing, selective item toggling, bulk pantry import, notification dispatch. |
| `ProductDetailsViewModelTest` | Product detail resolution, price history stream, pantry batches join, quantity increments, batch price mapping and calculation, in-place price recalculation (min/max/average), checkout stock price display, batch editing subflow and updates, add to shopping list. |

### Running the Tests
```bash
# Run all unit tests in the uisystem module
./gradlew :uisystem:testDebugUnitTest

# Run full project test suite
./gradlew testDebugUnitTest
```

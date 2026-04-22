# uisystem Module Analysis

## 1. Module Purpose

The `:uisystem` module is a Compose Multiplatform module responsible for providing the UI components and theming for the MinhaDespensa application on both Android and iOS platforms. It follows an MVVM architecture and is responsible for the presentation layer of the application.

## 2. Module Dependencies

The module has the following key dependencies:

- **Compose Multiplatform:** For building the user interface.
- **Kotlinx Serialization:** For JSON serialization.
- **Koin:** For dependency injection.
- **ViewModel:** For implementing the MVVM pattern.
- **:core module:** It has an `api` dependency on the `:core` module, meaning it consumes and exposes the public API of the `:core` module. This indicates a tight coupling between the two modules.

## 3. Main Architecture

The module is structured around the MVVM (Model-View-ViewModel) architecture.

- **Views/Composables:** The UI components are built using Jetpack Compose.
- **ViewModels:** The `ProductsListViewModel` is responsible for preparing and managing the data for the `ProductListScreen`.
- **Models:** The `PantryItemUiModel` represents the data displayed on the UI.

## 4. Source Files

The source code is organized into the following packages:

### `components`

This package contains reusable UI components:

- `Text.kt`: Custom text components.
- `Menus.kt`: UI components for menus.
- `Utils.kt`: Utility functions for UI.
- `Charts.kt`: Components for displaying charts.
- `Structure.kt`: Structural components for screen layouts.
- `Navigation.kt`: Navigation-related components.
- `CommonConstants.kt`: Common constants used in the UI.
- `PrimaryContainerGlassCard.kt`: A card with a glass effect.
- `SecondaryContainerGlassCard.kt`: Another card with a glass effect.

### `features`

This package contains the different features of the UI:

- **`home`**:
    - `HomeScreen.kt`: The main screen of the application.
    - `widgets`: Widgets displayed on the home screen.
- **`list`**:
    - `ProductListScreen.kt`: Screen that displays a list of products.
    - `ProductsListViewModel.kt`: The ViewModel for the product list screen.
    - `AddProductSheet.kt`: A bottom sheet for adding new products.
    - `SettingsScreen.kt`: The settings screen.
    - `PantryItemUiModel.kt`: The UI model for a pantry item.
    - `UiState.kt`: Defines the state for the list feature.

### `theme`

This package contains the theming for the application:

- `Color.kt`: Defines the color palette.
- `Theme.kt`: The main theme of the application.
- `Type.kt`: Defines the typography.
- `Dimens.kt`: Defines the dimensions used in the UI.
- `AppBackground.kt`: Defines the application background.

### `di`

This package contains the dependency injection setup for the module:

- `UiModule.kt`: Koin module for the UI layer.

## 5. Public API

The module exposes the following public APIs:

- **Composables:**
    - `HomeScreen`
    - `ProductListScreen`
    - `SettingsScreen`
- **ViewModels:**
    - `ProductsListViewModel`
- **Theming:**
    - `MinhaDespensaTheme`

## 6. Usage

The `:uisystem` module is intended to be used by the main application module (`composeApp`) to build the user interface of the application. It encapsulates all the UI-related logic and components, promoting a clean separation of concerns.

# Minha Despensa 🛒

![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue?logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpack-compose)
![License](https://img.shields.io/badge/License-CC%20BY%204.0-lightgrey.svg)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

**Minha Despensa** (My Pantry) is a multiplatform application (Android & iOS) focused on home intelligence and financial control for grocery shopping.

## 🎯 The Problem
Do you know that feeling of adding items to your cart and, upon reaching the checkout, getting an unpleasant surprise with the total cost? Or that uncertainty about whether your monthly budget will cover everything?

## 💡 The Solution (MVP)
The initial goal of **Minha Despensa** is to act as a "smart shopping cart calculator".
As you shop, you input the items, quantities, and unit prices. The app instantly calculates the total and compares it with your set budget, empowering you to make purchasing decisions **before** reaching the checkout counter.

## 🚀 Roadmap and Future
The project is being built incrementally. Beyond the cart control (MVP), the upcoming planned features are:

- [ ] **Pantry Management (Inventory):** Track what you have at home to avoid duplicate purchases or waste.
- [ ] **Automatic Lists:** Shopping suggestions based on consumption habits and minimum stock levels.
- [ ] **Price History:** Analysis of product price variations to verify if a "sale" is real.
- [ ] **Recipes:** Recipe creation based on the ingredients currently available in your pantry.

## 🛠️ Tech Stack & Architecture

This project is a *showcase* of modern mobile development technologies, focused on scalability and clean code.

* **Language:** [Kotlin](https://kotlinlang.org/) (100%)
* **Platform:** [Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html) for sharing logic between Android and iOS.
* **UI:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Shared declarative UI).
* **Architecture:** Clean Architecture + MVVM.
* **Dependency Injection:** [Koin](https://insert-koin.io/).
* **Data Persistence:** Room Database (KMP).
* **Logs & Observability:** Custom implementation of Structured Logging.

### Module Structure
The project follows strict modularization to ensure separation of concerns:

| Module | Responsibility | Layer (Clean Arch) |
| :--- | :--- | :--- |
| `:composeApp` | Entry point, DI Configuration, and Integration. | Application / Main |
| `:uisystem` | Screens, Visual Components (Design System), and ViewModels. | Presentation |
| `:core` | Domain Models, Repository Interfaces, and Business Rules. Pure Kotlin. | Domain |
| `:data` | Database Implementation (Room), APIs, and Data Sources. | Data / Infrastructure |

## 📱 How to Run the Project

### Prerequisites
* Android Studio (Ladybug or newer) or IntelliJ IDEA.
* Xcode (to run the iOS app).
* JDK 17 or newer.
* Kotlin Multiplatform Mobile Plugin installed.

### Steps
1.  Clone the repository:
    ```bash
    git clone [https://github.com/YOUR-USERNAME/MinhaDespensa.git](https://github.com/YOUR-USERNAME/MinhaDespensa.git)
    ```
2.  Open the project in Android Studio and wait for the Gradle Sync.
3.  **Android:** Select the `composeApp` configuration and run it on the emulator.
4.  **iOS:** Open the `iosApp.xcworkspace` file (inside the `iosApp` folder) in Xcode, or run directly via Android Studio by configuring the iOS target.

## 🤝 Contribution

Contributions are welcome! If you have ideas to improve shopping management or want to help with the technical implementation:

1.  Fork the project.
2.  Create a Branch for your Feature (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the Branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the **Creative Commons Attribution 4.0 International (CC BY 4.0)** - see the [LICENSE](LICENSE) file for details.

---
*Developed with 💙 and Kotlin by Willian Santos.*
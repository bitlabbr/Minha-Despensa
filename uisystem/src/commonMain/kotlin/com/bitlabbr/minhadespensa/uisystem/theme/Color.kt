/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.theme

import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------------
// ACTION / BRAND
// -----------------------------------------------------------------------------
// These two colors are the endpoints of the Minha Despensa Action Gradient.
//
// primary  -> green
// secondary -> warm orange
//
// -----------------------------------------------------------------------------

val primaryLightAppColor = Color(0xFF76F9C7)
val onPrimaryLightAppColor = Color(0xFF26332F)

val secondaryLightAppColor = Color(0xFFFF7C64)
val onSecondaryLightAppColor = Color(0xFF3A2420)

val primaryDarkAppColor = Color(0xFF8AF3C8)
val onPrimaryDarkAppColor = Color(0xFF1D2B27)

val secondaryDarkAppColor = Color(0xFFFF927D)
val onSecondaryDarkAppColor = Color(0xFF38211D)

// -----------------------------------------------------------------------------
// TERTIARY
// -----------------------------------------------------------------------------
// Kept only for Material/Compose ColorScheme compatibility.
// It is NOT part of the application's primary interaction language.
// -----------------------------------------------------------------------------

val tertiaryLightAppColor = Color(0xFFD8D6C9)
val onTertiaryLightAppColor = Color(0xFF3E403B)

val tertiaryDarkAppColor = Color(0xFF5C5A50)
val onTertiaryDarkAppColor = Color(0xFFE9E7DB)

// -----------------------------------------------------------------------------
// BACKGROUND
// -----------------------------------------------------------------------------

val backgroundLightAppColor = Color(0xFFFFFCF5)
val onBackgroundLightAppColor = Color(0xFF343532)

val backgroundDarkAppColor = Color(0xFF1B1B19)
val onBackgroundDarkAppColor = Color(0xFFE5E4DD)

// -----------------------------------------------------------------------------
// SURFACE
// -----------------------------------------------------------------------------
// Used by elevated transient UI: BottomSheet, Dialog, Dropdown, Menu.
// -----------------------------------------------------------------------------

val surfaceLightAppColor = Color(0xF5FFFFFF)
val onSurfaceLightAppColor = Color(0xFF30312F)

val surfaceDarkAppColor = Color(0xF51F1F1D)
val onSurfaceDarkAppColor = Color(0xFFE8E7E0)

// -----------------------------------------------------------------------------
// PRIMARY GLASS
// -----------------------------------------------------------------------------
// Layer 1: the main glass container of a screen.
// -----------------------------------------------------------------------------

val primaryContainerLightAppColor = Color(0xFFEDEBE5)
val onPrimaryContainerLightAppColor = Color(0xFF4B504D)

val primaryContainerDarkAppColor = Color(0xFF292927)
val onPrimaryContainerDarkAppColor = Color(0xFFD9D8D1)

// -----------------------------------------------------------------------------
// SECONDARY GLASS
// -----------------------------------------------------------------------------
// Layer 2: child containers, widgets, form sections and product cards.
// -----------------------------------------------------------------------------

val secondaryContainerLightAppColor = Color(0xFFE6E4DE)
val onSecondaryContainerLightAppColor = Color(0xFF2F302E)

val secondaryContainerDarkAppColor = Color(0xFF363633)
val onSecondaryContainerDarkAppColor = Color(0xFFE3E1D9)
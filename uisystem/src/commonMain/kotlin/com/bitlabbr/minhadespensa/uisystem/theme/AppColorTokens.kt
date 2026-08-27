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

/**
 * Small semantic aliases for component code.
 *
 * Keeping these names here prevents components from importing raw palette
 * values such as financialGaugePrimaryColor or defaultButtonColor.
 */
object AppColorTokens {

    /**
     * Official brand/action colors.
     *
     * Components that need the complete action treatment should normally use
     * the Action Gradient instead of one color alone.
     */
    object Action {
        val primary = primaryLightAppColor
        val secondary = secondaryLightAppColor
    }

    /**
     * Colors reserved for visualization.
     *
     * The values intentionally point to the same action endpoints so gauges
     * and buttons belong to the same visual language.
     */
    object Visualization {
        val start = primaryLightAppColor
        val end = secondaryLightAppColor
    }
}
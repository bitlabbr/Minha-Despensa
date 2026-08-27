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

/**
 * Semantic state colors.
 *
 * Use these only when the UI needs to communicate a state.
 * Never use them as generic button/card/container colors.
 */
data class AppStatusColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,
    val info: Color,
    val onInfo: Color,
)

val lightAppStatusColors = AppStatusColors(
    success = Color(0xFF5BBF93),
    onSuccess = Color(0xFF123126),
    warning = Color(0xFFE5A24D),
    onWarning = Color(0xFF3A2712),
    error = Color(0xFFD86A5D),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF7B9D9A),
    onInfo = Color(0xFF172B2A),
)

val darkAppStatusColors = AppStatusColors(
    success = Color(0xFF78D6AD),
    onSuccess = Color(0xFF10271E),
    warning = Color(0xFFF0B96A),
    onWarning = Color(0xFF30200F),
    error = Color(0xFFFF8A7D),
    onError = Color(0xFF351713),
    info = Color(0xFFA4C6C2),
    onInfo = Color(0xFF172B2A),
)

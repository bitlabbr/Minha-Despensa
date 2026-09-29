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

package com.bitlabbr.minhadespensa.uisystem.manager

import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AppNotification(
    val message: UiText,
    val type: MinhaDespensaSnackbarType = MinhaDespensaSnackbarType.SUCCESS,
)

class AppNotificationManager {
    private val _notifications = MutableSharedFlow<AppNotification>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    fun showSuccess(message: UiText) {
        _notifications.tryEmit(AppNotification(message, MinhaDespensaSnackbarType.SUCCESS))
    }

    fun showError(message: UiText) {
        _notifications.tryEmit(AppNotification(message, MinhaDespensaSnackbarType.ERROR))
    }

    fun showWarning(message: UiText) {
        _notifications.tryEmit(AppNotification(message, MinhaDespensaSnackbarType.WARNING))
    }
}
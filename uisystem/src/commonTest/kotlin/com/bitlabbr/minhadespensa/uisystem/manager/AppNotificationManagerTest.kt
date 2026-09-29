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

import app.cash.turbine.test
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.MinhaDespensaSnackbarType
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNotificationManagerTest {

    private val notificationManager = AppNotificationManager()

    @Test
    fun `showSuccess emits SUCCESS notification with message`() = runTest {
        notificationManager.notifications.test {
            val message = UiText.DynamicString("Sucesso ao salvar item!")
            notificationManager.showSuccess(message)

            val notification = awaitItem()
            assertEquals(message, notification.message)
            assertEquals(MinhaDespensaSnackbarType.SUCCESS, notification.type)
        }
    }

    @Test
    fun `showError emits ERROR notification with message`() = runTest {
        notificationManager.notifications.test {
            val message = UiText.DynamicString("Erro inesperado no servidor")
            notificationManager.showError(message)

            val notification = awaitItem()
            assertEquals(message, notification.message)
            assertEquals(MinhaDespensaSnackbarType.ERROR, notification.type)
        }
    }

    @Test
    fun `showWarning emits WARNING notification with message`() = runTest {
        notificationManager.notifications.test {
            val message = UiText.DynamicString("Atenção: produto próximo ao vencimento")
            notificationManager.showWarning(message)

            val notification = awaitItem()
            assertEquals(message, notification.message)
            assertEquals(MinhaDespensaSnackbarType.WARNING, notification.type)
        }
    }
}

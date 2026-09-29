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

package com.bitlabbr.minhadespensa.uisystem.components.core.snackbar

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotification
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GlobalNotificationHost(
    modifier: Modifier = Modifier,
    notificationManager: AppNotificationManager = koinInject(),
) {
    var currentNotification by remember { mutableStateOf<AppNotification?>(null) }
    var resolvedText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        notificationManager.notifications.collect { notification ->
            resolvedText = notification.message.asStringAsync()
            currentNotification = notification
            delay(3500.milliseconds)
            currentNotification = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = MinhaDespensaTheme.dimens.paddingSmall),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = currentNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        ) {
            currentNotification?.let { notification ->
                MinhaDespensaSnackbar(
                    message = resolvedText,
                    type = notification.type,
                    onDismiss = { currentNotification = null },
                )
            }
        }
    }
}
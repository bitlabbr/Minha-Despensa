/*
 * Copyright (c) 2026 Willian Santos
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0
 * International License (CC BY-NC 4.0).
 *
 * You may use, copy, modify, and distribute this file for non-commercial
 * purposes only, provided that proper attribution is given.
 *
 * The copyright holder retains all commercial rights and may
 * license this work under different terms.
 *
 * License: https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.bill.minhadispensa.minhadispensa

import androidx.compose.ui.window.ComposeUIViewController
import com.bill.minhadispensa.minhadispensa.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    try {
        initKoin()
    } catch (e: Exception) {
        println("Koin já iniciado ou erro: ${e.message}")
    }

    return ComposeUIViewController {
        App()
    }
}
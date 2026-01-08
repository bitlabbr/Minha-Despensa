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

package com.bitlabbr.minhadespensa.app

import androidx.compose.ui.window.ComposeUIViewController
import com.bitlabbr.minhadespensa.app.di.appModule
import com.bitlabbr.minhadespensa.app.di.initKoin
import com.bitlabbr.minhadespensa.app.di.iosDatabaseModule
import com.bitlabbr.minhadespensa.uisystem.di.uiModule
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    try {
        initKoin{
            modules(iosDatabaseModule)
        }
    } catch (e: Exception) {
        println("Koin already initiated: ${e.message}")
    }

    return ComposeUIViewController {
        App()
    }
}
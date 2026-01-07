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
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


package com.bitlabbr.minhadespensa.app.di

import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.core.domain.util.DiQualifiers
import com.bitlabbr.minhadespensa.core.domain.util.DiagnosticLogRecorder
import com.bitlabbr.minhadespensa.core.domain.util.RingBufferLogRecorder
import com.bitlabbr.minhadespensa.data.di.dataModule
import com.bitlabbr.minhadespensa.uisystem.di.uiModule
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<DiagnosticLogRecorder> {
        RingBufferLogRecorder()
    }

    factory<AppLogger>(named(DiQualifiers.APP_LOGGER)) {
        ConsoleLogger(moduleName = "App", recorder = get())
    }

    factory<AppLogger>(named(DiQualifiers.CORE_LOGGER)) {
        ConsoleLogger(moduleName = "Core", recorder = get())
    }

    factory<AppLogger>(named(DiQualifiers.DATA_LOGGER)) {
        ConsoleLogger(moduleName = "Data", recorder = get())
    }
}

val sharedModules = listOf(
    appModule,
    uiModule,
    dataModule
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(sharedModules)
}
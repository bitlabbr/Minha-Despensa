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

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.ConsoleLogger
import com.bitlabbr.minhadespensa.uisystem.di.DiQualifiers
import com.bitlabbr.minhadespensa.uisystem.di.uiModule
import org.koin.core.qualifier.named
import com.bitlabbr.minhadespensa.data.di.dataModule


val appModule = module {
    factory<AppLogger>(named(DiQualifiers.APP_LOGGER)) {
        ConsoleLogger(moduleName = "App")
    }

    factory<AppLogger>(named(DiQualifiers.CORE_LOGGER)) {
        ConsoleLogger(moduleName = "Core")
    }

    factory<AppLogger>(named(DiQualifiers.DATA_LOGGER)) {
        ConsoleLogger(moduleName = "Data")
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
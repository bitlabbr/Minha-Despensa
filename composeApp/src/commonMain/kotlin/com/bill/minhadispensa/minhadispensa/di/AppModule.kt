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


package com.bill.minhadispensa.minhadispensa.di

import com.bill.minhadispensa.core.domain.repository.ProductRepository
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration
import com.bill.minhadispensa.core.domain.util.AppLogger
import com.bill.minhadispensa.core.domain.util.ConsoleLogger
import com.bill.minhadispensa.minhadispensa.data.repository.FakeProductRepository
import com.bill.minhadespensa.uisystem.di.uiModule
import org.koin.core.qualifier.named
import org.koin.dsl.bind


val appModule = module {
    factory<AppLogger>(named("app_logger")) {
        ConsoleLogger(moduleName = "App/Data")
    }

    factory<AppLogger>(named("core_logger")) {
        ConsoleLogger(moduleName = "Core")
    }

    single {
        FakeProductRepository(
            logger = get(named("app_logger"))
        )
    } bind ProductRepository::class
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModule, uiModule)
}
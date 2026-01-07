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

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration
import com.bill.minhadispensa.core.domain.repository.ProductRepository
import com.bill.minhadispensa.minhadispensa.data.repository.FakeProductRepository
import com.bill.minhadispensa.uisystem.theme.features.list.ProductsListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind


val appModule = module {
    single { FakeProductRepository() } bind ProductRepository::class
    viewModelOf(::ProductsListViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModule)
}
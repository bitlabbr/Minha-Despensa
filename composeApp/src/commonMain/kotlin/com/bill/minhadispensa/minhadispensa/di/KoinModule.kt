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
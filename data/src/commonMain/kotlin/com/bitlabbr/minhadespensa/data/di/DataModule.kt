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

package com.bitlabbr.minhadespensa.data.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import com.bitlabbr.minhadespensa.core.domain.util.DiQualifiers
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPriceRepository
import com.bitlabbr.minhadespensa.data.repository.RoomShoppingListRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().catalogDao() }
    single { get<AppDatabase>().pantryDao() }
    single { get<AppDatabase>().priceDao() }
    single { get<AppDatabase>().shoppingListDao() }

    single<CatalogRepository> {
        RoomCatalogRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<PantryRepository> {
        RoomPantryRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<PriceRepository> {
        RoomPriceRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<RoomShoppingListRepository> {
        RoomShoppingListRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }
}

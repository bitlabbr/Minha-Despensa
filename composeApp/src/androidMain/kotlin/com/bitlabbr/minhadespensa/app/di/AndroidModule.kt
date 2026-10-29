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

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bitlabbr.minhadespensa.app.provider.AndroidPackageInstaller
import com.bitlabbr.minhadespensa.app.provider.AndroidPlatformInfoProvider
import com.bitlabbr.minhadespensa.app.provider.AndroidSupportPlatformProvider
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.util.AppPackageInstaller
import com.bitlabbr.minhadespensa.data.util.AppPlatformInfoProvider
import com.bitlabbr.minhadespensa.data.util.SupportPlatformProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = androidContext().getDatabasePath("minha_despensa.db")
        Room.databaseBuilder<AppDatabase>(
            context = androidContext(),
            name = dbFile.absolutePath
        )
    }

    single<AppPlatformInfoProvider> {
        AndroidPlatformInfoProvider(context = androidContext())
    }

    single<AppPackageInstaller> {
        AndroidPackageInstaller(context = androidContext())
    }

    single<SupportPlatformProvider>(createdAtStart = true) {
        val provider = AndroidSupportPlatformProvider(context = androidContext())
        provider.installCrashHandler(recorder = getOrNull())
        provider
    }
}

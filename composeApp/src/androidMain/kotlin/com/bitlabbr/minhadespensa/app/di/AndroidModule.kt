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
 *
 */

package com.bitlabbr.minhadespensa.app.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bitlabbr.minhadespensa.data.local.AppDatabase
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
}

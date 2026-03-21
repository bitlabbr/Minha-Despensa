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

package com.bitlabbr.minhadespensa.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.bitlabbr.minhadespensa.data.local.converter.Converters
import com.bitlabbr.minhadespensa.data.local.dao.CatalogProductDao
import com.bitlabbr.minhadespensa.data.local.dao.PantryRepositoryDao
import com.bitlabbr.minhadespensa.data.local.dao.PriceEntryDao
import com.bitlabbr.minhadespensa.data.local.dao.ShoppingItemDao
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ShoppingItemEntity

@Database(
    entities = [
        CatalogProductEntity::class,
        PantryItemEntity::class,
        PriceEntryEntity::class,
        ShoppingItemEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun catalogDao(): CatalogProductDao
    abstract fun pantryDao(): PantryRepositoryDao
    abstract fun priceDao(): PriceEntryDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
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

package com.bitlabbr.minhadespensa.data.local

import androidx.room.TypeConverter
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import kotlinx.datetime.Instant

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromUnityString(value: String): MeasureUnit {
        return try {
            MeasureUnit.valueOf(value)
        } catch (e: Exception) {
            MeasureUnit.UNITY
        }
    }

    @TypeConverter
    fun unityToString(unity: MeasureUnit): String {
        return unity.name
    }
}

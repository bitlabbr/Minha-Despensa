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

package com.bitlabbr.minhadespensa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit

import kotlinx.datetime.Instant

@Entity(tableName = "product")
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val amount: Double,
    val measureUnit: MeasureUnit,
    val expirationDate: Instant?,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

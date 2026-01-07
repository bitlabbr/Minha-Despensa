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

package com.bill.minhadispensa.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val amount: Double,
    val unitMeasure: MeasureUnit,
    val expirationDate: Instant?,
    val imgUrl: String? = null
)
enum class MeasureUnit {
    UNITY,
    KILOGRAM,
    LITER,
    PACKAGE
}
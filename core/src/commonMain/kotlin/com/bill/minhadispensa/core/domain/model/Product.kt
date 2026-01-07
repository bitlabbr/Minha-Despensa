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
package com.assignment.models

import kotlinx.serialization.Serializable

@Serializable
data class Discount(
    val discountId: String,
    val percent: Double,
)

@Serializable
data class ApplyDiscountRequest(
    val discountId: String,
    val percent: Double,
)

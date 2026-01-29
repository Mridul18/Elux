package com.assignment.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Product(
    val id: String,
    val name: String,
    val basePrice: Double,
    val country: Country,
    val discounts: List<Discount> = emptyList(),
) {
    fun calculateFinalPrice(vat: Double): Double {
        val totalDiscount = discounts.sumOf { it.percent } / 100.0
        return basePrice * (1 - totalDiscount) * (1 + vat / 100.0)
    }
}

@Serializable
data class ProductCreationRequest(
    val name: String,
    val basePrice: Double,
    val country: Country,
){
    fun toProduct(): Product {
        return Product(
            id = UUID.randomUUID().toString(),
            name = name,
            basePrice = basePrice,
            country = country,
            discounts = emptyList()
        )
    }
}

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val basePrice: Double,
    val country: Country,
    val discounts: List<Discount>,
    val finalPrice: Double,
)


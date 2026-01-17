package com.assignment.validator

import com.assignment.models.ApplyDiscountRequest
import com.assignment.models.Country
import com.assignment.models.Product

class ValidationException(message: String) : IllegalArgumentException(message)

object ProductValidator {
    fun validateCountry(country: String?): Country {
        if (country.isNullOrBlank()) {
            throw ValidationException("Country parameter is required")
        }

        return try {
            Country.fromStringOrThrow(country)
        } catch (e: IllegalArgumentException) {
            val supportedCountries = Country.getSupportedCountries()
            throw ValidationException("Invalid country. Supported countries: $supportedCountries")
        }
    }

    fun validateProductId(productId: String?): String {
        if (productId.isNullOrBlank()) {
            throw ValidationException("Product ID is required")
        }
        return productId
    }

    fun validateDiscountRequest(request: ApplyDiscountRequest) {
        validateDiscountPercent(request.percent)

        if (request.discountId.isBlank()) {
            throw ValidationException("Discount ID is required")
        }
    }

    fun validateDiscountPercent(percent: Double) {
        if (percent <= 0 || percent >= 100) {
            throw ValidationException("Discount percent must be between 0 and 100 (exclusive)")
        }
    }

    fun validateProduct(product: Product) {
        if (product.id.isBlank()) {
            throw ValidationException("Product ID is required")
        }

        if (product.name.isBlank()) {
            throw ValidationException("Product name is required")
        }

        if (product.basePrice < 0) {
            throw ValidationException("Product base price must be non-negative")
        }
    }
}

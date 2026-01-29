package com.assignment.validator

import com.assignment.exceptions.ApplyDiscountRequestInvalidException
import com.assignment.exceptions.InvalidCountryInRequestException
import com.assignment.models.ApplyDiscountRequest
import com.assignment.models.Country
import com.assignment.models.ProductCreationRequest

class ValidationException(message: String) : IllegalArgumentException(message)

object ProductValidator {
    fun validateCountry(country: String?): Country {
        if (country.isNullOrBlank()) {
            throw InvalidCountryInRequestException("Invalid country $country")
        }
        return Country.fromString(country)
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
            throw ApplyDiscountRequestInvalidException("Discount ID is required")
        }
    }

    fun validateProductRequest(productCreationRequest: ProductCreationRequest) {
        if (productCreationRequest.name.isBlank()) {
            throw ValidationException("Product name is required")
        }

        if (productCreationRequest.basePrice <= 0) {
            throw ValidationException("Product base price must be non-negative and non-zero")
        }
    }

    private fun validateDiscountPercent(percent: Double) {
        if (percent <= 0 || percent >= 100) {
            throw ApplyDiscountRequestInvalidException("Discount percentage must be between 0 and 100")
        }
    }
}

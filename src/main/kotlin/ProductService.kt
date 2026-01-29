package com.assignment

import com.assignment.exceptions.ProductToBeDiscountedNotFoundException
import com.assignment.models.Country
import com.assignment.models.Product
import com.assignment.models.ProductCreationRequest
import com.assignment.models.ProductResponse
import com.assignment.repository.ProductRepository

class ProductService(private val productRepository: ProductRepository) {
    suspend fun getProductsByCountry(country: Country): List<ProductResponse> {
        val products = productRepository.findByCountry(country)

        return products.map { product ->
            val vat = product.country.VAT
            val finalPrice = product.calculateFinalPrice(vat)

            ProductResponse(
                id = product.id,
                name = product.name,
                basePrice = product.basePrice,
                country = product.country,
                discounts = product.discounts,
                finalPrice = finalPrice,
            )
        }
    }

    suspend fun createProduct(productCreationRequest: ProductCreationRequest): Product {
        val product = productCreationRequest.toProduct()
        return productRepository.save(product)
    }

    suspend fun applyDiscount(
        productId: String,
        discountId: String,
        percent: Double,
    ): Boolean {
        productRepository.findById(productId)
            ?: throw ProductToBeDiscountedNotFoundException(productId)

        return productRepository.applyDiscount(productId, discountId, percent)
    }
}

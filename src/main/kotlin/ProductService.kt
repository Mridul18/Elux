package com.assignment

import com.assignment.models.Country
import com.assignment.models.Product
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

    suspend fun createProduct(product: Product) {
        productRepository.save(product)
    }

    suspend fun applyDiscount(
        productId: String,
        discountId: String,
        percent: Double,
    ): Boolean {
        if (percent <= 0 || percent >= 100) {
            throw IllegalArgumentException("Discount percent must be between 0 and 100 (exclusive)")
        }

        val product = productRepository.findById(productId)
        if (product == null) {
            throw IllegalArgumentException("Product with id $productId not found")
        }

        return productRepository.applyDiscount(productId, discountId, percent)
    }
}

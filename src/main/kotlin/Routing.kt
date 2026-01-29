package com.assignment

import com.assignment.models.ApplyDiscountRequest
import com.assignment.models.Product
import com.assignment.models.ProductCreationRequest
import com.assignment.validator.ProductValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.configureRouting(productService: ProductService) {
    routing {
        post("/products") {
            val productCreationRequest = call.receive<ProductCreationRequest>()
            ProductValidator.validateProductRequest(productCreationRequest)
            val product: Product = productService.createProduct(productCreationRequest)

            call.respond(HttpStatusCode.Created, mapOf("id" to product.id))
        }

        get("/products") {
            val countryParam = call.request.queryParameters["country"]
            val validatedCountry = ProductValidator.validateCountry(countryParam)
            val products = productService.getProductsByCountry(validatedCountry)
            call.respond(HttpStatusCode.OK, products)
        }

        put("/products/{id}/discount") {
            val productId = ProductValidator.validateProductId(call.parameters["id"])
            val applyDiscountRequest = call.receive<ApplyDiscountRequest>()
            ProductValidator.validateDiscountRequest(applyDiscountRequest)

            val wasApplied =
                productService.applyDiscount(
                    productId = productId,
                    discountId = applyDiscountRequest.discountId,
                    percent = applyDiscountRequest.percent,
                )

            if (wasApplied) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Discount applied successfully"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Discount already applied (idempotent)"))
            }
        }
    }
}

package com.assignment

import com.assignment.exceptions.ApplyDiscountRequestInvalidException
import com.assignment.exceptions.InvalidCountryInRequestException
import com.assignment.exceptions.ProductToBeDiscountedNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<ProductToBeDiscountedNotFoundException>{ call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to (cause.message ?: "Product not found"))
            )
        }

        exception<ApplyDiscountRequestInvalidException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Invalid apply discount request"))
            )
        }

        exception<InvalidCountryInRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Invalid country in request"))
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Invalid request")),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "An unexpected error occurred: ${cause.message}"),
            )
        }
    }
}

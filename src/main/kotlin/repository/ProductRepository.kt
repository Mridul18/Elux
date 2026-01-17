package com.assignment.repository

import com.assignment.database.ProductDiscounts
import com.assignment.database.Products
import com.assignment.models.Country
import com.assignment.models.Discount
import com.assignment.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal

class ProductRepository(private val database: Database) {
    suspend fun findByCountry(country: Country): List<Product> =
        withContext(Dispatchers.IO) {
            newSuspendedTransaction(db = database) {
                Products
                    .selectAll().where { Products.country.eq(country.name) }
                    .map { row -> row.toProduct() }
            }
        }

    suspend fun findById(productId: String): Product? =
        withContext(Dispatchers.IO) {
            newSuspendedTransaction(db = database) {
                Products
                    .selectAll().where { Products.id.eq(productId) }
                    .singleOrNull()
                    ?.toProduct()
            }
        }

    suspend fun save(product: Product): Unit =
        withContext(Dispatchers.IO) {
            newSuspendedTransaction(db = database) {
                Products.insert {
                    it[id] = product.id
                    it[name] = product.name
                    it[basePrice] = BigDecimal.valueOf(product.basePrice)
                    it[country] = product.country.name
                }
            }
        }

    suspend fun applyDiscount(
        productId: String,
        discountId: String,
        percent: Double,
    ): Boolean =
        withContext(Dispatchers.IO) {
            newSuspendedTransaction(db = database) {
                try {
                    ProductDiscounts.insert {
                        it[ProductDiscounts.productId] = productId
                        it[ProductDiscounts.discountId] = discountId
                        it[ProductDiscounts.percent] = BigDecimal.valueOf(percent)
                    }
                    true
                } catch (e: ExposedSQLException) {
                    if (isUniqueConstraintViolation(e)) {
                        false
                    } else {
                        throw e
                    }
                }
            }
        }

    private fun isUniqueConstraintViolation(e: ExposedSQLException): Boolean {
        val sqlState = e.sqlState
        val message = e.message?.uppercase() ?: ""
        return sqlState == "23505" ||
            message.contains("PRIMARY KEY") ||
            message.contains("UNIQUE CONSTRAINT") ||
            message.contains("UNIQUE VIOLATION")
    }

    private fun ResultRow.toProduct(): Product {
        val productId = this[Products.id]
        val discounts =
            ProductDiscounts
                .selectAll().where { ProductDiscounts.productId.eq(productId) }
                .map { row ->
                    Discount(
                        discountId = row[ProductDiscounts.discountId],
                        percent = row[ProductDiscounts.percent].toDouble(),
                    )
                }
        val countryString = this[Products.country]
        val country = Country.fromStringOrThrow(countryString)

        return Product(
            id = productId,
            name = this[Products.name],
            basePrice = this[Products.basePrice].toDouble(),
            country = country,
            discounts = discounts,
        )
    }
}

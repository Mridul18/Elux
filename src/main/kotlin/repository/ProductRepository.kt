package com.assignment.repository

import com.assignment.database.ProductDiscounts
import com.assignment.database.Products
import com.assignment.models.Country
import com.assignment.models.Discount
import com.assignment.models.Product
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal

class ProductRepository(private val database: Database) {
    suspend fun findByCountry(country: Country): List<Product> =
        newSuspendedTransaction(db = database) {
            val rows =
                Products.leftJoin(ProductDiscounts, { Products.id }, { ProductDiscounts.productId })
                    .selectAll().where { Products.country eq country.name }

            rows.groupBy { it[Products.id] }.map { (_, groupedRows) -> groupedRows.toProductWithDiscounts() }
        }

    suspend fun findById(productId: String): Product? =
        newSuspendedTransaction(db = database) {

            val rows =
                Products
                    .leftJoin(
                        ProductDiscounts,
                        { Products.id },
                        { ProductDiscounts.productId }
                    )
                    .selectAll().where { Products.id eq productId }
                    .toList()

            if (rows.isEmpty()) return@newSuspendedTransaction null

            rows.toProductWithDiscounts()
        }

    suspend fun save(product: Product): Product =
        newSuspendedTransaction(db = database) {
            Products.insert {
                it[id] = product.id
                it[name] = product.name
                it[basePrice] = BigDecimal.valueOf(product.basePrice)
                it[country] = product.country.name
            }
            product
        }

    suspend fun applyDiscount(
        productId: String,
        discountId: String,
        percent: Double,
    ): Boolean =
        newSuspendedTransaction(db = database) {
            ProductDiscounts.insertIgnore {
                it[ProductDiscounts.productId] = productId
                it[ProductDiscounts.discountId] = discountId
                it[ProductDiscounts.percent] = BigDecimal.valueOf(percent)
            }.insertedCount > 0
        }

    private fun List<ResultRow>.toProductWithDiscounts(): Product {
        val first = first()
        val discounts =
            mapNotNull { row ->
                row[ProductDiscounts.discountId].let { discountId ->
                    Discount(
                        discountId = discountId,
                        percent = row[ProductDiscounts.percent].toDouble(),
                    )
                }
            }

        return Product(
            id = first[Products.id],
            name = first[Products.name],
            basePrice = first[Products.basePrice].toDouble(),
            country = Country.fromString(first[Products.country]),
            discounts = discounts,
        )
    }
}

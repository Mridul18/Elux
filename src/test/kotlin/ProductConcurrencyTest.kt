package com.assignment

import com.assignment.models.Country
import com.assignment.models.Product
import com.assignment.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductConcurrencyTest {
    private lateinit var database: Database
    private lateinit var repository: ProductRepository
    private lateinit var service: ProductService

    @BeforeTest
    fun setup() =
        runBlocking {
            database =
                Database.connect(
                    url = "jdbc:h2:mem:test${System.currentTimeMillis()};DB_CLOSE_DELAY=-1",
                    driver = "org.h2.Driver",
                    user = "root",
                    password = "",
                )
            initializeProductTables(database)
            repository = ProductRepository(database)
            service = ProductService(repository)

            val product =
                Product(
                    id = "concurrent-test-product",
                    name = "Concurrent Test Product",
                    basePrice = 100.0,
                    country = Country.SWEDEN,
                )
            service.createProduct(product)
        }

    @AfterTest
    fun tearDown() {
        // H2 in-memory database is automatically cleaned up
    }

    @Test
    fun `test concurrent discount application - only one discount should be persisted`() =
        runBlocking {
            val productId = "concurrent-test-product"
            val discountId = "concurrent-discount-1"
            val numberOfConcurrentRequests = 50

            val results =
                coroutineScope {
                    (1..numberOfConcurrentRequests).map {
                        async {
                            try {
                                service.applyDiscount(productId, discountId, 10.0)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }.awaitAll()
                }

            val successfulRequests = results.count { it }
            assertTrue(successfulRequests > 0, "At least one request should succeed")

            val discountCount =
                runBlocking {
                    val product = repository.findById(productId)
                    product?.discounts?.count { it.discountId == discountId } ?: 0
                }

            assertEquals(
                1,
                discountCount,
                "Only one discount should be persisted despite $numberOfConcurrentRequests concurrent requests",
            )
        }
}

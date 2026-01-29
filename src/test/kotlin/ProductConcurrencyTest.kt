package com.assignment

import com.assignment.database.initializeProductTables
import com.assignment.models.Country
import com.assignment.models.ProductCreationRequest
import com.assignment.repository.ProductRepository
import com.assignment.service.ProductService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.junit.Assume.assumeTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductConcurrencyTest {
    private lateinit var database: Database
    private lateinit var repository: ProductRepository
    private lateinit var service: ProductService
    private lateinit var productId: String

    @BeforeTest
    fun setup() {
        runBlocking {
            val url = System.getenv("POSTGRES_TEST_URL")
            val user = System.getenv("POSTGRES_TEST_USER")
            val password = System.getenv("POSTGRES_TEST_PASSWORD")

            if (url.isNullOrBlank() || user.isNullOrBlank() || password.isNullOrBlank()) {
                println(
                    "Skipping ProductConcurrencyTest: POSTGRES_TEST_URL/POSTGRES_TEST_USER/POSTGRES_TEST_PASSWORD " +
                        "not set. Start postgres via docker-compose and export these vars to run this test.",
                )
                assumeTrue(false)
            }

            database =
                Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password,
                )

            initializeProductTables(database)

            repository = ProductRepository(database)
            service = ProductService(repository)

            val product =
                ProductCreationRequest(
                    name = "Concurrent Test Product",
                    basePrice = 100.0,
                    country = Country.SWEDEN,
                )

            productId = service.createProduct(product).id
        }
    }

    @AfterTest
    fun tearDown() {
        // postgres lifecycle handled externally (docker-compose)
    }

    @Test
    fun `test concurrent discount application - only one discount should be persisted remaining should be dropped`() =
        runBlocking {
            val discountId = "concurrent-discount-1"
            val numberOfConcurrentRequests = 50

            val results =
                coroutineScope {
                    (1..numberOfConcurrentRequests).map {
                        async {
                            service.applyDiscount(
                                productId = productId,
                                discountId = discountId,
                                percent = 10.0,
                            )
                        }
                    }.awaitAll()
                }

            val appliedCount = results.count { it }
            val ignoredCount = results.count { !it }

            // Exactly one request should apply the discount
            assertEquals(
                1,
                appliedCount,
                "Exactly one request should apply the discount",
            )

            assertEquals(
                numberOfConcurrentRequests - 1,
                ignoredCount,
                "All other requests should be idempotent no-ops",
            )

            val product = repository.findById(productId)
            assertTrue(product != null)

            val discountCount =
                product.discounts.count { it.discountId == discountId }

            assertEquals(
                1,
                discountCount,
                "Database must contain exactly one discount row",
            )
        }
}

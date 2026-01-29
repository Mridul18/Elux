package com.assignment.database

import com.assignment.handler.configureRouting
import com.assignment.repository.ProductRepository
import com.assignment.service.ProductService
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val database = connectToDatabase()
    initializeProductTables(database)

    val productRepository = ProductRepository(database)
    val productService = ProductService(productRepository)
    configureRouting(productService)
}

fun Application.connectToDatabase(): Database {
    Class.forName("org.postgresql.Driver")

    fun getConfig(
        envKey: String,
        configKey: String,
    ): String {
        return System.getenv(envKey) ?: environment.config.property(configKey).getString()
    }

    val url = getConfig("POSTGRES_URL", "postgres.url")
    val user = getConfig("POSTGRES_USER", "postgres.user")
    val password = getConfig("POSTGRES_PASSWORD", "postgres.password")

    log.info("Connecting to PostgreSQL database at $url")
    return Database.connect(url = url, driver = "org.postgresql.Driver", user = user, password = password)
}

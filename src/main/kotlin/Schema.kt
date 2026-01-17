package com.assignment

import com.assignment.database.ProductDiscounts
import com.assignment.database.Products
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeProductTables(database: Database) {
    transaction(database) {
        SchemaUtils.create(Products, ProductDiscounts)
    }
}

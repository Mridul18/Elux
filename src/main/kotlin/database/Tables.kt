package com.assignment.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Products : Table("products") {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val basePrice = decimal("base_price", 10, 2)
    val country = varchar("country", 255)

    override val primaryKey = PrimaryKey(id, name = "PK_PRODUCTS")
}

object ProductDiscounts : Table("product_discounts") {
    val productId = varchar("product_id", 255)
    val discountId = varchar("discount_id", 255)
    val percent = decimal("percent", 5, 2)

    override val primaryKey = PrimaryKey(productId, discountId, name = "PK_PRODUCT_DISCOUNTS")

    init {
        foreignKey(productId to Products.id, onDelete = ReferenceOption.CASCADE)
    }
}

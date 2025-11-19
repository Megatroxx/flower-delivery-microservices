package com.example.cart

import org.jetbrains.exposed.sql.Table

object CartItemsTable : Table("cart_items") {
    val id = varchar("id", 36)
    val userId = varchar("userId", 36)
    val productId = varchar("productId", 36)
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
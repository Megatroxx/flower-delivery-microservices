package com.example.order_items

import org.jetbrains.exposed.sql.Table

object OrderItemsTable : Table("order_items") {
    val id = varchar("id", 36)
    val orderId = varchar("orderId", 36)
    val productId = varchar("productId", 255)
    val quantity = integer("quantity")
    val price = integer("price")

    override val primaryKey = PrimaryKey(id)
}
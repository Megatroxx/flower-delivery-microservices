package com.example.orders

import org.jetbrains.exposed.sql.Table

object OrdersTable : Table("orders") {
    val id = varchar("id", 36)
    val userId = varchar("userId", 36)
    val address = text("address")
    val amount = integer("amount")
    val status = varchar("status", 50)
    val createdAt = varchar("createdAt", 50)
    val updatedAt = varchar("updatedAt", 50).nullable()
    val cancelledAt = varchar("cancelledAt", 50).nullable()
    val deliveryId = varchar("deliveryId", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}
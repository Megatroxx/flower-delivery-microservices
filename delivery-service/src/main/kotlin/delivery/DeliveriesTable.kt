package com.example.delivery

import org.jetbrains.exposed.sql.Table

object DeliveriesTable : Table("deliveries") {
    val id = varchar("id", 36)
    val orderId = varchar("orderId", 36)
    val address = text("address")
    val courierId = varchar("courierId", 50).nullable()
    val status = varchar("status", 50)
    val startedAt = varchar("startedAt", 50)
    val updatedAt = varchar("updatedAt", 50).nullable()
    val deliveredAt = varchar("deliveredAt", 50).nullable()
    val recipientSignature = text("recipientSignature").nullable()

    override val primaryKey = PrimaryKey(id)
}
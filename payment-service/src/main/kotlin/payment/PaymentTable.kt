package com.example.payment

import org.jetbrains.exposed.sql.Table

object PaymentTable : Table("payments") {
    val id = varchar("id", 36)
    val orderId = varchar("orderId", 36)
    val userId = varchar("userId", 36)
    val amount = integer("amount")
    val paymentMethod = varchar("paymentMethod", 50)
    val status = varchar("status", 20)
    val createdAt = varchar("createdAt", 50)
    val updatedAt = varchar("updatedAt", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}
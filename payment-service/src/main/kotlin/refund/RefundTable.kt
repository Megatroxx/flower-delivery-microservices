package com.example.refund

import org.jetbrains.exposed.sql.Table

object RefundTable : Table("refunds") {
    val id = varchar("id", 36)
    val orderId = varchar("orderId", 36)
    val amount = integer("amount")
    val status = varchar("status", 20)
    val processedAt = varchar("processedAt", 50)

    override val primaryKey = PrimaryKey(id)
}
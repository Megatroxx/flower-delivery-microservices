package com.example

import com.example.payment.PaymentTable
import com.example.refund.RefundTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect("jdbc:sqlite:payments.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(PaymentTable, RefundTable)
        }
    }
}
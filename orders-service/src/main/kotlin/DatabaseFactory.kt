package com.example

import com.example.order_items.OrderItemsTable
import com.example.orders.OrdersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:orders.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(OrdersTable, OrderItemsTable)
        }
    }
}
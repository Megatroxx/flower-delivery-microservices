package com.example

import com.example.delivery.DeliveriesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:delivery.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(DeliveriesTable)
        }
    }
}
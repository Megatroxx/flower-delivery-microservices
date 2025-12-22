package com.example

import com.example.flowers.FlowersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {

        val postgresUrl = System.getenv("POSTGRES_URL")

        if (postgresUrl != null) {
            Database. connect(postgresUrl)
        } else {
            Database. connect("jdbc:sqlite:catalog. db", driver = "org.sqlite.JDBC")
        }

        transaction {
            SchemaUtils.create(FlowersTable)
        }
    }
}
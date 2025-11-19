package com.example

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.cart.CartItemsTable

fun main() {
    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    initDatabase()
    configureSerialization()
    configureSecurity()
    configureRouting()
}

fun initDatabase() {
    Database.connect("jdbc:sqlite:cart.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(CartItemsTable)
    }
}

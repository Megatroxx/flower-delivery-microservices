package com.example

import com.example.users.UserTable
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(
        CIO,
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    initDatabase()

    configureSecurity()
    configureSerialization()
    configureRouting()
}

fun initDatabase() {
    Database.connect("jdbc:sqlite:users.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(UserTable)
    }
}

package com.example.users

import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val name = varchar("name", 255)
    val role = varchar("role", 50)
    val createdAt = varchar("createdAt", 50)

    override val primaryKey = PrimaryKey(id)
}
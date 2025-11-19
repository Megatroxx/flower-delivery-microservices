package com.example.flowers

import org.jetbrains.exposed.sql.Table

object FlowersTable : Table("flowers") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val description = text("description")
    val price = integer("price")
    val imageUrl = varchar("imageUrl", 512)
    val createdAt = varchar("createdAt", 50)
    val updatedAt = varchar("updatedAt", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}
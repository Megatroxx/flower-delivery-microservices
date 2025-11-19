package com.example.feedback

import org.jetbrains.exposed.sql.Table

object RatingsTable : Table("ratings") {
    val id = varchar("id", 36)
    val userId = varchar("userId", 36)
    val flowerId = varchar("flowerId", 36)
    val rating = integer("rating")
    val ratedAt = varchar("ratedAt", 50)

    override val primaryKey = PrimaryKey(id)
}

object ReviewsTable : Table("reviews") {
    val id = varchar("id", 36)
    val userId = varchar("userId", 36)
    val flowerId = varchar("flowerId", 36)
    val review = text("review")
    val reviewedAt = varchar("reviewedAt", 50)

    override val primaryKey = PrimaryKey(id)
}
package com.example.feedback

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID


class FeedbackRepository {

    fun rate(userId: String, req: RatingRequest): RatingResponse {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()

        transaction {
            RatingsTable.insert {
                it[RatingsTable.id] = id
                it[RatingsTable.userId] = userId
                it[RatingsTable.flowerId] = req.flowerId
                it[RatingsTable.rating] = req.rating
                it[RatingsTable.ratedAt] = now
            }
        }

        return RatingResponse(
            ratingId = id,
            flowerId = req.flowerId,
            status = "rated",
            ratedAt = now
        )
    }

    fun review(userId: String, req: ReviewRequest): ReviewResponse {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()

        transaction {
            ReviewsTable.insert {
                it[ReviewsTable.id] = id
                it[ReviewsTable.userId] = userId
                it[ReviewsTable.flowerId] = req.flowerId
                it[ReviewsTable.review] = req.review
                it[ReviewsTable.reviewedAt] = now
            }
        }

        return ReviewResponse(
            reviewId = id,
            flowerId = req.flowerId,
            status = "reviewed",
            reviewedAt = now
        )
    }
}
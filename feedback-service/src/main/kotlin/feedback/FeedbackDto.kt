package com.example.feedback

import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    val flowerId: String,
    val rating: Int
)

@Serializable
data class RatingResponse(
    val ratingId: String,
    val flowerId: String,
    val status: String,
    val ratedAt: String
)

@Serializable
data class ReviewRequest(
    val flowerId: String,
    val review: String
)

@Serializable
data class ReviewResponse(
    val reviewId: String,
    val flowerId: String,
    val status: String,
    val reviewedAt: String
)
package com.example.feedback

import com.example.catalog_client.CatalogClient

class FeedbackService(
    private val repo: FeedbackRepository,
    private val catalogClient: CatalogClient
) {

    suspend fun rate(userId: String, req: RatingRequest): RatingResponse {
        if (req.rating !in 1..5)
            throw IllegalArgumentException("Rating must be between 1 and 5")

        if (!catalogClient.flowerExists(req.flowerId))
            throw IllegalArgumentException("Flower does not exist")

        return repo.rate(userId, req)
    }

    suspend fun review(userId: String, req: ReviewRequest): ReviewResponse {
        if (req.review.isBlank())
            throw IllegalArgumentException("Review cannot be empty")

        if (!catalogClient.flowerExists(req.flowerId))
            throw IllegalArgumentException("Flower does not exist")

        return repo.review(userId, req)
    }
}
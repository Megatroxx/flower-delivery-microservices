package com.example.feedback

import com.example.catalog_client.CatalogClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeedbackServiceTest {

    private lateinit var repo: FeedbackRepository
    private lateinit var catalogClient: CatalogClient
    private lateinit var service: FeedbackService

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        catalogClient = mockk(relaxed = true)
        service = FeedbackService(repo, catalogClient)
    }

    @Test
    fun rateSavesFeedbackWhenFlowerExists() = runBlocking {
        val request = RatingRequest(flowerId = "flower-1", rating = 5)
        val response = RatingResponse("rating-1", request.flowerId, "rated", "2024-01-01T00:00:00Z")

        coEvery { catalogClient.flowerExists(request.flowerId) } returns true
        every { repo.rate("user-1", request) } returns response

        val result = service.rate("user-1", request)

        assertEquals(response, result)
        verify(exactly = 1) { repo.rate("user-1", request) }
    }

    @Test
    fun rateFailsForInvalidRating() = runBlocking {
        val request = RatingRequest(flowerId = "flower-1", rating = 0)

        assertFailsWith<IllegalArgumentException> {
            service.rate("user-1", request)
        }
        verify(exactly = 0) { repo.rate(any(), any()) }
    }

    @Test
    fun rateFailsWhenFlowerMissing() = runBlocking {
        val request = RatingRequest(flowerId = "flower-1", rating = 5)
        coEvery { catalogClient.flowerExists(request.flowerId) } returns false

        assertFailsWith<IllegalArgumentException> {
            service.rate("user-1", request)
        }
        verify(exactly = 0) { repo.rate(any(), any()) }
    }

    @Test
    fun reviewSavesFeedbackWhenFlowerExists() = runBlocking {
        val request = ReviewRequest(flowerId = "flower-1", review = "Great")
        val response = ReviewResponse("review-1", request.flowerId, "reviewed", "2024-01-01T00:00:00Z")

        coEvery { catalogClient.flowerExists(request.flowerId) } returns true
        every { repo.review("user-1", request) } returns response

        val result = service.review("user-1", request)

        assertEquals(response, result)
        verify(exactly = 1) { repo.review("user-1", request) }
    }

    @Test
    fun reviewFailsForEmptyText() = runBlocking {
        val request = ReviewRequest(flowerId = "flower-1", review = "  ")

        assertFailsWith<IllegalArgumentException> {
            service.review("user-1", request)
        }
        verify(exactly = 0) { repo.review(any(), any()) }
    }

    @Test
    fun reviewFailsWhenFlowerMissing() = runBlocking {
        val request = ReviewRequest(flowerId = "flower-1", review = "Good")
        coEvery { catalogClient.flowerExists(request.flowerId) } returns false

        assertFailsWith<IllegalArgumentException> {
            service.review("user-1", request)
        }
        verify(exactly = 0) { repo.review(any(), any()) }
    }
}


package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.feedback.FeedbackService
import com.example.feedback.RatingRequest
import com.example.feedback.RatingResponse
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun ratingEndpointReturnsOk() = testApplication {
        val service = mockk<FeedbackService>()
        val response = RatingResponse("rating-1", "flower-1", "rated", "2024-01-01T00:00:00Z")
        coEvery { service.rate("user-1", RatingRequest("flower-1", 5)) } returns response

        application {
            configureSerialization()
            configureSecurity()
            configureRouting(service)
        }

        val token = JWT.create()
            .withIssuer("http://0.0.0.0:8080/")
            .withAudience("users")
            .withClaim("id", "user-1")
            .sign(Algorithm.HMAC256("super_secret_jwt_key_123456789"))

        val httpResponse = client.post("/feedback/rating") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"flowerId":"flower-1","rating":5}""")
        }

        assertEquals(HttpStatusCode.OK, httpResponse.status)

        coVerify(exactly = 1) { service.rate("user-1", RatingRequest("flower-1", 5)) }
    }
}

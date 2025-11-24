package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.cart.AddItemRequest
import com.example.cart.CartItemResponse
import com.example.cart.CartService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun addItemEndpointReturnsOk() = testApplication {
        val service = mockk<CartService>()
        every { service.addItem("user-1", AddItemRequest("prod-1", 2)) } returns CartItemResponse(
            id = "item-1",
            productId = "prod-1",
            quantity = 2
        )

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

        val response = client.post("/cart/items") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId":"prod-1","quantity":2}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        verify(exactly = 1) { service.addItem("user-1", AddItemRequest("prod-1", 2)) }
    }
}

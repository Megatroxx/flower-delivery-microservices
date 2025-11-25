package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.cart.AddItemRequest
import com.example.cart.CartItemResponse
import com.example.cart.CartRepository
import com.example.cart.CartService
import com.example.cart.UpdateQuantityRequest
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun updateQuantity_itemNotFound_throwsException() {
        val repo = mockk<CartRepository>()
        val service = CartService(repo)

        every { repo.updateQuantity("user-1", "prod-1", 5) } returns false

        assertFailsWith<NoSuchElementException> {
            service.updateQuantity("user-1", "prod-1", UpdateQuantityRequest(5))
        }

        verify(exactly = 1) { repo.updateQuantity("user-1", "prod-1", 5) }
    }

    @Test
    fun addItem_returnsRepositoryResult() {
        val repo = mockk<CartRepository>()
        val service = CartService(repo)

        val expected = CartItemResponse(
            id = "item-1",
            productId = "prod-1",
            quantity = 3
        )

        every {
            repo.addItem("user-1", "prod-1", 3)
        } returns expected

        val result = service.addItem("user-1", AddItemRequest("prod-1", 3))

        assertEquals(expected, result)

        verify(exactly = 1) { repo.addItem("user-1", "prod-1", 3) }
    }
}

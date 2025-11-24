package com.example

import com.example.orders.CreateOrderRequest
import com.example.orders.CreateOrderResponse
import com.example.orders.OrderItemRequest
import com.example.orders.OrderService
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
    fun createOrderEndpointReturnsOk() = testApplication {
        val service = mockk<OrderService>()
        every { service.createOrder(any()) } returns CreateOrderResponse(
            orderId = "order-1",
            status = "created",
            amount = 100,
            createdAt = "2024-01-01T00:00:00Z"
        )

        application {
            configureSerialization()
            configureSecurity()
            configureRouting(service)
        }

        client.post("/orders") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "userId": "user-1",
                  "items": [
                    {"productId": "prod-1", "quantity": 1, "price": 100}
                  ],
                  "address": "Street 1",
                  "amount": 100
                }
                """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        verify(exactly = 1) {
            service.createOrder(
                CreateOrderRequest(
                    userId = "user-1",
                    items = listOf(OrderItemRequest("prod-1", 1, 100)),
                    address = "Street 1",
                    amount = 100
                )
            )
        }
    }
}

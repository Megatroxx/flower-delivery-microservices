package com.example

import com.example.delivery.DeliveryService
import com.example.delivery.StartDeliveryRequest
import com.example.delivery.StartDeliveryResponse
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
    fun startDeliveryEndpointReturnsOk() = testApplication {
        val service = mockk<DeliveryService>()
        every { service.startDelivery(any()) } returns StartDeliveryResponse(
            deliveryId = "delivery-1",
            orderId = "order-1",
            status = "created",
            startedAt = "2024-01-01T00:00:00Z"
        )

        application {
            configureSerialization()
            configureSecurity()
            configureRouting(service)
        }

        val response = client.post("/delivery/start") {
            contentType(ContentType.Application.Json)
            setBody("""{"orderId":"order-1","address":"Street 1"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 1) { service.startDelivery(StartDeliveryRequest("order-1", "Street 1")) }
    }
}

package com.example

import com.example.notification.NotificationResponse
import com.example.notification.NotificationService
import com.example.notification.SendNotificationRequest
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
    fun sendNotificationEndpointReturnsOk() = testApplication {
        val service = mockk<NotificationService>()
        coEvery { service.sendNotification(any()) } returns NotificationResponse(
            notificationId = "notif-1",
            userId = "user-1",
            status = "sent",
            sentAt = "2024-01-01T00:00:00Z"
        )

        application {
            configureSerialization()
            configureSecurity()
            configureRouting(service)
        }

        val response = client.post("/notifications/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId":"user-1","type":"status","message":"Hello"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { service.sendNotification(SendNotificationRequest("user-1", "status", "Hello")) }
    }
}

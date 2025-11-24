package com.example

import com.example.flowers.CatalogRepository
import com.example.flowers.CatalogService
import com.example.flowers.Flower
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
    fun getFlowerReturnsOk() = testApplication {
        val repo = mockk<CatalogRepository>()
        val service = CatalogService(repo)
        every { repo.findById("flower-1") } returns Flower(
            id = "flower-1",
            name = "Rose",
            description = "Lovely",
            price = 120,
            imageUrl = "https://example.com/rose.jpg",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        application {
            configureSerialization()
            configureSecurity()
            configureRouting(repo, service)
        }

        val response = client.get("/catalog/flower-1")

        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 1) { repo.findById("flower-1") }
    }
}

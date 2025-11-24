package com.example.flowers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CatalogServiceTest {

    private lateinit var repo: CatalogRepository
    private lateinit var service: CatalogService

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        service = CatalogService(repo)
    }

    @Test
    fun addFlowerHappyPath() {
        val request = AddFlowerRequest(
            name = "Rose",
            description = "Red",
            price = 100,
            imageUrl = "https://example.com/rose.jpg"
        )
        val flower = Flower(
            id = "flower-1",
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.addFlower(request) } returns flower

        val result = service.addFlower(request)

        assertEquals(
            AddFlowerResponse(
                flowerId = flower.id,
                status = "added",
                addedAt = flower.createdAt
            ),
            result
        )
        verify(exactly = 1) { repo.addFlower(request) }
    }

    @Test
    fun addFlowerFailsForInvalidPayload() {
        val request = AddFlowerRequest(
            name = "",
            description = "desc",
            price = 100,
            imageUrl = "url"
        )

        assertFailsWith<IllegalArgumentException> {
            service.addFlower(request)
        }
        verify(exactly = 0) { repo.addFlower(any()) }
    }

    @Test
    fun updateFlowerHappyPath() {
        val request = UpdateFlowerRequest(
            name = "Tulip",
            description = "desc",
            price = 120,
            imageUrl = "url"
        )
        val flower = Flower(
            id = "flower-2",
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-02T00:00:00Z"
        )

        every { repo.updateFlower("flower-2", request) } returns flower

        val result = service.updateFlower("flower-2", request)

        assertEquals(
            UpdateFlowerResponse(
                flowerId = flower.id,
                status = "updated",
                updatedAt = flower.updatedAt!!
            ),
            result
        )
    }

    @Test
    fun updateFlowerFailsWhenRepoReturnsNull() {
        val request = UpdateFlowerRequest(
            name = "Tulip",
            description = "desc",
            price = 120,
            imageUrl = "url"
        )

        every { repo.updateFlower("missing", request) } returns null

        assertFailsWith<NoSuchElementException> {
            service.updateFlower("missing", request)
        }
    }

    @Test
    fun deleteFlowerHappyPath() {
        val flower = Flower(
            id = "flower-3",
            name = "Lily",
            description = "desc",
            price = 90,
            imageUrl = "url",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.findById(flower.id) } returns flower
        every { repo.deleteFlower(flower.id) } returns true

        val result = service.deleteFlower(flower.id)

        assertEquals("deleted", result.status)
        assertEquals(flower.id, result.flowerId)
        verify(exactly = 1) { repo.deleteFlower(flower.id) }
    }

    @Test
    fun deleteFlowerFailsWhenMissing() {
        every { repo.findById("missing") } returns null

        assertFailsWith<NoSuchElementException> {
            service.deleteFlower("missing")
        }
    }

    @Test
    fun deleteFlowerFailsWhenDeleteReturnsFalse() {
        val flower = Flower(
            id = "flower-4",
            name = "Orchid",
            description = "desc",
            price = 150,
            imageUrl = "url",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.findById(flower.id) } returns flower
        every { repo.deleteFlower(flower.id) } returns false

        assertFailsWith<IllegalStateException> {
            service.deleteFlower(flower.id)
        }
    }
}


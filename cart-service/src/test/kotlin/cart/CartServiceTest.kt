package com.example.cart

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CartServiceTest {

    private lateinit var repo: CartRepository
    private lateinit var service: CartService

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        service = CartService(repo)
    }

    @Test
    fun addItemDelegatesToRepository() {
        val request = AddItemRequest("prod-1", 2)
        val response = CartItemResponse("item-1", request.productId, request.quantity)

        every { repo.addItem("user-1", request.productId, request.quantity) } returns response

        val result = service.addItem("user-1", request)

        assertEquals(response, result)
        verify(exactly = 1) { repo.addItem("user-1", request.productId, request.quantity) }
    }

    @Test
    fun updateQuantitySucceedsWhenRepositoryReturnsTrue() {
        every { repo.updateQuantity("user-1", "prod-1", 3) } returns true

        service.updateQuantity("user-1", "prod-1", UpdateQuantityRequest(3))

        verify(exactly = 1) { repo.updateQuantity("user-1", "prod-1", 3) }
    }

    @Test
    fun updateQuantityFailsWhenRepositoryReturnsFalse() {
        every { repo.updateQuantity(any(), any(), any()) } returns false

        assertFailsWith<NoSuchElementException> {
            service.updateQuantity("user-1", "missing", UpdateQuantityRequest(5))
        }
        verify(exactly = 1) { repo.updateQuantity("user-1", "missing", 5) }
    }

    @Test
    fun deleteItemSucceedsWhenRepositoryReturnsTrue() {
        every { repo.deleteItem("user-1", "prod-1") } returns true

        service.deleteItem("user-1", "prod-1")

        verify(exactly = 1) { repo.deleteItem("user-1", "prod-1") }
    }

    @Test
    fun deleteItemFailsWhenRepositoryReturnsFalse() {
        every { repo.deleteItem(any(), any()) } returns false

        assertFailsWith<NoSuchElementException> {
            service.deleteItem("user-1", "missing")
        }
        verify(exactly = 1) { repo.deleteItem("user-1", "missing") }
    }

    @Test
    fun clearCartAlwaysDelegates() {
        service.clearCart("user-1")

        verify(exactly = 1) { repo.clearCart("user-1") }
    }
}


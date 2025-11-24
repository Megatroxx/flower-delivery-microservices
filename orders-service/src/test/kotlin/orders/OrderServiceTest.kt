package com.example.orders

import com.example.RabbitMQProducer
import com.example.status.OrderStatus
import com.example.status.UpdateStatusResponse
import com.example.delivery.AssignDeliveryResponse
import io.mockk.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderServiceTest {

    private lateinit var repo: OrderRepository
    private lateinit var producer: RabbitMQProducer
    private lateinit var service: OrderService

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        producer = mockk(relaxed = true)
        service = OrderService(repo, producer)
        clearAllMocks()
    }

    @Test
    fun createOrderPublishesEvent() {
        val request = CreateOrderRequest(
            userId = "user-1",
            items = listOf(OrderItemRequest("prod-1", 2, 100)),
            address = "Street 1",
            amount = 200
        )
        val order = Order(
            id = "order-1",
            userId = request.userId,
            address = request.address,
            amount = request.amount,
            status = OrderStatus.CREATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            cancelledAt = null,
            deliveryId = null
        )

        every { repo.createOrder(request.userId, request.items, request.address, request.amount) } returns order
        every { producer.publishOrderCreated(any()) } just Runs

        val response = service.createOrder(request)

        assertEquals(
            CreateOrderResponse(order.id, order.status, order.amount, order.createdAt),
            response
        )
        verify(exactly = 1) { repo.createOrder(request.userId, request.items, request.address, request.amount) }
        verify(exactly = 1) {
            producer.publishOrderCreated(
                match {
                    it.orderId == order.id && it.userId == order.userId && it.amount == order.amount
                }
            )
        }
    }

    @Test
    fun createOrderFailsForEmptyItems() {
        val request = CreateOrderRequest(
            userId = "user-2",
            items = emptyList(),
            address = "Street 2",
            amount = 100
        )

        assertFailsWith<IllegalArgumentException> {
            service.createOrder(request)
        }
        verify(exactly = 0) { repo.createOrder(any(), any(), any(), any()) }
        verify(exactly = 0) { producer.publishOrderCreated(any()) }
    }

    @Test
    fun cancelOrderSucceeds() {
        val orderId = "order-2"
        val requester = "user-3"
        val existing = Order(
            id = orderId,
            userId = requester,
            address = "Street 3",
            amount = 150,
            status = OrderStatus.CREATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            cancelledAt = null,
            deliveryId = null
        )
        val cancelled = existing.copy(
            status = OrderStatus.CANCELLED,
            updatedAt = "2024-01-02T00:00:00Z",
            cancelledAt = "2024-01-02T00:00:00Z"
        )

        every { repo.findOrderById(orderId) } returns existing
        every { repo.cancelOrder(orderId) } returns cancelled

        val result = service.cancelOrder(requester, orderId)

        assertEquals(
            CancelOrderResponse(
                orderId = cancelled.id,
                status = cancelled.status,
                cancelledAt = cancelled.cancelledAt!!,
                message = "Заказ отменён"
            ),
            result
        )
        verify(exactly = 1) { repo.cancelOrder(orderId) }
    }

    @Test
    fun cancelOrderFailsWhenNotFound() {
        every { repo.findOrderById("missing") } returns null

        assertFailsWith<NoSuchElementException> {
            service.cancelOrder("user", "missing")
        }
        verify(exactly = 0) { repo.cancelOrder(any()) }
    }

    @Test
    fun cancelOrderFailsWhenForbidden() {
        val order = Order(
            id = "order-3",
            userId = "owner",
            address = "Street 4",
            amount = 300,
            status = OrderStatus.CREATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            cancelledAt = null,
            deliveryId = null
        )

        every { repo.findOrderById(order.id) } returns order

        assertFailsWith<SecurityException> {
            service.cancelOrder("other", order.id)
        }
        verify(exactly = 0) { repo.cancelOrder(any()) }
    }

    @Test
    fun cancelOrderFailsWhenAlreadyCancelled() {
        val order = Order(
            id = "order-4",
            userId = "owner",
            address = "Street 5",
            amount = 350,
            status = OrderStatus.CANCELLED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            cancelledAt = "2024-01-02T00:00:00Z",
            deliveryId = null
        )

        every { repo.findOrderById(order.id) } returns order

        assertFailsWith<IllegalStateException> {
            service.cancelOrder(order.userId, order.id)
        }
        verify(exactly = 0) { repo.cancelOrder(any()) }
    }

    @Test
    fun cancelOrderFailsWhenRepositoryReturnsNull() {
        val order = Order(
            id = "order-5",
            userId = "owner",
            address = "Street 6",
            amount = 400,
            status = OrderStatus.CREATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            cancelledAt = null,
            deliveryId = null
        )

        every { repo.findOrderById(order.id) } returns order
        every { repo.cancelOrder(order.id) } returns null

        assertFailsWith<IllegalStateException> {
            service.cancelOrder(order.userId, order.id)
        }
    }

    @Test
    fun updateStatusSucceeds() {
        val orderId = "order-6"
        val updated = Order(
            id = orderId,
            userId = "owner",
            address = "Street 7",
            amount = 500,
            status = OrderStatus.PROCESSING,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-02T00:00:00Z",
            cancelledAt = null,
            deliveryId = null
        )

        every { repo.updateStatus(orderId, OrderStatus.PROCESSING) } returns (OrderStatus.CREATED to updated)

        val result = service.updateStatus(orderId, OrderStatus.PROCESSING)

        assertEquals(
            UpdateStatusResponse(
                orderId = updated.id,
                oldStatus = OrderStatus.CREATED,
                newStatus = updated.status,
                updatedAt = updated.updatedAt!!
            ),
            result
        )
    }

    @Test
    fun updateStatusFailsForInvalidStatus() {
        assertFailsWith<IllegalArgumentException> {
            service.updateStatus("order", "invalid")
        }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun updateStatusFailsWhenOrderMissing() {
        every { repo.updateStatus("order", OrderStatus.PROCESSING) } returns null

        assertFailsWith<NoSuchElementException> {
            service.updateStatus("order", OrderStatus.PROCESSING)
        }
    }

    @Test
    fun assignDeliverySucceeds() {
        val orderId = "order-7"
        val deliveryId = "delivery-1"
        val updated = Order(
            id = orderId,
            userId = "owner",
            address = "Street 8",
            amount = 600,
            status = OrderStatus.DELIVERY_ASSIGNED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-03T00:00:00Z",
            cancelledAt = null,
            deliveryId = deliveryId
        )

        every { repo.assignDelivery(orderId, deliveryId) } returns updated

        val result = service.assignDelivery(orderId, deliveryId)

        assertEquals(
            AssignDeliveryResponse(
                orderId = updated.id,
                deliveryId = deliveryId,
                status = updated.status,
                assignedAt = updated.updatedAt!!
            ),
            result
        )
    }

    @Test
    fun assignDeliveryFailsWhenOrderMissing() {
        every { repo.assignDelivery("order", "delivery") } returns null

        assertFailsWith<NoSuchElementException> {
            service.assignDelivery("order", "delivery")
        }
    }
}


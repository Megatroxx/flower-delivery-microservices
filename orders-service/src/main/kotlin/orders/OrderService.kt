package com.example.orders

import com.example.OrderCreatedEvent
import com.example.RabbitMQProducer
import com.example.delivery.AssignDeliveryResponse
import com.example.status.OrderStatus
import com.example.status.UpdateStatusResponse
import java.time.Instant

class OrderService(
    private val repo: OrderRepository,
    private val producer: RabbitMQProducer
) {

    fun createOrder(req: CreateOrderRequest): CreateOrderResponse {
        if (req.items.isEmpty())
            throw IllegalArgumentException("Order must have items")

        val order = repo.createOrder(req.userId, req.items, req.address, req.amount)

        producer.publishOrderCreated(
            OrderCreatedEvent(
                orderId = order.id,
                userId = order.userId,
                address = order.address,
                amount = order.amount
            )
        )

        return CreateOrderResponse(
            orderId = order.id,
            status = order.status,
            amount = order.amount,
            createdAt = order.createdAt
        )
    }

    fun cancelOrder(requesterUserId: String, orderId: String): CancelOrderResponse {
        val order = repo.findOrderById(orderId)
            ?: throw NoSuchElementException("Order not found")

        if (order.userId != requesterUserId)
            throw SecurityException("Forbidden")

        if (order.status == OrderStatus.CANCELLED)
            throw IllegalStateException("Already cancelled")

        val cancelled = repo.cancelOrder(orderId)
            ?: throw IllegalStateException("Failed to cancel")

        return CancelOrderResponse(
            orderId = cancelled.id,
            status = cancelled.status,
            cancelledAt = cancelled.cancelledAt ?: Instant.now().toString(),
            message = "Заказ отменён"
        )
    }

    fun updateStatus(orderId: String, newStatus: String): UpdateStatusResponse {
        if (newStatus !in OrderStatus.all)
            throw IllegalArgumentException("Invalid status")

        val result = repo.updateStatus(orderId, newStatus)
            ?: throw NoSuchElementException("Order not found")

        val (old, updated) = result

        return UpdateStatusResponse(
            orderId = updated.id,
            oldStatus = old,
            newStatus = updated.status,
            updatedAt = updated.updatedAt ?: Instant.now().toString()
        )
    }

    fun assignDelivery(orderId: String, deliveryId: String): AssignDeliveryResponse {
        val updated = repo.assignDelivery(orderId, deliveryId)
            ?: throw NoSuchElementException("Order not found")

        return AssignDeliveryResponse(
            orderId = updated.id,
            deliveryId = deliveryId,
            status = updated.status,
            assignedAt = updated.updatedAt ?: Instant.now().toString()
        )
    }
}
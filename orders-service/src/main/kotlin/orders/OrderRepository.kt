package com.example.orders

import com.example.order_items.OrderItemsTable
import com.example.orders.OrdersTable
import com.example.status.OrderStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID


class OrderRepository {

    fun createOrder(
        userId: String,
        items: List<OrderItemRequest>,
        address: String,
        amount: Int
    ): Order = transaction {

        val orderId = UUID.randomUUID().toString()
        val createdAt = Instant.now().toString()

        // Создать заказ
        OrdersTable.insert {
            it[id] = orderId
            it[OrdersTable.userId] = userId
            it[OrdersTable.address] = address
            it[OrdersTable.amount] = amount
            it[status] = OrderStatus.CREATED
            it[OrdersTable.createdAt] = createdAt
            it[updatedAt] = null
            it[cancelledAt] = null
            it[deliveryId] = null
        }

        // Добавить позиции
        items.forEach { item ->
            OrderItemsTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[OrderItemsTable.orderId] = orderId
                it[productId] = item.productId
                it[quantity] = item.quantity
                it[price] = item.price
            }
        }

        // Вернуть объект
        Order(
            id = orderId,
            userId = userId,
            address = address,
            amount = amount,
            status = OrderStatus.CREATED,
            createdAt = createdAt,
            updatedAt = null,
            cancelledAt = null,
            deliveryId = null
        )
    }

    fun findOrderById(id: String): Order? = transaction {
        OrdersTable
            .selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull()
            ?.let {
                Order(
                    id = it[OrdersTable.id],
                    userId = it[OrdersTable.userId],
                    address = it[OrdersTable.address],
                    amount = it[OrdersTable.amount],
                    status = it[OrdersTable.status],
                    createdAt = it[OrdersTable.createdAt],
                    updatedAt = it[OrdersTable.updatedAt],
                    cancelledAt = it[OrdersTable.cancelledAt],
                    deliveryId = it[OrdersTable.deliveryId]
                )
            }
    }

    fun updateStatus(orderId: String, newStatus: String): Pair<String, Order>? = transaction {

        // Найти старый заказ
        val oldRow = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq orderId }
            .singleOrNull() ?: return@transaction null

        val oldStatus = oldRow[OrdersTable.status]
        val now = Instant.now().toString()

        // Обновить статус
        OrdersTable.update(
            where = { OrdersTable.id eq orderId }
        ) {
            it[status] = newStatus
            it[updatedAt] = now
        }

        // Получить обновлённый заказ
        val updated = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq orderId }
            .single()

        val updatedOrder = Order(
            id = updated[OrdersTable.id],
            userId = updated[OrdersTable.userId],
            address = updated[OrdersTable.address],
            amount = updated[OrdersTable.amount],
            status = updated[OrdersTable.status],
            createdAt = updated[OrdersTable.createdAt],
            updatedAt = updated[OrdersTable.updatedAt],
            cancelledAt = updated[OrdersTable.cancelledAt],
            deliveryId = updated[OrdersTable.deliveryId]
        )

        oldStatus to updatedOrder
    }

    fun cancelOrder(id: String): Order? = transaction {

        val oldRow = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction null

        val now = Instant.now().toString()

        OrdersTable.update(
            where = { OrdersTable.id eq id }
        ) {
            it[status] = OrderStatus.CANCELLED
            it[cancelledAt] = now
            it[updatedAt] = now
        }

        val updated = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq id }
            .single()

        Order(
            id = updated[OrdersTable.id],
            userId = updated[OrdersTable.userId],
            address = updated[OrdersTable.address],
            amount = updated[OrdersTable.amount],
            status = updated[OrdersTable.status],
            createdAt = updated[OrdersTable.createdAt],
            updatedAt = updated[OrdersTable.updatedAt],
            cancelledAt = updated[OrdersTable.cancelledAt],
            deliveryId = updated[OrdersTable.deliveryId]
        )
    }

    fun assignDelivery(id: String, deliveryId: String): Order? = transaction {

        val oldRow = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction null

        val now = Instant.now().toString()

        OrdersTable.update(
            where = { OrdersTable.id eq id }
        ) {
            it[OrdersTable.deliveryId] = deliveryId
            it[status] = OrderStatus.DELIVERY_ASSIGNED
            it[updatedAt] = now
        }

        val updated = OrdersTable
            .selectAll()
            .where { OrdersTable.id eq id }
            .single()

        Order(
            id = updated[OrdersTable.id],
            userId = updated[OrdersTable.userId],
            address = updated[OrdersTable.address],
            amount = updated[OrdersTable.amount],
            status = updated[OrdersTable.status],
            createdAt = updated[OrdersTable.createdAt],
            updatedAt = updated[OrdersTable.updatedAt],
            cancelledAt = updated[OrdersTable.cancelledAt],
            deliveryId = updated[OrdersTable.deliveryId]
        )
    }
}
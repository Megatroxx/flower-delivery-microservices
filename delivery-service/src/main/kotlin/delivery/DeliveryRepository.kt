package com.example.delivery

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class DeliveryRepository {

    fun startDelivery(orderId: String, address: String): Delivery = transaction {
        val deliveryId = UUID.randomUUID().toString()
        val startedAt = Instant.now().toString()

        DeliveriesTable.insert {
            it[id] = deliveryId
            it[DeliveriesTable.orderId] = orderId
            it[DeliveriesTable.address] = address
            it[courierId] = null
            it[status] = DeliveryStatus.STARTED
            it[DeliveriesTable.startedAt] = startedAt
            it[updatedAt] = null
            it[deliveredAt] = null
            it[recipientSignature] = null
        }

        Delivery(
            id = deliveryId,
            orderId = orderId,
            address = address,
            courierId = null,
            status = DeliveryStatus.STARTED,
            startedAt = startedAt,
            updatedAt = null,
            deliveredAt = null,
            recipientSignature = null
        )
    }

    fun findById(id: String): Delivery? = transaction {
        DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq id }
            .singleOrNull()
            ?.toDelivery()
    }

    fun assignCourier(deliveryId: String, courier: String): Delivery? = transaction {
        val existing = DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .singleOrNull() ?: return@transaction null

        val now = Instant.now().toString()

        DeliveriesTable.update({ DeliveriesTable.id eq deliveryId }) {
            it[courierId] = courier
            it[status] = DeliveryStatus.COURIER_ASSIGNED
            it[updatedAt] = now
        }

        DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .single()
            .toDelivery()
    }

    fun updateStatus(deliveryId: String, newStatus: String): Pair<String, Delivery>? = transaction {
        val oldRow = DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .singleOrNull() ?: return@transaction null

        val oldStatus = oldRow[DeliveriesTable.status]
        val now = Instant.now().toString()

        DeliveriesTable.update({ DeliveriesTable.id eq deliveryId }) {
            it[status] = newStatus
            it[updatedAt] = now
        }

        val updated = DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .single()
            .toDelivery()

        oldStatus to updated
    }

    fun complete(
        deliveryId: String,
        deliveredAtValue: String,
        signature: String
    ): Delivery? = transaction {
        val existing = DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .singleOrNull() ?: return@transaction null

        val now = Instant.now().toString()

        DeliveriesTable.update({ DeliveriesTable.id eq deliveryId }) {
            it[status] = DeliveryStatus.DELIVERED
            it[deliveredAt] = deliveredAtValue
            it[updatedAt] = now
            it[recipientSignature] = signature
        }

        DeliveriesTable
            .selectAll()
            .where { DeliveriesTable.id eq deliveryId }
            .single()
            .toDelivery()
    }

    private fun ResultRow.toDelivery() = Delivery(
        id = this[DeliveriesTable.id],
        orderId = this[DeliveriesTable.orderId],
        address = this[DeliveriesTable.address],
        courierId = this[DeliveriesTable.courierId],
        status = this[DeliveriesTable.status],
        startedAt = this[DeliveriesTable.startedAt],
        updatedAt = this[DeliveriesTable.updatedAt],
        deliveredAt = this[DeliveriesTable.deliveredAt],
        recipientSignature = this[DeliveriesTable.recipientSignature]
    )
}
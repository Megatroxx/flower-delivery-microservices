package com.example.delivery

import java.time.Instant

class DeliveryService(
    private val repo: DeliveryRepository
) {

    fun startDelivery(req: StartDeliveryRequest): StartDeliveryResponse {
        val delivery = repo.startDelivery(req.orderId, req.address)

        return StartDeliveryResponse(
            deliveryId = delivery.id,
            orderId = delivery.orderId,
            status = delivery.status,
            startedAt = delivery.startedAt
        )
    }

    fun assignCourier(deliveryId: String, courierId: String): AssignCourierResponse {
        val updated = repo.assignCourier(deliveryId, courierId)
            ?: throw NoSuchElementException("Delivery not found")

        return AssignCourierResponse(
            deliveryId = updated.id,
            courierId = updated.courierId ?: courierId,
            status = updated.status,
            assignedAt = updated.updatedAt ?: Instant.now().toString()
        )
    }

    fun updateStatus(deliveryId: String, newStatus: String): UpdateDeliveryStatusResponse {
        if (newStatus !in DeliveryStatus.all) {
            throw IllegalArgumentException("Invalid status")
        }

        val (old, updated) = repo.updateStatus(deliveryId, newStatus)
            ?: throw NoSuchElementException("Delivery not found")

        return UpdateDeliveryStatusResponse(
            deliveryId = updated.id,
            oldStatus = old,
            newStatus = updated.status,
            updatedAt = updated.updatedAt ?: Instant.now().toString()
        )
    }

    fun completeDelivery(deliveryId: String, req: CompleteDeliveryRequest): CompleteDeliveryResponse {
        val updated = repo.complete(deliveryId, req.deliveredAt, req.recipientSignature)
            ?: throw NoSuchElementException("Delivery not found")

        return CompleteDeliveryResponse(
            deliveryId = updated.id,
            status = updated.status,
            deliveredAt = updated.deliveredAt ?: req.deliveredAt,
            message = "Заказ доставлен"
        )
    }
}
package com.example.delivery

import kotlinx.serialization.Serializable

@Serializable
data class StartDeliveryRequest(
    val orderId: String,
    val address: String
)

@Serializable
data class StartDeliveryResponse(
    val deliveryId: String,
    val orderId: String,
    val status: String,
    val startedAt: String
)

@Serializable
data class AssignCourierRequest(
    val courierId: String
)

@Serializable
data class AssignCourierResponse(
    val deliveryId: String,
    val courierId: String,
    val status: String,
    val assignedAt: String
)

@Serializable
data class UpdateDeliveryStatusRequest(
    val status: String
)

@Serializable
data class UpdateDeliveryStatusResponse(
    val deliveryId: String,
    val oldStatus: String,
    val newStatus: String,
    val updatedAt: String
)

@Serializable
data class CompleteDeliveryRequest(
    val deliveredAt: String,
    val recipientSignature: String
)

@Serializable
data class CompleteDeliveryResponse(
    val deliveryId: String,
    val status: String,
    val deliveredAt: String,
    val message: String
)
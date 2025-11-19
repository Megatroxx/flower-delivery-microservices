package com.example.delivery

import kotlinx.serialization.Serializable

@Serializable
data class AssignDeliveryResponse(
    val orderId: String,
    val deliveryId: String,
    val status: String,
    val assignedAt: String
)
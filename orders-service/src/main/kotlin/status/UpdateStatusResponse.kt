package com.example.status

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatusResponse(
    val orderId: String,
    val oldStatus: String,
    val newStatus: String,
    val updatedAt: String
)
package com.example.orders

import kotlinx.serialization.Serializable

@Serializable
data class CancelOrderResponse(
    val orderId: String,
    val status: String,
    val cancelledAt: String,
    val message: String
)
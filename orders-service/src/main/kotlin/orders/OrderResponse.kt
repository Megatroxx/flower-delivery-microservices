package com.example.orders

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderResponse(
    val orderId: String,
    val status: String,
    val amount: Int,
    val createdAt: String
)
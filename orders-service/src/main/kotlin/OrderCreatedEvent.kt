package com.example

import kotlinx.serialization.Serializable

@Serializable
data class OrderCreatedEvent(
    val orderId: String,
    val userId: String,
    val address: String,
    val amount: Int
)
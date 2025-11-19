package com.example.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val productId: String,
    val quantity: Int,
    val price: Int
)

@Serializable
data class CreateOrderRequest(
    val userId: String,
    val items: List<OrderItemRequest>,
    val address: String,
    val amount: Int
)
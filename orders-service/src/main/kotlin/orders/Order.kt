package com.example.orders

data class Order(
    val id: String,
    val userId: String,
    val address: String,
    val amount: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String?,
    val cancelledAt: String?,
    val deliveryId: String?
)
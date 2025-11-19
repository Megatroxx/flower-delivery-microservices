package com.example.delivery

data class Delivery(
    val id: String,
    val orderId: String,
    val address: String,
    val courierId: String?,
    val status: String,
    val startedAt: String,
    val updatedAt: String?,
    val deliveredAt: String?,
    val recipientSignature: String?
)
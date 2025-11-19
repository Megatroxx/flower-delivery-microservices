package com.example.payment

data class Payment(
    val id: String,
    val orderId: String,
    val userId: String,
    val amount: Int,
    val paymentMethod: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String?
)
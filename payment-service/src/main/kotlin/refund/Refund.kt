package com.example.refund

data class Refund(
    val id: String,
    val orderId: String,
    val amount: Int,
    val status: String,
    val processedAt: String
)
package com.example.refund

import kotlinx.serialization.Serializable

@Serializable
data class RefundResponse(
    val refundId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
    val processedAt: String
)
package com.example.refund

import kotlinx.serialization.Serializable

@Serializable
data class RefundRequest(
    val orderId: String,
    val amount: Int
)
package com.example.payment

import kotlinx.serialization.Serializable

@Serializable
data class InitiatePaymentRequest(
    val orderId: String,
    val amount: Int,
    val paymentMethod: String
)
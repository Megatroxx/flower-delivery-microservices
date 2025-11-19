package com.example.payment

import kotlinx.serialization.Serializable

@Serializable
data class InitiatePaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: String,
    val redirectUrl: String
)
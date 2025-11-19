package com.example.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentSuccessResponse(
    val paymentId: String,
    val status: String,
    val confirmedAt: String,
    val message: String
)
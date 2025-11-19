package com.example.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentFailedResponse(
    val paymentId: String,
    val status: String,
    val failedAt: String,
    val reason: String
)
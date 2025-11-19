package com.example.payment

object PaymentStatus {
    const val INITIATED = "initiated"
    const val SUCCESS = "success"
    const val FAILED = "failed"
    const val REFUNDED = "refunded"

    val all = setOf(INITIATED, SUCCESS, FAILED, REFUNDED)
}
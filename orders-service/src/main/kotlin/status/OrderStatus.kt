package com.example.status

object OrderStatus {
    const val CREATED = "created"
    const val PROCESSING = "processing"
    const val CANCELLED = "cancelled"
    const val DELIVERY_ASSIGNED = "delivery_assigned"

    val all = setOf(CREATED, PROCESSING, CANCELLED, DELIVERY_ASSIGNED)
}
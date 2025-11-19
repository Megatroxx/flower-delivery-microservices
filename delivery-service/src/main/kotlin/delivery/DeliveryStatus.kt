package com.example.delivery

object DeliveryStatus {
    const val STARTED = "started"
    const val COURIER_ASSIGNED = "courier_assigned"
    const val IN_TRANSIT = "in_transit"
    const val DELIVERED = "delivered"

    val all = setOf(STARTED, COURIER_ASSIGNED, IN_TRANSIT, DELIVERED)
}
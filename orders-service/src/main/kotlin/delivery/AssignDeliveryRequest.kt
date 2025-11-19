package com.example.delivery

import kotlinx.serialization.Serializable

@Serializable
data class AssignDeliveryRequest(val deliveryId: String)
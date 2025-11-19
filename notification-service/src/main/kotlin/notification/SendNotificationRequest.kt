package com.example.notification

import kotlinx.serialization.Serializable

@Serializable
data class SendNotificationRequest(
    val userId: String,
    val type: String,
    val message: String
)
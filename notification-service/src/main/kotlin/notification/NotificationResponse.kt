package com.example.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val notificationId: String,
    val userId: String,
    val status: String,
    val sentAt: String
)
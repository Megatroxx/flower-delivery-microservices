package com.example.notification

data class Notification(
    val id: String,
    val userId: String,
    val type: String,
    val message: String,
    val status: String,
    val sentAt: String
)
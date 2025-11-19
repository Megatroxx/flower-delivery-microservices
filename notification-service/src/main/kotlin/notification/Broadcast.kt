package com.example.notification

data class Broadcast(
    val id: String,
    val segment: String,
    val message: String,
    val targetCount: Int,
    val status: String,
    val scheduledAt: String
)
package com.example.notification

import kotlinx.serialization.Serializable

@Serializable
data class BroadcastResponse(
    val broadcastId: String,
    val targetCount: Int,
    val status: String,
    val scheduledAt: String
)
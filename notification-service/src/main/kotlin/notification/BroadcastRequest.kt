package com.example.notification

import kotlinx.serialization.Serializable

@Serializable
data class BroadcastRequest(
    val segment: String,
    val message: String
)
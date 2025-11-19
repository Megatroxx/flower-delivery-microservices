package com.example.flowers

import kotlinx.serialization.Serializable

@Serializable
data class AddFlowerResponse(
    val flowerId: String,
    val status: String,
    val addedAt: String
)
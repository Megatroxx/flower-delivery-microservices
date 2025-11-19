package com.example.flowers

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFlowerResponse(
    val flowerId: String,
    val status: String,
    val updatedAt: String
)
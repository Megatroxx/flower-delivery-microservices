package com.example.flowers

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFlowerRequest(
    val name: String,
    val description: String,
    val price: Int,
    val imageUrl: String
)
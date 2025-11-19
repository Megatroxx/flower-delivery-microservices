package com.example.flowers

import kotlinx.serialization.Serializable


@Serializable
data class Flower(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val imageUrl: String,
    val createdAt: String,
    val updatedAt: String?
)
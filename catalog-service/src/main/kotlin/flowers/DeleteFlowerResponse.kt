package com.example.flowers

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFlowerResponse(
    val flowerId: String,
    val status: String,
    val deletedAt: String
)
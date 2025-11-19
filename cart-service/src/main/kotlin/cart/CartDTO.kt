package com.example.cart

import kotlinx.serialization.Serializable

@Serializable
data class AddItemRequest(
    val productId: String,
    val quantity: Int
)

@Serializable
data class UpdateQuantityRequest(
    val quantity: Int
)

@Serializable
data class CartItemResponse(
    val id: String,
    val productId: String,
    val quantity: Int
)
package com.example.users

import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleResponse(
    val userId: String,
    val newRole: String,
    val updatedAt: String
)
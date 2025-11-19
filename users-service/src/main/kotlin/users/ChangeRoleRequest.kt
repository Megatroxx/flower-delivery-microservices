package com.example.users

import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleRequest(
    val role: String
)
package com.example.users

import kotlinx.serialization.Serializable

@Serializable
data class UserListItem(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)
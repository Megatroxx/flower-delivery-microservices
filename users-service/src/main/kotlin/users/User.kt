package com.example.users

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val createdAt: String
)
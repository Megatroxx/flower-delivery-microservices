package com.example.status

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatusRequest(val status: String)
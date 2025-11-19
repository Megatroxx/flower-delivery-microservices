package com.example.users

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class UsersClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getAllUsers(): List<UserDto> {
        return client.get("http://0.0.0.0:8080/users/list").body()
    }

    suspend fun findUser(userId: String): UserDto? {
        return getAllUsers().find { it.id == userId }
    }
}
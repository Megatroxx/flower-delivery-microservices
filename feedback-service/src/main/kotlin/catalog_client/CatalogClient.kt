package com.example.catalog_client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FlowerDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Int? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

class CatalogClient {

    private val client = HttpClient(CIO) {

        install(Logging) {
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun flowerExists(id: String): Boolean {
        return try {
            println("🔵 Sending request to Catalog: http://0.0.0.0:8085/catalog/$id")

            val response = client.get("http://0.0.0.0:8085/catalog/$id")

            println("🟣 Catalog response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val dto: FlowerDto = response.body()
                println("🟢 Parsed FlowerDto: $dto")
                true
            } else {
                println("🟡 Catalog responded but not OK: ${response.status}")
                false
            }

        } catch (e: Exception) {
            println("🔴 ERROR while calling Catalog: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*

fun Application.configureSecurity() {

    val jwtSecret = "super_secret_jwt_key_123456789"
    val jwtIssuer = "http://0.0.0.0:8080/"
    val jwtAudience = "users"
    val jwtRealm = "feedback-service"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("id").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}

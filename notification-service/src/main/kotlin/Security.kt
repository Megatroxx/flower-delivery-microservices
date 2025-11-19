package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
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

    val secret = "super_secret_jwt_key_123456789"
    val issuer = "http://0.0.0.0:8080/"
    val audience = "users"

    authentication {
        jwt("auth-jwt-admin") {
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )

            validate { credential ->
                val role = credential.payload.getClaim("role").asString()
                if (role == "admin") JWTPrincipal(credential.payload) else null
            }
        }
    }
}

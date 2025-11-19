package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {

    val jwtAudience = "users"
    val jwtIssuer = "http://0.0.0.0:8080/"
    val jwtRealm = "users-service"
    val jwtSecret = "super_secret_jwt_key_123456789"

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}

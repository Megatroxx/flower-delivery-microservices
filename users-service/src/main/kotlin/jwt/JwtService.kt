package com.example.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.users.User
import java.util.*

object JwtService {

    private const val secret = "super_secret_jwt_key_123456789"
    private const val issuer = "http://0.0.0.0:8080/"
    private const val audience = "users"
    private const val validityInMs = 3600 * 1000 // 1 час

    fun createAccessToken(user: User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("id", user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(Algorithm.HMAC256(secret))
    }
}
package com.example.users

import com.example.jwt.JwtService
import com.example.utils.PasswordHasher
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class UserService(
    private val repo: UserRepository
) {

    fun register(req: RegisterRequest): UserResponse {
        // Проверка на существование email
        val existing = repo.findByEmail(req.email)
        if (existing != null) {
            throw IllegalStateException("User already exists")
        }

        val hashedPassword = PasswordHasher.hash(req.password)
        val user = repo.createUser(req.email, hashedPassword, req.name)

        return UserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            createdAt = user.createdAt
        )
    }

    fun login(req: LoginRequest): TokenResponse {
        val result = repo.findByEmail(req.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        val (user, passwordHash) = result

        if (!PasswordHasher.verify(req.password, passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val token = JwtService.createAccessToken(user)

        return TokenResponse(
            accessToken = token,
            expiresIn = 3600
        )
    }

    fun changeRole(requester: User, userId: String, newRole: String): User {
        // 1. проверка роли вызывающего
        if (requester.role != "admin") {
            throw SecurityException("Not enough permissions")
        }

        // 2. проверяем валидность роли
        val validRoles = setOf("user", "courier", "admin")

        if (newRole !in validRoles) {
            throw IllegalArgumentException("Invalid role")
        }

        // 3. ищем пользователя
        val user = repo.findById(userId)
            ?: throw NoSuchElementException("User not found")

        // 4. устанавливаем новую роль
        val success = repo.setRole(userId, newRole)
        if (!success) throw IllegalStateException("Failed to update role")

        // 5. возвращаем обновленного пользователя
        return user.copy(role = newRole)
    }
}
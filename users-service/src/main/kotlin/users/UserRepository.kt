package com.example.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class UserRepository {

    fun createUser(email: String, passwordHash: String, name: String): User {
        val id = UUID.randomUUID().toString()
        val createdAt = Instant.now().toString()

        transaction {
            UserTable.insert {
                it[UserTable.id] = id
                it[UserTable.email] = email
                it[UserTable.password] = passwordHash
                it[UserTable.name] = name
                it[UserTable.role] = "user"
                it[UserTable.createdAt] = createdAt
            }
        }

        return User(id, email, name, "user", createdAt)
    }

    fun findByEmail(email: String): Pair<User, String>? =
        transaction {
            UserTable.select { UserTable.email eq email }
                .singleOrNull()
                ?.let {
                    val user = User(
                        it[UserTable.id],
                        it[UserTable.email],
                        it[UserTable.name],
                        it[UserTable.role],
                        it[UserTable.createdAt]
                    )
                    user to it[UserTable.password]
                }
        }

    fun findById(id: String): User? =
        transaction {
            UserTable.select { UserTable.id eq id }
                .singleOrNull()
                ?.let {
                    User(
                        it[UserTable.id],
                        it[UserTable.email],
                        it[UserTable.name],
                        it[UserTable.role],
                        it[UserTable.createdAt]
                    )
                }
        }

    fun setRole(id: String, role: String): Boolean =
        transaction {
            UserTable.update({ UserTable.id eq id }) {
                it[UserTable.role] = role
            } > 0
        }
}
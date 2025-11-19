package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.users.*
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun Application.configureRouting() {

    val userRepository = UserRepository()
    val userService = UserService(userRepository)

    routing {

        post("/users/register") {
            val req = call.receive<RegisterRequest>()

            try {
                val user = userService.register(req)
                call.respond(user)
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }

        post("/users/login") {
            val req = call.receive<LoginRequest>()

            try {
                val token = userService.login(req)
                call.respond(token)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            }
        }

        get("/users/list") {
            val users = transaction {
                UserTable.selectAll().map {
                    UserListItem(
                        id = it[UserTable.id],
                        email = it[UserTable.email],
                        name = it[UserTable.name],
                        role = it[UserTable.role]
                    )
                }
            }
            call.respond(users)
        }

        authenticate("auth-jwt") {
            post("/users/{userId}/role") {
                val userId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

                val req = call.receive<ChangeRoleRequest>()

                // достаём из токена данные о пользователе, который делает запрос
                val principal = call.principal<JWTPrincipal>()!!
                val requester = User(
                    id = principal.payload.getClaim("id").asString(),
                    email = principal.payload.getClaim("email").asString(),
                    name = "unknown", // имя нам не нужно для проверки
                    role = principal.payload.getClaim("role").asString(),
                    createdAt = ""
                )

                try {
                    val updated = userService.changeRole(requester, userId, req.role)

                    call.respond(
                        ChangeRoleResponse(
                            userId = updated.id,
                            newRole = updated.role,
                            updatedAt = Instant.now().toString()
                        )
                    )
                }
                catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
                catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                }
                catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }
        }
    }
}

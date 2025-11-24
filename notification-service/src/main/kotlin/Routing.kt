package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.notification.*
import com.example.users.UsersClient
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

fun Application.configureRouting(
    notificationService: NotificationService = NotificationService(UsersClient())
) {

    val service = notificationService

    routing {

        post("/notifications/send") {
            val req = call.receive<SendNotificationRequest>()
            try {
                val result = service.sendNotification(req)
                call.respond(result)
            } catch (e: NoSuchElementException) {
                call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        authenticate("auth-jwt-admin") {
            post("/notifications/broadcast") {
                val req = call.receive<BroadcastRequest>()
                val result = service.broadcast(req)
                call.respond(result)
            }
        }
    }
}
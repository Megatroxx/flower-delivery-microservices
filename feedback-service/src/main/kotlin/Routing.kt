package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.catalog_client.CatalogClient
import com.example.feedback.FeedbackRepository
import com.example.feedback.FeedbackService
import com.example.feedback.RatingRequest
import com.example.feedback.ReviewRequest
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

fun Application.configureRouting() {

    val repo = FeedbackRepository()
    val catalogClient = CatalogClient()
    val service = FeedbackService(repo, catalogClient)

    routing {

        authenticate("auth-jwt") {

            post("/feedback/rating") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val req = call.receive<RatingRequest>()

                try {
                    val res = service.rate(userId, req)
                    call.respond(res)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()        // <-- добавь
                    println("ERROR OCCURRED: ${e.message}")   // <-- добавь
                    call.respondText(e.message ?: "Invalid data", status = io.ktor.http.HttpStatusCode.BadRequest)
                }
            }

            post("/feedback/review") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val req = call.receive<ReviewRequest>()

                try {
                    val res = service.review(userId, req)
                    call.respond(res)
                } catch (e: IllegalArgumentException) {
                    call.respondText(e.message ?: "Invalid data", status = io.ktor.http.HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
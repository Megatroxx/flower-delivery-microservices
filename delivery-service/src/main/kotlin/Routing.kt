package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.delivery.*
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

fun Application.configureRouting(
    deliveryService: DeliveryService = DeliveryService(DeliveryRepository())
) {

    val service = deliveryService

    routing {

        post("/delivery/start") {
            val req = call.receive<StartDeliveryRequest>()

            try {
                val resp = service.startDelivery(req)
                call.respond(resp)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "unknown error")))
            }
        }

        post("/delivery/{deliveryId}/assign-courier") {
            val deliveryId = call.parameters["deliveryId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val req = call.receive<AssignCourierRequest>()

            try {
                val resp = service.assignCourier(deliveryId, req.courierId)
                call.respond(resp)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "unknown error")))
            }
        }

        authenticate("auth-jwt") {

            patch("/delivery/{deliveryId}/status") {
                val deliveryId = call.parameters["deliveryId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)

                val req = call.receive<UpdateDeliveryStatusRequest>()

                try {
                    val resp = service.updateStatus(deliveryId, req.status)
                    call.respond(resp)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "unknown error")))
                }
            }

            post("/delivery/{deliveryId}/complete") {
                val deliveryId = call.parameters["deliveryId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val req = call.receive<CompleteDeliveryRequest>()

                try {
                    val resp = service.completeDelivery(deliveryId, req)
                    call.respond(resp)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "unknown error")))
                }
            }
        }
    }
}
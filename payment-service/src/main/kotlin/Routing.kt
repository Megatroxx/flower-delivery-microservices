package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.payment.InitiatePaymentRequest
import com.example.payment.PaymentRepository
import com.example.payment.PaymentService
import com.example.refund.RefundRequest
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

    val repo = PaymentRepository()
    val service = PaymentService(repo)

    routing {

        authenticate("auth-jwt") {
            post("/payments/initiate") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val req = call.receive<InitiatePaymentRequest>()

                try {
                    val resp = service.initiatePayment(userId, req)
                    call.respond(HttpStatusCode.OK, resp)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }
        }

        post("/payments/{paymentId}/success") {
            val paymentId = call.parameters["paymentId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing paymentId"))

            try {
                val resp = service.markSuccess(paymentId)
                call.respond(HttpStatusCode.OK, resp)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Payment not found")))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }

        post("/payments/{paymentId}/failed") {
            val paymentId = call.parameters["paymentId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing paymentId"))

            val reason = "card_declined"

            try {
                val resp = service.markFailed(paymentId, reason)
                call.respond(HttpStatusCode.OK, resp)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Payment not found")))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }

        post("/payments/refund") {
            val req = call.receive<RefundRequest>()

            try {
                val resp = service.refund(req)
                call.respond(HttpStatusCode.OK, resp)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }
    }
}

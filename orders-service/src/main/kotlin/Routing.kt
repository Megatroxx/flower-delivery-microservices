package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.delivery.AssignDeliveryRequest
import com.example.orders.CreateOrderRequest
import com.example.orders.OrderRepository
import com.example.orders.OrderService
import com.example.status.UpdateStatusRequest
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
    orderService: OrderService = OrderService(
        OrderRepository(),
        producer = RabbitMQProducer()
    )
) {

    val service = orderService

    routing {

        // 3.1 — Система — создать заказ
        post("/orders") {
            val req = call.receive<CreateOrderRequest>()

            try {
                val resp = service.createOrder(req)
                call.respond(resp)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        authenticate("auth-jwt") {
            // 3.2 — Пользователь — отмена заказа
            post("/orders/{orderId}/cancel") {
                val orderId = call.parameters["orderId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("id").asString()

                try {
                    val resp = service.cancelOrder(userId, orderId)
                    call.respond(resp)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                }
            }
        }

        // 3.3 — Система — обновить статус
        patch("/orders/{orderId}/status") {
            val orderId = call.parameters["orderId"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest)

            val req = call.receive<UpdateStatusRequest>()

            try {
                val resp = service.updateStatus(orderId, req.status)
                call.respond(resp)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // 3.4 — Система — назначить доставку
        post("/orders/{orderId}/assign-delivery") {
            val orderId = call.parameters["orderId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val req = call.receive<AssignDeliveryRequest>()

            try {
                val resp = service.assignDelivery(orderId, req.deliveryId)
                call.respond(resp)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}

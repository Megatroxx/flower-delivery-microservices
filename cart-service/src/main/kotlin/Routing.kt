package com.example

import com.example.cart.AddItemRequest
import com.example.cart.CartRepository
import com.example.cart.CartService
import com.example.cart.UpdateQuantityRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    cartService: CartService = CartService(CartRepository())
) {

    val service = cartService

    routing {                     // ← добавляем это!

        authenticate("auth-jwt") {

            // Добавить товар в корзину
            post("/cart/items") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val req = call.receive<AddItemRequest>()
                val item = service.addItem(userId, req)
                call.respond(item)
            }

            // Изменить количество товара
            patch("/cart/items/{productId}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val productId = call.parameters["productId"]!!
                val req = call.receive<UpdateQuantityRequest>()

                try {
                    service.updateQuantity(userId, productId, req)
                    call.respond(HttpStatusCode.OK)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Удалить товар из корзины
            delete("/cart/items/{productId}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                val productId = call.parameters["productId"]!!

                try {
                    service.deleteItem(userId, productId)
                    call.respond(HttpStatusCode.OK)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Очистить корзину
            delete("/cart") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asString()

                service.clearCart(userId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

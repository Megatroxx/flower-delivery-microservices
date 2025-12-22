package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.flowers.AddFlowerRequest
import com.example.flowers.CatalogRepository
import com.example.flowers.CatalogService
import com.example.flowers.UpdateFlowerRequest
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
    catalogRepository: CatalogRepository = CatalogRepository(),
    catalogService: CatalogService = CatalogService(catalogRepository)
) {

    val repo = catalogRepository
    val service = catalogService

    routing {
        get("/catalog/{flowerId}") {
            val id = call.parameters["flowerId"]!!

            val flower = repo.findById(id)
                ?: return@get call.respondText(
                    "Flower not found",
                    status = HttpStatusCode.NotFound
                )

            call.respond(flower)

        }

        authenticate("auth-jwt-admin") {

            post("/catalog/add") {
                val req = call.receive<AddFlowerRequest>()

                try {
                    val resp = service.addFlower(req)
                    call.respond(HttpStatusCode.OK, resp)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            put("/catalog/update/{flowerId}") {
                val flowerId = call.parameters["flowerId"]
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing flowerId")
                    )

                val req = call.receive<UpdateFlowerRequest>()

                try {
                    val resp = service.updateFlower(flowerId, req)
                    call.respond(HttpStatusCode.OK, resp)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Not found")))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            delete("/catalog/delete/{flowerId}") {
                val flowerId = call.parameters["flowerId"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing flowerId")
                    )

                try {
                    val resp = service.deleteFlower(flowerId)
                    call.respond(HttpStatusCode.OK, resp)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Not found")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

        }
    }
}

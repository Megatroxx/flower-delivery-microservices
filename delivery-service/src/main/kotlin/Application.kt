package com.example

import com.example.delivery.DeliveryRepository
import com.example.delivery.DeliveryService
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(
        CIO,
        host = "0.0.0.0",
        port = 8087,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureSecurity()
    configureRouting()

    val service = DeliveryService(DeliveryRepository())
    RabbitMQConsumer(service).start()
}

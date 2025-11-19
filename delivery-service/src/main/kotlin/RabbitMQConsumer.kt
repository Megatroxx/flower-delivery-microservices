package com.example

import com.example.delivery.DeliveryService
import com.example.delivery.StartDeliveryRequest
import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class RabbitMQConsumer(
    private val service: DeliveryService
) {

    private val exchangeName = "order.events"
    private val queueName = "order.created.queue"
    private val routingKey = "order.created"

    fun start() {
        val factory = ConnectionFactory().apply {
            host = "localhost"
            username = "admin"
            password = "admin"
            port = 5672
        }

        val conn = factory.newConnection()
        val channel = conn.createChannel()

        channel.exchangeDeclare(exchangeName, "topic", true)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, routingKey)

        println(" [*] Waiting for order.created events...")

        val deliverCallback = { _: String?, delivery: com.rabbitmq.client.Delivery ->
            val message = String(delivery.body)
            println(" [x] Received event: $message")

            val event = Json.decodeFromString<OrderCreatedEvent>(message)

            service.startDelivery(
                StartDeliveryRequest(
                    orderId = event.orderId,
                    address = event.address
                )
            )

            println(" [✔] Delivery process started from event")
        }

        channel.basicConsume(queueName, true, deliverCallback, { _ -> })
    }
}
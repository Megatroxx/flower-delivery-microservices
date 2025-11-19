package com.example

import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RabbitMQProducer {

    private val exchangeName = "order.events"
    private val routingKey = "order.created"

    private val factory = ConnectionFactory().apply {
        host = "localhost"
        username = "admin"
        password = "admin"
        port = 5672
    }

    private val connection = factory.newConnection()
    private val channel = connection.createChannel()

    init {
        channel.exchangeDeclare(exchangeName, "topic", true)
    }

    fun publishOrderCreated(event: OrderCreatedEvent) {
        val json = Json.encodeToString(event)
        channel.basicPublish(exchangeName, routingKey, null, json.toByteArray())
        println(" [x] Sent event to RabbitMQ: $json")
    }
}
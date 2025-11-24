package com.example

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun refundEndpointReturnsOk() = testApplication {
        application {
            module()
        }
        client.post("/payments/refund") {
            contentType(ContentType.Application.Json)
            setBody("""{"orderId":"test-order","amount":100}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}

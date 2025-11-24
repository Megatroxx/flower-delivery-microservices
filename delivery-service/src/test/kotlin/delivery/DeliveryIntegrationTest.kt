package com.example.delivery

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeliveryIntegrationTest {

    private lateinit var repo: DeliveryRepository
    private lateinit var service: DeliveryService
    private lateinit var dbFile: Path

    @BeforeTest
    fun setup() {
        dbFile = Files.createTempFile("delivery-test", ".db").apply {
            toFile().deleteOnExit()
        }

        Database.connect(
            url = "jdbc:sqlite:${dbFile.toAbsolutePath()}",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(DeliveriesTable)
            DeliveriesTable.deleteAll()
        }

        repo = DeliveryRepository()
        service = DeliveryService(repo)
    }

    @AfterTest
    fun tearDown() {
        kotlin.runCatching { Files.deleteIfExists(dbFile) }
    }

    @Test
    fun deliveryLifecyclePersistsState() {
        val start = service.startDelivery(StartDeliveryRequest("order-1", "Street 1"))
        assertEquals(DeliveryStatus.STARTED, start.status)

        val assigned = service.assignCourier(start.deliveryId, "courier-1")
        assertEquals("courier-1", assigned.courierId)

        val updated = service.updateStatus(start.deliveryId, DeliveryStatus.IN_TRANSIT)
        assertEquals(DeliveryStatus.COURIER_ASSIGNED, updated.oldStatus)
        assertEquals(DeliveryStatus.IN_TRANSIT, updated.newStatus)

        val completed = service.completeDelivery(
            start.deliveryId,
            CompleteDeliveryRequest(
                deliveredAt = "2024-01-01T12:00:00Z",
                recipientSignature = "John"
            )
        )
        assertEquals(DeliveryStatus.DELIVERED, completed.status)

        val finalStatus = transaction {
            DeliveriesTable
                .select { DeliveriesTable.id eq start.deliveryId }
                .single()[DeliveriesTable.status]
        }
        assertEquals(DeliveryStatus.DELIVERED, finalStatus)
    }
}


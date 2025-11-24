package com.example.delivery

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeliveryServiceTest {

    private lateinit var repo: DeliveryRepository
    private lateinit var service: DeliveryService

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        service = DeliveryService(repo)
    }

    @Test
    fun startDeliveryCreatesRecord() {
        val request = StartDeliveryRequest(orderId = "order-1", address = "Street 1")
        val delivery = deliveryStub()

        every { repo.startDelivery(request.orderId, request.address) } returns delivery

        val result = service.startDelivery(request)

        assertEquals(
            StartDeliveryResponse(
                deliveryId = delivery.id,
                orderId = delivery.orderId,
                status = delivery.status,
                startedAt = delivery.startedAt
            ),
            result
        )
        verify(exactly = 1) { repo.startDelivery(request.orderId, request.address) }
    }

    @Test
    fun assignCourierReturnsUpdatedDelivery() {
        val delivery = deliveryStub().copy(courierId = "courier-1", updatedAt = "2024-01-01T01:00:00Z")

        every { repo.assignCourier(delivery.id, "courier-1") } returns delivery

        val result = service.assignCourier(delivery.id, "courier-1")

        assertEquals(
            AssignCourierResponse(
                deliveryId = delivery.id,
                courierId = "courier-1",
                status = delivery.status,
                assignedAt = delivery.updatedAt!!
            ),
            result
        )
    }

    @Test
    fun assignCourierFailsWhenMissing() {
        every { repo.assignCourier("missing", any()) } returns null

        assertFailsWith<NoSuchElementException> {
            service.assignCourier("missing", "courier")
        }
    }

    @Test
    fun updateStatusValidatesStatus() {
        assertFailsWith<IllegalArgumentException> {
            service.updateStatus("delivery", "invalid")
        }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun updateStatusReturnsResponse() {
        val updated = deliveryStub().copy(
            status = DeliveryStatus.IN_TRANSIT,
            updatedAt = "2024-01-01T02:00:00Z"
        )

        every { repo.updateStatus("delivery-1", DeliveryStatus.IN_TRANSIT) } returns
            (DeliveryStatus.STARTED to updated)

        val result = service.updateStatus("delivery-1", DeliveryStatus.IN_TRANSIT)

        assertEquals(
            UpdateDeliveryStatusResponse(
                deliveryId = updated.id,
                oldStatus = DeliveryStatus.STARTED,
                newStatus = DeliveryStatus.IN_TRANSIT,
                updatedAt = updated.updatedAt!!
            ),
            result
        )
    }

    @Test
    fun updateStatusFailsWhenRepoReturnsNull() {
        every { repo.updateStatus("missing", DeliveryStatus.IN_TRANSIT) } returns null

        assertFailsWith<NoSuchElementException> {
            service.updateStatus("missing", DeliveryStatus.IN_TRANSIT)
        }
    }

    @Test
    fun completeDeliveryReturnsConfirmation() {
        val updated = deliveryStub().copy(
            status = DeliveryStatus.DELIVERED,
            deliveredAt = "2024-01-01T03:00:00Z",
            recipientSignature = "John",
            updatedAt = "2024-01-01T03:00:00Z"
        )
        val request = CompleteDeliveryRequest(
            deliveredAt = "2024-01-01T03:00:00Z",
            recipientSignature = "John"
        )

        every { repo.complete(updated.id, request.deliveredAt, request.recipientSignature) } returns updated

        val result = service.completeDelivery(updated.id, request)

        assertEquals(
            CompleteDeliveryResponse(
                deliveryId = updated.id,
                status = updated.status,
                deliveredAt = updated.deliveredAt!!,
                message = "Заказ доставлен"
            ),
            result
        )
    }

    @Test
    fun completeDeliveryFailsWhenMissing() {
        val request = CompleteDeliveryRequest("2024-01-01T03:00:00Z", "John")
        every { repo.complete("missing", any(), any()) } returns null

        assertFailsWith<NoSuchElementException> {
            service.completeDelivery("missing", request)
        }
    }

    private fun deliveryStub() = Delivery(
        id = "delivery-1",
        orderId = "order-1",
        address = "Street 1",
        courierId = null,
        status = DeliveryStatus.STARTED,
        startedAt = "2024-01-01T00:00:00Z",
        updatedAt = null,
        deliveredAt = null,
        recipientSignature = null
    )
}


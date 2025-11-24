package com.example.payment

import com.example.refund.Refund
import com.example.refund.RefundRequest
import com.example.refund.RefundResponse
import io.mockk.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaymentServiceTest {

    private lateinit var repo: PaymentRepository
    private lateinit var service: PaymentService

    @BeforeTest
    fun setup() {
        clearAllMocks()
        repo = mockk(relaxed = true)
        service = PaymentService(repo)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun initiatePaymentReturnsRedirectData() {
        val req = InitiatePaymentRequest(
            orderId = "order-1",
            amount = 1200,
            paymentMethod = "card"
        )
        val payment = Payment(
            id = "payment-1",
            orderId = req.orderId,
            userId = "user-1",
            amount = req.amount,
            paymentMethod = req.paymentMethod,
            status = PaymentStatus.INITIATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.createPayment("user-1", req) } returns payment

        val result = service.initiatePayment("user-1", req)

        assertEquals(
            InitiatePaymentResponse(
                paymentId = payment.id,
                orderId = payment.orderId,
                status = payment.status,
                redirectUrl = "https://pay.example/redirect?p=payment-1"
            ),
            result
        )
        verify(exactly = 1) { repo.createPayment("user-1", req) }
    }

    @Test
    fun initiatePaymentFailsForNonPositiveAmount() {
        val req = InitiatePaymentRequest(
            orderId = "order-2",
            amount = 0,
            paymentMethod = "card"
        )

        assertFailsWith<IllegalArgumentException> {
            service.initiatePayment("user-1", req)
        }
        verify(exactly = 0) { repo.createPayment(any(), any()) }
    }

    @Test
    fun markSuccessReturnsConfirmation() {
        val paymentId = "payment-2"
        val existing = Payment(
            id = paymentId,
            orderId = "order-3",
            userId = "user-3",
            amount = 500,
            paymentMethod = "card",
            status = PaymentStatus.INITIATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )
        val updated = existing.copy(status = PaymentStatus.SUCCESS, updatedAt = "2024-01-01T10:00:00Z")

        every { repo.findPaymentById(paymentId) } returns existing
        every { repo.updateStatus(paymentId, PaymentStatus.SUCCESS) } returns (PaymentStatus.INITIATED to updated)

        val result = service.markSuccess(paymentId)

        assertEquals(
            PaymentSuccessResponse(
                paymentId = updated.id,
                status = updated.status,
                confirmedAt = "2024-01-01T10:00:00Z",
                message = "Платеж подтверждён"
            ),
            result
        )
        verify(exactly = 1) { repo.findPaymentById(paymentId) }
        verify(exactly = 1) { repo.updateStatus(paymentId, PaymentStatus.SUCCESS) }
    }

    @Test
    fun markSuccessFailsWhenPaymentMissing() {
        every { repo.findPaymentById("missing") } returns null

        assertFailsWith<NoSuchElementException> {
            service.markSuccess("missing")
        }
        verify(exactly = 1) { repo.findPaymentById("missing") }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun markSuccessFailsIfAlreadySuccess() {
        val payment = Payment(
            id = "payment-3",
            orderId = "order-4",
            userId = "user-4",
            amount = 700,
            paymentMethod = "card",
            status = PaymentStatus.SUCCESS,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T01:00:00Z"
        )

        every { repo.findPaymentById(payment.id) } returns payment

        assertFailsWith<IllegalStateException> {
            service.markSuccess(payment.id)
        }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun markSuccessFailsWhenUpdateReturnsNull() {
        val paymentId = "payment-4"
        val existing = Payment(
            id = paymentId,
            orderId = "order-5",
            userId = "user-5",
            amount = 900,
            paymentMethod = "card",
            status = PaymentStatus.INITIATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.findPaymentById(paymentId) } returns existing
        every { repo.updateStatus(paymentId, PaymentStatus.SUCCESS) } returns null

        assertFailsWith<IllegalStateException> {
            service.markSuccess(paymentId)
        }
        verify(exactly = 1) { repo.findPaymentById(paymentId) }
        verify(exactly = 1) { repo.updateStatus(paymentId, PaymentStatus.SUCCESS) }
    }

    @Test
    fun markFailedReturnsFailureInfo() {
        val paymentId = "payment-5"
        val existing = Payment(
            id = paymentId,
            orderId = "order-6",
            userId = "user-6",
            amount = 400,
            paymentMethod = "card",
            status = PaymentStatus.INITIATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )
        val updated = existing.copy(status = PaymentStatus.FAILED, updatedAt = "2024-01-02T00:00:00Z")

        every { repo.findPaymentById(paymentId) } returns existing
        every { repo.updateStatus(paymentId, PaymentStatus.FAILED) } returns (PaymentStatus.INITIATED to updated)

        val result = service.markFailed(paymentId, "card_declined")

        assertEquals(
            PaymentFailedResponse(
                paymentId = updated.id,
                status = updated.status,
                failedAt = "2024-01-02T00:00:00Z",
                reason = "card_declined"
            ),
            result
        )
        verify(exactly = 1) { repo.findPaymentById(paymentId) }
        verify(exactly = 1) { repo.updateStatus(paymentId, PaymentStatus.FAILED) }
    }

    @Test
    fun markFailedFailsWhenPaymentMissing() {
        every { repo.findPaymentById("missing") } returns null

        assertFailsWith<NoSuchElementException> {
            service.markFailed("missing", "card_declined")
        }
        verify(exactly = 1) { repo.findPaymentById("missing") }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun markFailedFailsIfAlreadyFailed() {
        val payment = Payment(
            id = "payment-6",
            orderId = "order-7",
            userId = "user-7",
            amount = 300,
            paymentMethod = "card",
            status = PaymentStatus.FAILED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T01:00:00Z"
        )

        every { repo.findPaymentById(payment.id) } returns payment

        assertFailsWith<IllegalStateException> {
            service.markFailed(payment.id, "card_declined")
        }
        verify(exactly = 0) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun markFailedFailsWhenUpdateReturnsNull() {
        val paymentId = "payment-7"
        val existing = Payment(
            id = paymentId,
            orderId = "order-8",
            userId = "user-8",
            amount = 250,
            paymentMethod = "card",
            status = PaymentStatus.INITIATED,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null
        )

        every { repo.findPaymentById(paymentId) } returns existing
        every { repo.updateStatus(paymentId, PaymentStatus.FAILED) } returns null

        assertFailsWith<IllegalStateException> {
            service.markFailed(paymentId, "card_declined")
        }
        verify(exactly = 1) { repo.findPaymentById(paymentId) }
        verify(exactly = 1) { repo.updateStatus(paymentId, PaymentStatus.FAILED) }
    }

    @Test
    fun refundCreatesProcessedRefund() {
        val req = RefundRequest(orderId = "order-9", amount = 150)
        val refund = Refund(
            id = "refund-1",
            orderId = req.orderId,
            amount = req.amount,
            status = "processed",
            processedAt = "2024-01-03T00:00:00Z"
        )

        every { repo.createRefund(req) } returns refund

        val result = service.refund(req)

        assertEquals(
            RefundResponse(
                refundId = refund.id,
                orderId = refund.orderId,
                amount = refund.amount,
                status = refund.status,
                processedAt = refund.processedAt
            ),
            result
        )
        verify(exactly = 1) { repo.createRefund(req) }
    }

    @Test
    fun refundFailsForNonPositiveAmount() {
        val req = RefundRequest(orderId = "order-10", amount = -1)

        assertFailsWith<IllegalArgumentException> {
            service.refund(req)
        }
        verify(exactly = 0) { repo.createRefund(any()) }
    }
}


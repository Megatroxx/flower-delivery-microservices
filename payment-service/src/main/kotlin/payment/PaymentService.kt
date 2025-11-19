package com.example.payment

import com.example.refund.RefundRequest
import com.example.refund.RefundResponse
import java.time.Instant

class PaymentService(
    private val repo: PaymentRepository
) {

    fun initiatePayment(userId: String, req: InitiatePaymentRequest): InitiatePaymentResponse {
        if (req.amount <= 0) {
            throw IllegalArgumentException("Amount must be positive")
        }

        val payment = repo.createPayment(userId, req)

        val redirectUrl = "https://pay.example/redirect?p=${payment.id}"

        return InitiatePaymentResponse(
            paymentId = payment.id,
            orderId = payment.orderId,
            status = payment.status,
            redirectUrl = redirectUrl
        )
    }

    fun markSuccess(paymentId: String): PaymentSuccessResponse {
        val payment = repo.findPaymentById(paymentId)
            ?: throw NoSuchElementException("Payment not found")

        if (payment.status == PaymentStatus.SUCCESS) {
            throw IllegalStateException("Payment already successful")
        }

        val (_, updated) = repo.updateStatus(paymentId, PaymentStatus.SUCCESS)
            ?: throw IllegalStateException("Failed to update payment")

        val confirmedAt = updated.updatedAt ?: Instant.now().toString()

        return PaymentSuccessResponse(
            paymentId = updated.id,
            status = updated.status,
            confirmedAt = confirmedAt,
            message = "Платеж подтверждён"
        )
    }

    fun markFailed(paymentId: String, reason: String): PaymentFailedResponse {
        val payment = repo.findPaymentById(paymentId)
            ?: throw NoSuchElementException("Payment not found")

        if (payment.status == PaymentStatus.FAILED) {
            throw IllegalStateException("Payment already failed")
        }

        val (_, updated) = repo.updateStatus(paymentId, PaymentStatus.FAILED)
            ?: throw IllegalStateException("Failed to update payment")

        val failedAt = updated.updatedAt ?: Instant.now().toString()

        return PaymentFailedResponse(
            paymentId = updated.id,
            status = updated.status,
            failedAt = failedAt,
            reason = reason
        )
    }

    fun refund(req: RefundRequest): RefundResponse {
        if (req.amount <= 0) {
            throw IllegalArgumentException("Amount must be positive")
        }

        val refund = repo.createRefund(req)

        return RefundResponse(
            refundId = refund.id,
            orderId = refund.orderId,
            amount = refund.amount,
            status = refund.status,
            processedAt = refund.processedAt
        )
    }
}
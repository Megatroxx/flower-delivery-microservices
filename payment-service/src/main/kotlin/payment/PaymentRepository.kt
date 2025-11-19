package com.example.payment

import com.example.refund.Refund
import com.example.refund.RefundRequest
import com.example.refund.RefundTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class PaymentRepository {

    fun createPayment(userId: String, req: InitiatePaymentRequest): Payment = transaction {
        val paymentId = UUID.randomUUID().toString()
        val createdAt = Instant.now().toString()

        PaymentTable.insert {
            it[id] = paymentId
            it[orderId] = req.orderId
            it[PaymentTable.userId] = userId
            it[amount] = req.amount
            it[paymentMethod] = req.paymentMethod
            it[status] = PaymentStatus.INITIATED
            it[PaymentTable.createdAt] = createdAt
            it[updatedAt] = null
        }

        Payment(
            id = paymentId,
            orderId = req.orderId,
            userId = userId,
            amount = req.amount,
            paymentMethod = req.paymentMethod,
            status = PaymentStatus.INITIATED,
            createdAt = createdAt,
            updatedAt = null
        )
    }

    fun findPaymentById(id: String): Payment? = transaction {
        PaymentTable
            .selectAll()
            .where { PaymentTable.id eq id }
            .singleOrNull()
            ?.let {
                Payment(
                    id = it[PaymentTable.id],
                    orderId = it[PaymentTable.orderId],
                    userId = it[PaymentTable.userId],
                    amount = it[PaymentTable.amount],
                    paymentMethod = it[PaymentTable.paymentMethod],
                    status = it[PaymentTable.status],
                    createdAt = it[PaymentTable.createdAt],
                    updatedAt = it[PaymentTable.updatedAt]
                )
            }
    }

    fun updateStatus(paymentId: String, newStatus: String): Pair<String, Payment>? = transaction {
        val oldRow = PaymentTable
            .selectAll()
            .where { PaymentTable.id eq paymentId }
            .singleOrNull() ?: return@transaction null

        val oldStatus = oldRow[PaymentTable.status]
        val now = Instant.now().toString()

        PaymentTable.update(where = { PaymentTable.id eq paymentId }) {
            it[status] = newStatus
            it[updatedAt] = now
        }

        val updated = PaymentTable
            .selectAll()
            .where { PaymentTable.id eq paymentId }
            .single()

        oldStatus to Payment(
            id = updated[PaymentTable.id],
            orderId = updated[PaymentTable.orderId],
            userId = updated[PaymentTable.userId],
            amount = updated[PaymentTable.amount],
            paymentMethod = updated[PaymentTable.paymentMethod],
            status = updated[PaymentTable.status],
            createdAt = updated[PaymentTable.createdAt],
            updatedAt = updated[PaymentTable.updatedAt]
        )
    }

    fun createRefund(req: RefundRequest): Refund = transaction {
        val refundId = UUID.randomUUID().toString()
        val processedAt = Instant.now().toString()

        RefundTable.insert {
            it[id] = refundId
            it[orderId] = req.orderId
            it[amount] = req.amount
            it[status] = "processed"
            it[RefundTable.processedAt] = processedAt
        }

        Refund(
            id = refundId,
            orderId = req.orderId,
            amount = req.amount,
            status = "processed",
            processedAt = processedAt
        )
    }
}
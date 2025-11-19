package com.example.notification

import com.example.users.UsersClient
import java.time.Instant
import java.util.*

class NotificationService(
    private val usersClient: UsersClient
) {

    suspend fun sendNotification(req: SendNotificationRequest): NotificationResponse {
        val user = usersClient.findUser(req.userId)
            ?: throw NoSuchElementException("User not found")

        return NotificationResponse(
            notificationId = UUID.randomUUID().toString(),
            userId = user.id,
            status = "sent",
            sentAt = Instant.now().toString()
        )
    }

    suspend fun broadcast(req: BroadcastRequest): BroadcastResponse {
        val users = usersClient.getAllUsers()

        val target = when (req.segment) {
            "all_customers" -> users
            "couriers" -> users.filter { it.role == "courier" }
            "admins" -> users.filter { it.role == "admin" }
            else -> users
        }

        return BroadcastResponse(
            broadcastId = UUID.randomUUID().toString(),
            targetCount = target.size,
            status = "scheduled",
            scheduledAt = Instant.now().toString()
        )
    }
}
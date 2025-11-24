package com.example.notification

import com.example.users.UserDto
import com.example.users.UsersClient
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NotificationServiceTest {

    private lateinit var usersClient: UsersClient
    private lateinit var service: NotificationService

    @BeforeTest
    fun setup() {
        usersClient = mockk(relaxed = true)
        service = NotificationService(usersClient)
    }

    @Test
    fun sendNotificationSucceedsWhenUserExists(): Unit = runBlocking {
        val request = SendNotificationRequest("user-1", "status", "Hello")
        val user = UserDto("user-1", "user@example.com", "User", "user")

        coEvery { usersClient.findUser(request.userId) } returns user

        val result = service.sendNotification(request)

        assertEquals(user.id, result.userId)
        assertEquals("sent", result.status)
    }

    @Test
    fun sendNotificationFailsWhenUserMissing(): Unit = runBlocking {
        val request = SendNotificationRequest("missing", "status", "Hello")
        coEvery { usersClient.findUser(request.userId) } returns null

        assertFailsWith<NoSuchElementException> {
            service.sendNotification(request)
        }
    }

    @Test
    fun broadcastFiltersBySegment(): Unit = runBlocking {
        val users = listOf(
            UserDto("1", "a@example.com", "Alice", "user"),
            UserDto("2", "b@example.com", "Bob", "courier"),
            UserDto("3", "c@example.com", "Cleo", "admin")
        )
        coEvery { usersClient.getAllUsers() } returns users

        val allResp = service.broadcast(BroadcastRequest("all_customers", "Hi"))
        assertEquals(3, allResp.targetCount)

        val courierResp = service.broadcast(BroadcastRequest("couriers", "Hi"))
        assertEquals(1, courierResp.targetCount)

        val adminResp = service.broadcast(BroadcastRequest("admins", "Hi"))
        assertEquals(1, adminResp.targetCount)
    }
}


package com.example.users

import com.example.jwt.JwtService
import com.example.utils.PasswordHasher
import io.mockk.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceTest {

    private lateinit var repo: UserRepository
    private lateinit var service: UserService

    @BeforeTest
    fun setup() {
        unmockkAll()
        repo = mockk(relaxed = true)
        mockkObject(PasswordHasher)
        mockkObject(JwtService)
        service = UserService(repo)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun registerCreatesUserWhenEmailUnique() {
        val request = RegisterRequest(
            email = "jane@example.com",
            password = "secret",
            name = "Jane"
        )
        val storedUser = User(
            id = "user-1",
            email = request.email,
            name = request.name,
            role = "user",
            createdAt = "2024-01-01T00:00:00Z"
        )

        every { repo.findByEmail(request.email) } returns null
        every { PasswordHasher.hash(request.password) } returns "hashed-secret"
        every { repo.createUser(request.email, "hashed-secret", request.name) } returns storedUser

        val result = service.register(request)

        assertEquals(
            UserResponse(storedUser.id, storedUser.email, storedUser.name, storedUser.createdAt),
            result
        )
        verify(exactly = 1) { repo.findByEmail(request.email) }
        verify(exactly = 1) { PasswordHasher.hash(request.password) }
        verify(exactly = 1) { repo.createUser(request.email, "hashed-secret", request.name) }
    }

    @Test
    fun registerFailsWhenEmailAlreadyExists() {
        val request = RegisterRequest(
            email = "john@example.com",
            password = "secret",
            name = "John"
        )
        val existingUser = User(
            id = "user-2",
            email = request.email,
            name = request.name,
            role = "user",
            createdAt = "2024-01-01T00:00:00Z"
        )

        every { repo.findByEmail(request.email) } returns (existingUser to "hashed")

        assertFailsWith<IllegalStateException> {
            service.register(request)
        }
        verify(exactly = 1) { repo.findByEmail(request.email) }
        verify(exactly = 0) { repo.createUser(any(), any(), any()) }
        verify(exactly = 0) { PasswordHasher.hash(any()) }
    }

    @Test
    fun loginReturnsTokenForValidCredentials() {
        val request = LoginRequest(email = "user@example.com", password = "top-secret")
        val user = User(
            id = "user-3",
            email = request.email,
            name = "User",
            role = "user",
            createdAt = "2024-01-01T00:00:00Z"
        )

        every { repo.findByEmail(request.email) } returns (user to "stored-hash")
        every { PasswordHasher.verify(request.password, "stored-hash") } returns true
        every { JwtService.createAccessToken(user) } returns "token-123"

        val result = service.login(request)

        assertEquals(TokenResponse("token-123", 3600), result)
        verify(exactly = 1) { repo.findByEmail(request.email) }
        verify(exactly = 1) { PasswordHasher.verify(request.password, "stored-hash") }
        verify(exactly = 1) { JwtService.createAccessToken(user) }
    }

    @Test
    fun loginFailsWhenUserAbsent() {
        val request = LoginRequest(email = "absent@example.com", password = "any")
        every { repo.findByEmail(request.email) } returns null

        assertFailsWith<IllegalArgumentException> {
            service.login(request)
        }
        verify(exactly = 1) { repo.findByEmail(request.email) }
        verify(exactly = 0) { PasswordHasher.verify(any(), any()) }
    }

    @Test
    fun loginFailsWhenPasswordMismatch() {
        val request = LoginRequest(email = "user@example.com", password = "wrong")
        val user = User(
            id = "user-4",
            email = request.email,
            name = "User",
            role = "user",
            createdAt = "2024-01-01T00:00:00Z"
        )

        every { repo.findByEmail(request.email) } returns (user to "stored-hash")
        every { PasswordHasher.verify(request.password, "stored-hash") } returns false

        assertFailsWith<IllegalArgumentException> {
            service.login(request)
        }
        verify(exactly = 1) { repo.findByEmail(request.email) }
        verify(exactly = 1) { PasswordHasher.verify(request.password, "stored-hash") }
        verify(exactly = 0) { JwtService.createAccessToken(any()) }
    }

    @Test
    fun changeRoleSucceedsForAdminAndValidRole() {
        val admin = User(
            id = "admin-1",
            email = "admin@example.com",
            name = "Admin",
            role = "admin",
            createdAt = "2024-01-01T00:00:00Z"
        )
        val target = User(
            id = "user-5",
            email = "target@example.com",
            name = "Target",
            role = "user",
            createdAt = "2024-01-02T00:00:00Z"
        )

        every { repo.findById(target.id) } returns target
        every { repo.setRole(target.id, "courier") } returns true

        val result = service.changeRole(admin, target.id, "courier")

        assertEquals(target.copy(role = "courier"), result)
        verify(exactly = 1) { repo.findById(target.id) }
        verify(exactly = 1) { repo.setRole(target.id, "courier") }
    }

    @Test
    fun changeRoleFailsForNonAdminRequester() {
        val requester = User(
            id = "user-6",
            email = "user@example.com",
            name = "User",
            role = "user",
            createdAt = "2024-01-01T00:00:00Z"
        )

        assertFailsWith<SecurityException> {
            service.changeRole(requester, "other", "admin")
        }
        verify(exactly = 0) { repo.findById(any()) }
        verify(exactly = 0) { repo.setRole(any(), any()) }
    }

    @Test
    fun changeRoleFailsForInvalidRoleValue() {
        val admin = User(
            id = "admin-2",
            email = "admin@example.com",
            name = "Admin",
            role = "admin",
            createdAt = "2024-01-01T00:00:00Z"
        )

        assertFailsWith<IllegalArgumentException> {
            service.changeRole(admin, "target", "superuser")
        }
        verify(exactly = 0) { repo.findById(any()) }
        verify(exactly = 0) { repo.setRole(any(), any()) }
    }

    @Test
    fun changeRoleFailsWhenUserMissing() {
        val admin = User(
            id = "admin-3",
            email = "admin@example.com",
            name = "Admin",
            role = "admin",
            createdAt = "2024-01-01T00:00:00Z"
        )

        every { repo.findById("missing") } returns null

        assertFailsWith<NoSuchElementException> {
            service.changeRole(admin, "missing", "courier")
        }
        verify(exactly = 1) { repo.findById("missing") }
        verify(exactly = 0) { repo.setRole(any(), any()) }
    }

    @Test
    fun changeRoleFailsWhenRepositoryUpdateFails() {
        val admin = User(
            id = "admin-4",
            email = "admin@example.com",
            name = "Admin",
            role = "admin",
            createdAt = "2024-01-01T00:00:00Z"
        )
        val target = User(
            id = "user-7",
            email = "target@example.com",
            name = "Target",
            role = "user",
            createdAt = "2024-01-02T00:00:00Z"
        )

        every { repo.findById(target.id) } returns target
        every { repo.setRole(target.id, "courier") } returns false

        assertFailsWith<IllegalStateException> {
            service.changeRole(admin, target.id, "courier")
        }
        verify(exactly = 1) { repo.findById(target.id) }
        verify(exactly = 1) { repo.setRole(target.id, "courier") }
    }
}


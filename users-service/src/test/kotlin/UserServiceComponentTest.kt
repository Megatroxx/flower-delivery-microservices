import com.example.users.*
import com.example.utils.PasswordHasher
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.Instant

class UserServiceComponentTest {

    private val repo = mock<UserRepository>()
    private val service = UserService(repo)

    @Test
    fun `should register new user successfully`() {
        val req = RegisterRequest(
            email = "test@mail.com",
            password = "123456",
            name = "John"
        )

        whenever(repo.findByEmail("test@mail.com")).thenReturn(null)

        val fakeUser = User(
            id = "u-123",
            email = "test@mail.com",
            name = "John",
            role = "user",
            createdAt = Instant.now().toString()
        )

        whenever(repo.createUser(eq("test@mail.com"), any(), eq("John")))
            .thenReturn(fakeUser)

        val resp = service.register(req)

        assertEquals("u-123", resp.id)
        assertEquals("test@mail.com", resp.email)
        assertEquals("John", resp.name)

        verify(repo).findByEmail("test@mail.com")
        verify(repo).createUser(eq("test@mail.com"), any(), eq("John"))
    }


    @Test
    fun `should login successfully with correct password`() {
        val user = User(
            id = "u-555",
            email = "login@mail.com",
            name = "Mike",
            role = "user",
            createdAt = "2020"
        )
        val hashedPassword = PasswordHasher.hash("secret")

        whenever(repo.findByEmail("login@mail.com"))
            .thenReturn(user to hashedPassword)

        val resp = service.login(LoginRequest("login@mail.com", "secret"))

        assertNotNull(resp.accessToken)
        assertEquals(3600, resp.expiresIn)

        verify(repo).findByEmail("login@mail.com")
    }

    @Test
    fun `admin should change user role successfully`() {
        val admin = User(
            id = "adm-1",
            email = "admin@mail.com",
            name = "Admin",
            role = "admin",
            createdAt = "2020"
        )

        val targetUser = User(
            id = "u-777",
            email = "user@mail.com",
            name = "Target",
            role = "user",
            createdAt = "2020"
        )

        whenever(repo.findById("u-777"))
            .thenReturn(targetUser)

        whenever(repo.setRole("u-777", "courier"))
            .thenReturn(true)

        val updated = service.changeRole(admin, "u-777", "courier")

        assertEquals("u-777", updated.id)
        assertEquals("courier", updated.role)

        verify(repo).findById("u-777")
        verify(repo).setRole("u-777", "courier")
    }
}
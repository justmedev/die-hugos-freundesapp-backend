package service

import core.exceptions.Unauthorized
import de.mkammerer.argon2.Argon2
import io.ktor.server.config.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import service.auth.AuthService
import service.user.UserService
import testutils.Users
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

class AuthServiceTest {
    private val userService = mockk<UserService>()
    private val config = mockk<ApplicationConfig>()
    private val argon2 = mockk<Argon2>()
    private lateinit var authService: AuthService

    private val user = Users.nonAdminUser

    @Before
    fun setup() {
        every { config.property("jwt.secret").getString() } returns "secret"
        every { config.property("jwt.issuer").getString() } returns "issuer"
        every { config.property("jwt.audience").getString() } returns "audience"

        authService = AuthService(userService, config, argon2)
    }

    @Test
    fun `login - success`() {
        runBlocking {
            val cmd = LoginCommand("test@example.com", "password")
            coEvery { userService.findByEmail(cmd.email) } returns user
            every { argon2.verify(any<String>(), any<CharArray>()) } returns true

            val result = authService.login(cmd)

            assertNotNull(result)
            assertEquals(user.id, result.user.id)
            assertNotNull(result.accessToken)
            assertNotNull(result.refreshToken)
        }
    }

    @Test
    fun `login - admin success`() {
        runBlocking {
            val adminUser = Users.adminUser
            val cmd = LoginCommand(adminUser.email, "password")
            coEvery { userService.findByEmail(cmd.email) } returns adminUser
            every { argon2.verify(any<String>(), any<CharArray>()) } returns true

            val result = authService.login(cmd)

            assertNotNull(result)
            assertEquals(adminUser.id, result.user.id)
            // JWT decoding to verify claim would be better, but for now we check if it returns a token pair
            assertNotNull(result.accessToken)
        }
    }

    @Test
    fun `login - wrong password - throws Unauthorized`() {
        runBlocking {
            val cmd = LoginCommand("test@example.com", "wrong")
            coEvery { userService.findByEmail(cmd.email) } returns user
            every { argon2.verify(any<String>(), any<CharArray>()) } returns false

            assertFailsWith<Unauthorized> {
                authService.login(cmd)
            }
        }
    }

    @Test
    fun `login - user not found - throws Unauthorized`() {
        runBlocking {
            val cmd = LoginCommand("notfound@example.com", "password")
            coEvery { userService.findByEmail(cmd.email) } throws core.exceptions.UserNotFound()

            assertFailsWith<Unauthorized> {
                authService.login(cmd)
            }
        }
    }

    @Test
    fun `register - success`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val cmd = RegisterCommand(
                "test@example.com", "First", "Last", null, null, "password", now.date, false
            )
            every { argon2.hash(any<Int>(), any<Int>(), any<Int>(), any<CharArray>()) } returns "hashed_password"
            coEvery { userService.create(any()) } returns user

            val result = authService.register(cmd)

            assertNotNull(result)
            assertEquals(user.id, result.id)
            coVerify { userService.create(any()) }
        }
    }

    @Test
    fun `refresh - success`() {
        runBlocking {
            coEvery { userService.findByEmail(user.email) } returns user
            every { argon2.verify(any<String>(), any<CharArray>()) } returns true

            // We need a real refresh token because it's verified by JWT
            val tokenPair = authService.login(LoginCommand(user.email, "password"))
            val refreshToken = tokenPair.refreshToken

            coEvery { userService.findById(user.id) } returns user

            val result = authService.refresh(RefreshCommand(refreshToken))

            assertNotNull(result)
            assertEquals(user.id, result.user.id)
        }
    }

    @Test
    fun `refresh - invalid token - throws Unauthorized`() {
        runBlocking {
            assertFailsWith<Unauthorized> {
                authService.refresh(RefreshCommand("invalid_token"))
            }
        }
    }
}

package service

import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import service.auth.AuthService
import service.user.UserService
import testutils.Users
import kotlin.test.assertNull

class AuthServiceTest {
    private val userService = mockk<UserService>()
    private val config = mockk<ApplicationConfig>()
    private lateinit var authService: AuthService

    private val user = Users.nonAdminUser

    // TODO: create user when no user with keycloakid is present
    // TODO: update user when keycloakid is known

    @Before
    fun setup() {
        every { config.property("jwt.secret").getString() } returns "secret"
        every { config.property("jwt.issuer").getString() } returns "issuer"
        every { config.property("jwt.audience").getString() } returns "audience"

        authService = AuthService(userService, config)
    }

    @Test
    fun `validateCredential - is invalid - returns null`() {
        runBlocking {
            assertNull(authService.validateCredential(mockk<JWTCredential>()))
        }
    }

    /*@Test
    fun `validateCredential - valid token - returns principal`() {
        runBlocking {
            assertFailsWith<Unauthorized> {
                authService.refresh(RefreshCommand("invalid_token"))
            }
        }
    }*/
}

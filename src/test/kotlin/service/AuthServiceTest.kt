package service

import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import service.auth.AuthService
import service.user.UserService
import testutils.Users

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

    /*@Test
    fun `validateCredential - valid token - returns principal`() {
        runBlocking {
            assertFailsWith<Unauthorized> {
                authService.refresh(RefreshCommand("invalid_token"))
            }
        }
    }*/
}

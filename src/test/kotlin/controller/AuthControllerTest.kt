package controller

import domain.models.User
import domain.models.UserTokenPair
import dto.auth.AuthResponse
import dto.auth.LoginRequest
import dto.auth.RefreshRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class AuthControllerTest : BaseControllerTest() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val user = User(1, "test@example.com", "Test", "User", "hashed", now, false, now)

    @Test
    fun `login - success`() = withTestApplication {
        val request = LoginRequest("test@example.com", "password")
        val tokenPair = UserTokenPair("access", "refresh", user)

        coEvery { authService.login(any()) } returns tokenPair

        val client = createClient()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("access", body.accessToken)
        assertEquals("test@example.com", body.user.email)
    }

    @Test
    fun `login - failure`() = withTestApplication {
        val request = LoginRequest("test@example.com", "wrong")

        coEvery { authService.login(any()) } throws Exception("Invalid email or password")

        val client = createClient()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `refresh - success`() = withTestApplication {
        val request = RefreshRequest("old_refresh")
        val tokenPair = UserTokenPair("new_access", "new_refresh", user)

        coEvery { authService.refresh(any()) } returns tokenPair

        val client = createClient()
        val response = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("new_access", body.accessToken)
    }
}

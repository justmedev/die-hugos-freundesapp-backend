package controller

import core.exceptions.Unauthorized
import domain.models.UserTokenPair
import dto.auth.AuthResponse
import dto.auth.LoginRequest
import dto.auth.RefreshRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals

class AuthControllerTest : BaseControllerTest() {
    private val nonAdminUser = Users.nonAdminUser

    @Test
    fun `login - success`() = withTestApplication {
        val request = LoginRequest("test@example.com", "password")
        val tokenPair = UserTokenPair("access", "refresh", nonAdminUser)

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

        coEvery { authService.login(any()) } throws Unauthorized()

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
        val tokenPair = UserTokenPair("new_access", "new_refresh", nonAdminUser)

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

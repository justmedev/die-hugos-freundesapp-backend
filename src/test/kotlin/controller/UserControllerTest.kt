package controller

import dto.user.CreateUserRequest
import dto.user.UserResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class UserControllerTest : BaseControllerTest() {

    @Test
    fun `post user - as admin - success`() = withTestApplication(createMockPrincipal(1, "admin")) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = CreateUserRequest(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "password",
            birthDate = now,
            isAdmin = false
        )

        coEvery { authService.register(any()) } returns domain.models.User(
            id = 2,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = "hashed_password",
            birthdate = request.birthDate,
            isAdmin = request.isAdmin,
            createdAt = now
        )

        val client = createClient()
        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        if (response.status != HttpStatusCode.OK) {
            println("Response: ${response.status} - ${response.bodyAsText()}")
        }
        assertEquals(HttpStatusCode.OK, response.status, "Response status should be OK but was ${response.status}")
        val body = response.body<UserResponse>()
        assertEquals(request.email, body.email)
        assertEquals(2, body.id)
    }

    @Test
    fun `post user - as user - forbidden`() = withTestApplication(createMockPrincipal(1, "user")) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = CreateUserRequest(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "password",
            birthDate = now,
            isAdmin = false
        )

        val client = createClient()
        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `post user - no auth - unauthorized`() = withTestApplication {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = CreateUserRequest(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "password",
            birthDate = now,
            isAdmin = false
        )

        val client = createClient()
        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `post user - service failure - unauthorized`() = withTestApplication(createMockPrincipal(1, "admin")) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = CreateUserRequest(
            email = "test@ex.com",
            firstName = "F",
            lastName = "L",
            password = "p",
            birthDate = now,
            isAdmin = false
        )

        coEvery { authService.register(any()) } throws Exception("Failed")

        val client = createClient()
        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}

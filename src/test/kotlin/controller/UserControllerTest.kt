package controller

import domain.models.User
import dto.user.CreateUserRequest
import dto.user.UpdateUserRequest
import dto.user.UserResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals
import kotlin.time.Clock

class UserControllerTest : BaseControllerTest() {
    // TODO: put user needs to pass through keycloak, this would also change these tests.

    @Test
    fun `put user - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = UpdateUserRequest(
            email = "updated@example.com",
            firstName = "Updated",
            lastName = "User",
            birthdate = now.date
        )

        coEvery { userService.update(any(), any()) } returns User(
            id = 1,
            keycloakId = "",
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            birthdate = request.birthdate,
            isAdmin = false,
            createdAt = now
        )

        val client = createClient()
        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<UserResponse>()
        assertEquals(request.email, body.email)
        assertEquals(1, body.id)
    }

    @Test
    fun `put user - user not found - not found`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = UpdateUserRequest(
            email = "updated@example.com",
            firstName = "Updated",
            lastName = "User",
            birthdate = now.date
        )

        coEvery { userService.update(any(), any()) } throws core.exceptions.UserNotFound()

        val client = createClient()
        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `put user - no auth - unauthorized`() = withTestApplication {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val request = UpdateUserRequest(
            email = "updated@example.com",
            firstName = "Updated",
            lastName = "User",
            birthdate = now.date
        )

        val client = createClient()
        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}

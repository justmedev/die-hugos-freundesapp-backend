package controller

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import core.utils.UpdateProperty
import domain.models.Cashpool
import dto.cashpool.CashpoolResponse
import dto.cashpool.CreateCashpoolRequest
import dto.cashpool.UpdateCashpoolRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolControllerTest : BaseControllerTest() {
    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val owner = Users.nonAdminUser

    @Test
    fun `post cashpool - success`() = withTestApplication(createMockPrincipal(owner)) {
        val request = CreateCashpoolRequest("Title", "Description")
        val created = Cashpool(1, request.title, request.description, owner, true, now)

        coEvery { cashpoolService.create(any()) } returns created
        coEvery { cashpoolMemberService.create(any()) } returns mockk()

        val client = createClient()
        val response = client.post("/cashpools") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<CashpoolResponse>()
        assertEquals(request.title, body.title)
    }

    @Test
    fun `put cashpool - success`() = withTestApplication(createMockPrincipal(owner)) {
        val request = UpdateCashpoolRequest(UpdateProperty("New Title"), UpdateProperty("New Description"))
        val updated = Cashpool(1, request.title.value!!, request.description.value!!, owner, true, now)

        coEvery { cashpoolService.update(owner.id, any()) } returns updated

        val client = createClient()
        val response = client.put("/cashpools/1") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CashpoolResponse>()
        assertEquals(request.title.value, body.title)
    }

    @Test
    fun `put cashpool - unauthorized`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val request = UpdateCashpoolRequest(UpdateProperty("New Title"), UpdateProperty("New Description"))

        coEvery { cashpoolService.update(any(), any()) } throws core.exceptions.Unauthorized("Forbidden")

        val client = createClient()
        val response = client.put("/cashpools/1") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `put cashpool - not a member`() = withTestApplication(createMockPrincipal(owner)) {
        val request = UpdateCashpoolRequest(UpdateProperty("New Title"), UpdateProperty("New Description"))

        coEvery { cashpoolService.update(any(), any()) } throws NotaCashpoolMember()

        val client = createClient()
        val response = client.put("/cashpools/1") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `get cashpools - success`() = withTestApplication(createMockPrincipal(owner)) {
        val cashpools = listOf(
            Cashpool(1, "Title 1", "Desc 1", owner, true, now),
            Cashpool(2, "Title 2", "Desc 2", owner, true, now)
        )

        coEvery { cashpoolService.findAll() } returns cashpools

        val client = createClient()
        val response = client.get("/cashpools") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolResponse>>()
        assertEquals(2, body.size)
    }

    @Test
    fun `get cashpool by id - success`() = withTestApplication(createMockPrincipal(owner)) {
        val cashpool = Cashpool(1, "Title", "Desc", owner, true, now)

        coEvery { cashpoolService.findByIdOnlyIfMember(1, owner.id) } returns cashpool

        val client = createClient()
        val response = client.get("/cashpools/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CashpoolResponse>()
        assertEquals(1, body.id)
    }

    @Test
    fun `get cashpool by id - not found`() = withTestApplication(createMockPrincipal(owner)) {
        coEvery { cashpoolService.findByIdOnlyIfMember(1, owner.id) } throws CashpoolNotFound()

        val client = createClient()
        val response = client.get("/cashpools/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get cashpool by id - forbidden`() = withTestApplication(createMockPrincipal(owner)) {
        coEvery { cashpoolService.findByIdOnlyIfMember(1, owner.id) } throws NotaCashpoolMember()

        val client = createClient()
        val response = client.get("/cashpools/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `delete cashpool by id - success`() = withTestApplication(createMockPrincipal(owner)) {
        coEvery { cashpoolService.deleteById(1, owner.id) } returns Unit

        val client = createClient()
        val response = client.delete("/cashpools/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}

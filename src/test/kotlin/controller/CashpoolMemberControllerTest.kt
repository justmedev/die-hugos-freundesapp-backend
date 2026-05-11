package controller

import domain.models.Cashpool
import domain.models.CashpoolMember
import domain.models.User
import dto.cashpool_member.CashpoolMemberResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool_member.CreateCashpoolMemberCommand
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolMemberControllerTest : BaseControllerTest() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val user = User(1, "user@example.com", "First", "Last", null, null, "pass", now, false, now)
    private val cashpool = Cashpool(1, "Title", "Desc", user, true, now)

    @Test
    fun `post join cashpool - success`() = withTestApplication(createMockPrincipal(1)) {
        val member = CashpoolMember(1, user, cashpool, now)

        coEvery { cashpoolMemberService.create(CreateCashpoolMemberCommand(1, 1)) } returns member

        val client = createClient()
        val response = client.post("/cashpools/1/members") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<CashpoolMemberResponse>()
        assertEquals(1, body.id)
        assertEquals(user.id, body.user.id)
    }

    @Test
    fun `post join cashpool - invalid cashpoolId`() = withTestApplication(createMockPrincipal(1)) {
        val client = createClient()
        val response = client.post("/cashpools/invalid/members") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `post join cashpool - unauthorized when no principal`() = withTestApplication(null) {
        val client = createClient()
        val response = client.post("/cashpools/1/members") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}

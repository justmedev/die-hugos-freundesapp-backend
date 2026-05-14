package controller

import domain.models.CashpoolSettlement
import dto.cashpool_settlement.CashpoolSettlementResponse
import dto.cashpool_settlement.CreateCashpoolSettlementRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolSettlementControllerTest : BaseControllerTest() {
    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val from = Users.nonAdminUser
    private val to = Users.nonAdminUser

    @Test
    fun `post settlement - success`() = withTestApplication(createMockPrincipal(1)) {
        val request = CreateCashpoolSettlementRequest(from.id, to.id, 10_00, "Label")
        val created = CashpoolSettlement(1, from, to, request.amountCents, request.purpose, now)

        coEvery { cashpoolSettlementService.create(any()) } returns created

        val client = createClient()
        val response = client.post("/cashpools/1/settle") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<CashpoolSettlementResponse>()
        assertEquals(request.fromId, body.from.id)
        assertEquals(request.toId, body.to.id)
        assertEquals(request.amountCents, body.amountCents)
        assertEquals(request.purpose, body.purpose)
    }

    @Test
    fun `get settlements - success`() = withTestApplication(createMockPrincipal(1)) {
        val settlements = listOf(
            CashpoolSettlement(1, from, to, 10_00, "Label 1", now),
            CashpoolSettlement(2, from, to, 25_00, "Label 2", now)
        )

        coEvery { cashpoolSettlementService.findByCashpoolId(1, 1) } returns settlements

        val client = createClient()
        val response = client.get("/cashpools/1/settle") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolSettlementResponse>>()
        assertEquals(2, body.size)
    }
}

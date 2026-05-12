package controller

import domain.models.CashpoolSettlement
import domain.models.User
import dto.cashpool_settlement.CashpoolSettlementResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolSettlementControllerTest : BaseControllerTest() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val user1 = User(1, "u1@ex.com", "U1", "L1", null, null,"p", now, false, now)
    private val user2 = User(2, "u2@ex.com", "U2", "L2", null, null,"p", now, false, now)

    @Test
    fun `get settlements - success`() = withTestApplication(createMockPrincipal(1)) {
        val settlements = listOf(
            CashpoolSettlement(user1, user2, 500)
        )

        coEvery { cashpoolSettlementService.calculateSettlements(1) } returns settlements

        val client = createClient()
        val response = client.get("/cashpools/1/settle") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolSettlementResponse>>()
        assertEquals(1, body.size)
        assertEquals(500, body[0].amountCents)
        assertEquals(user1.id, body[0].from.id)
    }

    @Test
    fun `get settlements - not found`() = withTestApplication(createMockPrincipal(1)) {
        coEvery { cashpoolSettlementService.calculateSettlements(1) } throws core.exceptions.CashpoolNotFound()

        val client = createClient()
        val response = client.get("/cashpools/1/settle") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

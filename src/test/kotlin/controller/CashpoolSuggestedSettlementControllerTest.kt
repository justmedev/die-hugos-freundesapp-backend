package controller

import domain.models.CashpoolSuggestedSettlement
import dto.cashpool_settlement.CashpoolSuggestedSettlementResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals

class CashpoolSuggestedSettlementControllerTest : BaseControllerTest() {
    private val user1 = Users.nonAdminUser
    private val user2 = Users.nonAdminUser

    @Test
    fun `get settlements - success`() = withTestApplication(createMockPrincipal(1)) {
        val settlements = listOf(
            CashpoolSuggestedSettlement(user1, user2, 500)
        )

        coEvery { cashpoolSuggestedSettlementCalculationService.calculateSettlements(1, 1) } returns settlements

        val client = createClient()
        val response = client.get("/cashpools/1/settle/suggest") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolSuggestedSettlementResponse>>()
        assertEquals(1, body.size)
        assertEquals(500, body[0].amountCents)
        assertEquals(user1.id, body[0].from.id)
    }

    @Test
    fun `get settlements - not found`() = withTestApplication(createMockPrincipal(1)) {
        coEvery { cashpoolSuggestedSettlementCalculationService.calculateSettlements(1, 1) } throws core.exceptions.CashpoolNotFound()

        val client = createClient()
        val response = client.get("/cashpools/1/settle/suggest") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

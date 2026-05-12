package controller

import domain.models.CashpoolTransaction
import domain.models.User
import dto.cashpool_transaction.CashpoolTransactionResponse
import dto.cashpool_transaction.CreateCashpoolTransactionRequest
import dto.cashpool_transaction.UpdateCashpoolTransactionRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolTransactionControllerTest : BaseControllerTest() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val user = User(1, "test@example.com", "Test", "User", null, null, "pass", now, false, now)

    @Test
    fun `post transaction - success`() = withTestApplication(createMockPrincipal(1)) {
        val request = CreateCashpoolTransactionRequest("Label", 1000)
        val created = CashpoolTransaction(1, user, request.label, request.amountCents, now)

        coEvery { cashpoolTransactionService.create(any()) } returns created

        val client = createClient()
        val response = client.post("/cashpools/1/transactions") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<CashpoolTransactionResponse>()
        assertEquals(request.label, body.label)
    }

    @Test
    fun `get transactions - success`() = withTestApplication(createMockPrincipal(1)) {
        val transactions = listOf(
            CashpoolTransaction(1, user, "Label 1", 1000, now),
            CashpoolTransaction(2, user, "Label 2", 2000, now)
        )

        coEvery { cashpoolTransactionService.findByCashpoolId(1) } returns transactions

        val client = createClient()
        val response = client.get("/cashpools/1/transactions") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolTransactionResponse>>()
        assertEquals(2, body.size)
    }

    @Test
    fun `put transaction - success`() = withTestApplication(createMockPrincipal(1)) {
        val request = UpdateCashpoolTransactionRequest("Updated Label", 2000)
        val updated = CashpoolTransaction(1, user, request.label, request.amountCents, now)

        coEvery { cashpoolTransactionService.update(any()) } returns updated

        val client = createClient()
        val response = client.put("/cashpools/1/transactions/1") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CashpoolTransactionResponse>()
        assertEquals(request.label, body.label)
        assertEquals(request.amountCents, body.amountCents)
    }

    @Test
    fun `delete transaction - success`() = withTestApplication(createMockPrincipal(1)) {
        val client = createClient()
        val response = client.delete("/cashpools/1/transactions/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `delete transaction - forbidden if not member`() = withTestApplication(createMockPrincipal(1)) {
        coEvery { cashpoolService.findByIdOnlyIfMember(1, 1) } returns null

        val client = createClient()
        val response = client.delete("/cashpools/1/transactions/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}

package controller

import domain.models.CashpoolTransaction
import domain.models.events.CashpoolTransactionEvent
import dto.cashpool_transaction.CashpoolTransactionResponse
import dto.cashpool_transaction.CreateCashpoolTransactionRequest
import dto.cashpool_transaction.UpdateCashpoolTransactionRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.junit.Test
import testutils.Users
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class CashpoolTransactionControllerTest : BaseControllerTest() {
    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val user = Users.nonAdminUser

    @Test
    fun `sse transactions - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val eventsFlow = MutableSharedFlow<CashpoolTransactionEvent>()
        coEvery { cashpoolTransactionService.events } returns eventsFlow
        coEvery { cashpoolService.requireMembership(1, 1) } returns Unit

        val client = createClient {
            install(SSE)
        }

        client.sse("/cashpools/1/transactions/listen", request = {
            header(HttpHeaders.Authorization, "Bearer test")
        }) {
            val tx = CashpoolTransaction(1, user, "Label", 1000, now)
            launch {
                delay(200.milliseconds) // Wait for connection and hello
                eventsFlow.emit(CashpoolTransactionEvent.Created(1, tx))
            }

            val events = incoming.take(2).toList()

            val hello = events[0]
            assertEquals("hello", hello.event)
            assertEquals("Connected to SSE endpoint", hello.data)

            val created = events[1]
            assertEquals("created", created.event)
            val body = Json.decodeFromString<CashpoolTransactionResponse>(created.data ?: "")
            assertEquals("Label", body.label)
        }
    }

    @Test
    fun `post transaction - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
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
    fun `get transactions - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val transactions = listOf(
            CashpoolTransaction(1, user, "Label 1", 1000, now),
            CashpoolTransaction(2, user, "Label 2", 2000, now)
        )

        coEvery { cashpoolTransactionService.findByCashpoolId(1, 1) } returns transactions

        val client = createClient()
        val response = client.get("/cashpools/1/transactions") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<CashpoolTransactionResponse>>()
        assertEquals(2, body.size)
    }

    @Test
    fun `put transaction - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
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
    fun `delete transaction - success`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        val client = createClient()
        val response = client.delete("/cashpools/1/transactions/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `delete transaction - forbidden if not member`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        coEvery { cashpoolTransactionService.deleteById(1, 1, 1) } throws core.exceptions.NotaCashpoolMember()

        val client = createClient()
        val response = client.delete("/cashpools/1/transactions/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `delete transaction - not found`() = withTestApplication(createMockPrincipal(Users.nonAdminUser)) {
        coEvery { cashpoolTransactionService.deleteById(1, 1, 1) } throws core.exceptions.TransactionNotFound()

        val client = createClient()
        val response = client.delete("/cashpools/1/transactions/1") {
            header(HttpHeaders.Authorization, "Bearer test")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

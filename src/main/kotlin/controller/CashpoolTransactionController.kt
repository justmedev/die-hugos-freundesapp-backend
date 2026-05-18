package controller

import controller.resources.CashpoolResource
import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.Unauthorized
import core.extensions.requireUserId
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.models.events.CashpoolTransactionEvent
import dto.cashpool_transaction.CashpoolTransactionDeletedEventResponse
import dto.cashpool_transaction.CashpoolTransactionResponse
import dto.cashpool_transaction.CreateCashpoolTransactionRequest
import dto.cashpool_transaction.UpdateCashpoolTransactionRequest
import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.github.smiley4.ktoropenapi.resources.put
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.Json
import service.cashpool.CashpoolService
import service.cashpool_transaction.CashpoolTransactionService

fun Application.configureCashpoolTransactionsController() {
    val tag = "Cashpool Transaction"
    val cashpoolTransactionService: CashpoolTransactionService by dependencies
    val cashpoolService: CashpoolService by dependencies

    routing {
        authenticate {
            post<CashpoolResource.Id.Transactions>({
                description = "Create a new cashpool transaction inside of a cashpool."
                tags = listOf(tag)
                request { body<CreateCashpoolTransactionRequest>() }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Transaction successfully created"
                        body<CashpoolTransactionResponse>()
                    }
                    code(HttpStatusCode.BadRequest) { description = "Invalid request data" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val createRequest = call.receive<CreateCashpoolTransactionRequest>()
                val created = cashpoolTransactionService.create(
                    CreateCashpoolTransactionCommand(
                        call.requireUserId(),
                        resource.parent.id,
                        createRequest.label,
                        createRequest.amountCents
                    )
                )

                call.respond(HttpStatusCode.Created, CashpoolTransactionResponse.from(created))
            }

            get<CashpoolResource.Id.Transactions>({
                description = "Get all cashpool transactions inside a cashpool."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) {
                        description = "List of transactions"
                        body<List<CashpoolTransactionResponse>>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val transactions = cashpoolTransactionService.findByCashpoolId(
                    cashpoolId = resource.parent.id,
                    requestingUserId = call.requireUserId()
                )

                call.respond(HttpStatusCode.OK, transactions.map { CashpoolTransactionResponse.from(it) })
            }

            put<CashpoolResource.Id.Transactions.Transaction>({
                description = "Edit an existing cashpool transaction by id."
                tags = listOf(tag)
                request { body<UpdateCashpoolTransactionRequest>() }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Transaction successfully updated"
                        body<CashpoolTransactionResponse>()
                    }
                    code(HttpStatusCode.BadRequest) { description = "Invalid request data" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool or does not own the transaction" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool or transaction not found" }
                }
            }) { resource ->
                val updateRequest = call.receive<UpdateCashpoolTransactionRequest>()
                val updated = cashpoolTransactionService.update(
                    UpdateCashpoolTransactionCommand(
                        call.requireUserId(),
                        resource.parent.parent.id,
                        resource.transactionId,
                        updateRequest.label,
                        updateRequest.amountCents
                    )
                )

                call.respond(HttpStatusCode.OK, CashpoolTransactionResponse.from(updated))
            }

            delete<CashpoolResource.Id.Transactions.Transaction>({
                description = "Delete a specific cashpool transaction by id."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.NoContent) { description = "Transaction successfully deleted" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool or does not own the transaction" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool or transaction not found" }
                }
            }) { resource ->
                cashpoolTransactionService.deleteById(
                    cashpoolId = resource.parent.parent.id,
                    requestingUserId = call.requireUserId(),
                    transactionId = resource.transactionId
                )

                call.respond(HttpStatusCode.NoContent)
            }

            route({
                description = "SSE endpoint for real-time transaction updates."
                tags = listOf(tag)
            }) {
                sse("/cashpools/{id}/transactions/listen") {
                    try {
                        val cashpoolId =
                            call.parameters["id"]?.toIntOrNull() ?: throw CashpoolNotFound()
                        val userId = call.requireUserId()
                        cashpoolService.requireMembership(cashpoolId, userId)

                        println("A user (id: $userId) connected to the SSE endpoint for cashpool id: $cashpoolId")
                        send("Connected to SSE endpoint", "hello")

                        cashpoolTransactionService.events
                            .filter { it.cashpoolId == cashpoolId }
                            .collect { event ->
                                when (event) {
                                    is CashpoolTransactionEvent.Created -> {
                                        val dto = CashpoolTransactionResponse.from(event.transaction)
                                        send(ServerSentEvent(data = Json.encodeToString(dto), event = "created"))
                                    }

                                    is CashpoolTransactionEvent.Updated -> {
                                        val dto = CashpoolTransactionResponse.from(event.transaction)
                                        send(ServerSentEvent(data = Json.encodeToString(dto), event = "updated"))
                                    }

                                    is CashpoolTransactionEvent.Deleted -> {
                                        send(
                                            ServerSentEvent(
                                                data = Json.encodeToString(
                                                    CashpoolTransactionDeletedEventResponse(event.transactionId)
                                                ), event = "deleted"
                                            )
                                        )
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        when (e) {
                            is Unauthorized -> {
                                send("User not authorized", "error")
                            }
                            is CashpoolNotFound -> {
                                send("Cashpool not found", "error")
                            }
                            is NotaCashpoolMember -> {
                                send("Not a member of this cashpool", "error")
                            }
                            else -> {
                                e.printStackTrace()
                                send("An unknown error occurred", "error")
                            }
                        }
                        close()
                        return@sse
                    }
                }
            }
        }
    }
}

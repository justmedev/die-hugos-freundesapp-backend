package controller

import dto.cashpool_transaction.CashpoolTransactionResponse
import dto.cashpool_transaction.CreateCashpoolTransactionRequest
import dto.cashpool_transaction.UpdateCashpoolTransactionRequest
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_transaction.CashpoolTransactionService
import service.cashpool_transaction.CreateCashpoolTransactionCommand
import service.cashpool_transaction.UpdateCashpoolTransactionCommand

fun Application.configureCashpoolTransactionsController() {
    val tag = "Cashpool Transaction"
    val cashpoolTransactionService: CashpoolTransactionService by dependencies

    routing {
        authenticate {
            route("/cashpools/{id}/transactions") {
                post({
                    description = "Create a new cashpool transaction inside of a cashpool."
                    tags = listOf(tag)
                    request { body<CreateCashpoolTransactionRequest>() }
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolTransactionResponse>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.Forbidden)

                    val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    val createRequest = call.receive<CreateCashpoolTransactionRequest>()
                    val created = cashpoolTransactionService.create(
                        CreateCashpoolTransactionCommand(
                            userId,
                            id,
                            createRequest.label,
                            createRequest.amountCents
                        )
                    )

                    call.respond(HttpStatusCode.Created, CashpoolTransactionResponse.from(created))
                }

                put("/{transactionId}", {
                    description = "Edit an existing cashpool transaction by id."
                    tags = listOf(tag)
                    request { body<CreateCashpoolTransactionRequest>() }
                    response {
                        code(HttpStatusCode.OK) { body<UpdateCashpoolTransactionRequest>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.Forbidden)

                    val cashpoolId = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    val transactionId = call.parameters["transactionId"]?.toIntOrNull() ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    val updateRequest = call.receive<UpdateCashpoolTransactionRequest>()
                    val updated = cashpoolTransactionService.update(
                        UpdateCashpoolTransactionCommand(
                            userId,
                            cashpoolId,
                            transactionId,
                            updateRequest.label,
                            updateRequest.amountCents
                        )
                    )

                    call.respond(HttpStatusCode.OK, CashpoolTransactionResponse.from(updated))
                }

                get({
                    description = "Get all cashpool transactions inside a cashpool."
                    tags = listOf(tag)
                    response {
                        code(HttpStatusCode.OK) { body<List<CashpoolTransactionResponse>>() }
                    }
                }) {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    call.respond(
                        HttpStatusCode.OK,
                        cashpoolTransactionService.findByCashpoolId(id).map { CashpoolTransactionResponse.from(it!!) })
                }
            }
        }
    }
}

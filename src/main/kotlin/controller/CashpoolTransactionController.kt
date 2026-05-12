package controller

import controller.resources.CashpoolsResource
import core.extensions.requireUserId
import dto.cashpool_transaction.CashpoolTransactionResponse
import dto.cashpool_transaction.CreateCashpoolTransactionRequest
import dto.cashpool_transaction.UpdateCashpoolTransactionRequest
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_transaction.CashpoolTransactionService
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand

fun Application.configureCashpoolTransactionsController() {
    val tag = "Cashpool Transaction"
    val cashpoolTransactionService: CashpoolTransactionService by dependencies

    routing {
        authenticate {
            route("/cashpools/{id}/transactions") {
                post<CashpoolsResource.Id.Transactions>({
                    description = "Create a new cashpool transaction inside of a cashpool."
                    tags = listOf(tag)
                    request { body<CreateCashpoolTransactionRequest>() }
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolTransactionResponse>() }
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

                get<CashpoolsResource.Id.Transactions>({
                    description = "Get all cashpool transactions inside a cashpool."
                    tags = listOf(tag)
                    response {
                        code(HttpStatusCode.OK) { body<List<CashpoolTransactionResponse>>() }
                        code(HttpStatusCode.Forbidden) { description = "You are not a member of this cashpool" }
                    }
                }) { resource ->
                    val transactions = cashpoolTransactionService.findByCashpoolId(
                        cashpoolId = resource.parent.id,
                        requestingUserId = call.requireUserId()
                    )

                    call.respond(HttpStatusCode.OK, transactions.map { CashpoolTransactionResponse.from(it) })
                }

                put<CashpoolsResource.Id.Transactions.Transaction>({
                    description = "Edit an existing cashpool transaction by id."
                    tags = listOf(tag)
                    request { body<UpdateCashpoolTransactionRequest>() }
                    response {
                        code(HttpStatusCode.OK) { body<CashpoolTransactionResponse>() }
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

                delete<CashpoolsResource.Id.Transactions.Transaction>({
                    description = "Delete a specific cashpool transaction by id."
                    tags = listOf(tag)
                    response {
                        code(HttpStatusCode.NoContent) {}
                        code(HttpStatusCode.Forbidden) { description = "You are not a member of this cashpool" }
                    }
                }) { resource ->
                    cashpoolTransactionService.deleteById(
                        cashpoolId = resource.parent.parent.id,
                        requestingUserId = call.requireUserId(),
                        transactionId = resource.transactionId
                    )

                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

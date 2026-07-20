package controller

import controller.resources.CashpoolResource
import core.extensions.requireUserId
import domain.commands.CreateCashpoolSettlementCommand
import dto.cashpool_settlement.CashpoolSettlementResponse
import dto.cashpool_settlement.CashpoolSuggestedSettlementResponse
import dto.cashpool_settlement.CreateCashpoolSettlementRequest
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_settlement.CashpoolSettlementService

fun Application.configureCashpoolSettlementController() {
    val tag = "Cashpool Settlement"
    val cashpoolSettlementService: CashpoolSettlementService by dependencies

    routing {
        authenticate {
            post<CashpoolResource.CashpoolId.Settle>({
                description = "Create a new cashpool settlement transaction."
                tags = listOf(tag)
                request { body<CreateCashpoolSettlementRequest>() }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Settlement successfully created"
                        body<CashpoolSettlementResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val createRequest = call.receive<CreateCashpoolSettlementRequest>()
                val created = cashpoolSettlementService.create(
                    CreateCashpoolSettlementCommand(
                        createRequest.fromId,
                        createRequest.toId,
                        resource.parent.cashpoolId,
                        createRequest.purpose,
                        createRequest.amountCents
                    )
                )
                call.respond(HttpStatusCode.Created, CashpoolSettlementResponse.from(created))
            }

            get<CashpoolResource.CashpoolId.Settle>({
                description =
                    "Get a list of settlement transactions by cashpool id that have already been made. This is not the same as 'suggested settlements'!"
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) {
                        description = "List of settlements"
                        body<List<CashpoolSuggestedSettlementResponse>>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                call.respond(
                    HttpStatusCode.OK,
                    cashpoolSettlementService.findByCashpoolId(resource.parent.cashpoolId, call.requireUserId()).map {
                        CashpoolSettlementResponse.from(it)
                    })
            }
        }
    }
}

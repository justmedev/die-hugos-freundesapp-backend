package controller

import core.extensions.requireUserId
import controller.resources.CashpoolResource
import dto.cashpool_settlement.CashpoolSuggestedSettlementResponse
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_settlement.CashpoolSuggestedSettlementCalculationService

fun Application.configureCashpoolSuggestedSettlementController() {
    val tag = "Cashpool Suggested Settlement"
    val cashpoolSuggestedSettlementCalculationService: CashpoolSuggestedSettlementCalculationService by dependencies

    routing {
        authenticate {
            get<CashpoolResource.Id.Settle.Suggest>({
                description =
                    "Get a list of settlement transactions that need to be made by each member to even out the cashpool (referred to as 'suggested settlements')."
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
                    cashpoolSuggestedSettlementCalculationService.calculateSettlements(resource.parent.parent.id, call.requireUserId()).map {
                        CashpoolSuggestedSettlementResponse.from(
                            it
                        )
                    })
            }
        }
    }
}

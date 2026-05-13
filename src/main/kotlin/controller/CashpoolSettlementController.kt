package controller

import controller.resources.CashpoolResource
import dto.cashpool_settlement.CashpoolSettlementResponse
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_settlement.CashpoolSettlementService

fun Application.configureCashpoolSettlementController() {
    val tag = "Cashpool Settlement"
    val cashpoolSettlementService: CashpoolSettlementService by dependencies

    routing {
        authenticate {
            get<CashpoolResource.Id.Settle>({
                description =
                    "Get a list of settlement transactions that need to be made by each member to even out the cashpool."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) { body<List<CashpoolSettlementResponse>>() }
                }
            }) { resource ->
                call.respond(
                    HttpStatusCode.OK,
                    cashpoolSettlementService.calculateSettlements(resource.parent.id).map {
                        CashpoolSettlementResponse.from(
                            it
                        )
                    })
            }
        }
    }
}

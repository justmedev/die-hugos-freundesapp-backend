package controller

import dto.cashpool_settlement.CashpoolSettlementResponse
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_settlement.CashpoolSettlementService

fun Application.configureCashpoolSettlementController() {
    val cashpoolSettlementService: CashpoolSettlementService by dependencies

    routing {
        authenticate {
            route("/cashpools/{cashpoolId}/settle") {
                get({
                    description =
                        "Get a list of settlement transactions that need to be made by each member to even out the cashpool."
                    tags = listOf("Cashpool")
                    response {
                        code(HttpStatusCode.OK) { body<List<CashpoolSettlementResponse>>() }
                    }
                }) {
                    val cashpoolId = call.parameters["cashpoolId"]?.toIntOrNull() ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    call.respond(
                        HttpStatusCode.OK,
                        cashpoolSettlementService.calculateSettlements(cashpoolId).map {
                            CashpoolSettlementResponse.from(
                                it
                            )
                        })
                }
            }
        }
    }
}

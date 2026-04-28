package controller

import domain.models.User
import dto.cashpool.CashpoolResponse
import dto.cashpool.CreateCashpoolRequest
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpools.CashpoolsService
import service.cashpools.CreateCashpoolCommand

fun Application.configureCashpoolController() {
    val cashpoolService: CashpoolsService by dependencies

    routing {
        authenticate {
            route("/cashpools") {
                post("/", {
                    description = "Create a new cashpool. You will still have to join (as a member), even as the owner."
                    tags = listOf("Cashpool")
                    request { body<CreateCashpoolRequest>() }
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolResponse>() }
                    }
                }) {
                    val user = call.principal<User>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val createRequest = call.receive<CreateCashpoolRequest>()
                    val created = cashpoolService.create(
                        CreateCashpoolCommand(
                            createRequest.title,
                            createRequest.description,
                            user.id
                        )
                    )
                    call.respond(HttpStatusCode.Created, CashpoolResponse.from(created))
                }
            }
        }
    }
}

package controller

import core.exceptions.NotaCashpoolMember
import dto.cashpool.CashpoolResponse
import dto.cashpool.CreateCashpoolRequest
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
                post({
                    description = "Create a new cashpool. You will still have to join (as a member), even as the owner."
                    tags = listOf("Cashpool")
                    request { body<CreateCashpoolRequest>() }
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolResponse>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.Forbidden)

                    val createRequest = call.receive<CreateCashpoolRequest>()
                    val created = cashpoolService.create(
                        CreateCashpoolCommand(
                            createRequest.title,
                            createRequest.description,
                            userId
                        )
                    )
                    call.respond(HttpStatusCode.Created, CashpoolResponse.from(created))
                }

                get({
                    description = "Get all cashpools."
                    tags = listOf("Cashpool")
                    response {
                        code(HttpStatusCode.OK) { body<List<CashpoolResponse>>() }
                    }
                }) {
                    call.respond(HttpStatusCode.OK, cashpoolService.findAll().map { CashpoolResponse.from(it!!) })
                }

                get("/{id}", {
                    description = "Get a specific cashpool. This only returns cashpools the user is a member of."
                    tags = listOf("Cashpool")
                    response {
                        code(HttpStatusCode.OK) { body<CashpoolResponse>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.Forbidden)
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    try {
                        val domain = cashpoolService.findByIdOnlyIfMember(id, userId) ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            "Cashpool not found"
                        )
                        call.respond(HttpStatusCode.OK, CashpoolResponse.from(domain))
                    } catch (_: NotaCashpoolMember) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            "You are not a member of this cashpool"
                        )
                    }
                }
            }
        }
    }
}

package controller

import dto.cashpool_member.CashpoolMemberResponse
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_member.CashpoolMemberService
import domain.commands.CreateCashpoolMemberCommand

fun Application.configureCashpoolMemberController() {
    val tag = "Cashpool Membership"
    val cashpoolMemberService: CashpoolMemberService by dependencies

    routing {
        authenticate {
            route("/cashpools/{id}/members") {
                post({
                    description = "Join an existing cashpool."
                    tags = listOf(tag)
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolMemberResponse>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.Forbidden)

                    val cashpoolId = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid id"
                    )

                    val created = cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

                    call.respond(HttpStatusCode.Created, CashpoolMemberResponse.from(created))
                }
            }
        }
    }
}

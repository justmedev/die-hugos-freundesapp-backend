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
import service.cashpool_member.CreateCashpoolMemberCommand

fun Application.configureCashpoolMemberController() {
    val cashpoolMemberService: CashpoolMemberService by dependencies

    routing {
        authenticate {
            route("/cashpools/{cashpoolId}/members") {
                post({
                    description = "Join an existing cashpool."
                    tags = listOf("Cashpool Memberships")
                    response {
                        code(HttpStatusCode.Created) { body<CashpoolMemberResponse>() }
                    }
                }) {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.Forbidden)

                    val cashpoolId = call.parameters["cashpoolId"]?.toIntOrNull() ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid cashpoolId"
                    )

                    val created = cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

                    call.respond(HttpStatusCode.Created, CashpoolMemberResponse.from(created))
                }
            }
        }
    }
}

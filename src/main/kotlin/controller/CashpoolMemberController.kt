package controller

import controller.resources.CashpoolResource
import core.extensions.requireUserId
import domain.commands.CreateCashpoolMemberCommand
import dto.cashpool_member.CashpoolMemberResponse
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool_member.CashpoolMemberService

fun Application.configureCashpoolMemberController() {
    val tag = "Cashpool Membership"
    val cashpoolMemberService: CashpoolMemberService by dependencies

    routing {
        authenticate {
            post<CashpoolResource.Id.Members>({
                description = "Join an existing cashpool by its id."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.Created) {
                        description = "Successfully joined the cashpool"
                        body<CashpoolMemberResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User cannot join this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val created =
                    cashpoolMemberService.create(CreateCashpoolMemberCommand(call.requireUserId(), resource.parent.id))

                call.respond(HttpStatusCode.Created, CashpoolMemberResponse.from(created))
            }
        }
    }
}

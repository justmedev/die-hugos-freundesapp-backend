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
                description = "Join an existing cashpool."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.Created) { body<CashpoolMemberResponse>() }
                }
            }) { resource ->
                val created =
                    cashpoolMemberService.create(CreateCashpoolMemberCommand(call.requireUserId(), resource.parent.id))

                call.respond(HttpStatusCode.Created, CashpoolMemberResponse.from(created))
            }
        }
    }
}

package controller

import controller.resources.CashpoolResource
import core.extensions.requireUserId
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import dto.cashpool.CashpoolResponse
import dto.cashpool.CreateCashpoolRequest
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService

fun Application.configureCashpoolController() {
    val tag = "Cashpool"
    val cashpoolService: CashpoolService by dependencies
    val cashpoolMemberService: CashpoolMemberService by dependencies

    routing {
        authenticate {
            post<CashpoolResource>({
                description = "Create a new cashpool. The creator will automatically be a member of the cashpool."
                tags = listOf(tag)
                request { body<CreateCashpoolRequest>() }
                response {
                    code(HttpStatusCode.Created) { body<CashpoolResponse>() }
                }
            }) {
                val createRequest = call.receive<CreateCashpoolRequest>()
                val created = cashpoolService.create(
                    CreateCashpoolCommand(
                        createRequest.title,
                        createRequest.description,
                        call.requireUserId()
                    )
                )
                cashpoolMemberService.create(CreateCashpoolMemberCommand(call.requireUserId(), created.id))

                call.respond(HttpStatusCode.Created, CashpoolResponse.from(created))
            }

            get<CashpoolResource>({
                description = "Get all cashpools."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) { body<List<CashpoolResponse>>() }
                }
            }) {
                call.respond(HttpStatusCode.OK, cashpoolService.findAll().map { CashpoolResponse.from(it) })
            }

            get<CashpoolResource.Id>({
                description = "Get a specific cashpool. This only returns cashpools the user is a member of."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) { body<CashpoolResponse>() }
                }
            }) { resource ->
                val domain = cashpoolService.findByIdOnlyIfMember(resource.id, call.requireUserId())
                call.respond(HttpStatusCode.OK, CashpoolResponse.from(domain))
            }
        }
    }
}

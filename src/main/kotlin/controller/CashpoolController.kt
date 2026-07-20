package controller

import controller.resources.CashpoolResource
import core.extensions.requireUserId
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.UpdateCashpoolCommand
import dto.cashpool.CashpoolResponse
import dto.cashpool.CreateCashpoolRequest
import dto.cashpool.UpdateCashpoolRequest
import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.github.smiley4.ktoropenapi.resources.put
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
                    code(HttpStatusCode.Created) {
                        description = "Cashpool successfully created"
                        body<CashpoolResponse>()
                    }
                    code(HttpStatusCode.BadRequest) { description = "Invalid request data" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
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
                description = "Get all cashpools the authenticated user is a member of."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) {
                        description = "List of cashpools"
                        body<List<CashpoolResponse>>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                }
            }) {
                call.respond(HttpStatusCode.OK, cashpoolService.findAll().map { CashpoolResponse.from(it) })
            }

            get<CashpoolResource.CashpoolId>({
                description = "Get a specific cashpool. This only returns cashpools the user is a member of."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) {
                        description = "The requested cashpool"
                        body<CashpoolResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not a member of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val domain = cashpoolService.findByIdOnlyIfMember(resource.cashpoolId, call.requireUserId())
                call.respond(HttpStatusCode.OK, CashpoolResponse.from(domain))
            }

            put<CashpoolResource.CashpoolId>({
                description = "Update a specific cashpool. Only the creator of the cashpool can update it."
                tags = listOf(tag)
                request { body<UpdateCashpoolRequest>() }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Cashpool successfully updated"
                        body<CashpoolResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not the creator of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                val updateRequest = call.receive<UpdateCashpoolRequest>()
                val updated = cashpoolService.update(
                    call.requireUserId(),
                    UpdateCashpoolCommand(resource.cashpoolId, updateRequest.title, updateRequest.description)
                )
                call.respond(HttpStatusCode.OK, CashpoolResponse.from(updated))
            }

            delete<CashpoolResource.CashpoolId>({
                description = "Delete a specific cashpool. Only the creator of the cashpool can delete it."
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.NoContent) { description = "Cashpool successfully deleted" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "User is not the creator of this cashpool" }
                    code(HttpStatusCode.NotFound) { description = "Cashpool not found" }
                }
            }) { resource ->
                call.respond(HttpStatusCode.NoContent, cashpoolService.deleteById(resource.cashpoolId, call.requireUserId()))
            }
        }
    }
}

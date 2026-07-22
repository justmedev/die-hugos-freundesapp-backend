package controller

import controller.resources.UserResource
import core.extensions.requireUser
import domain.commands.UpdateUserCommand
import dto.user.ExternalUpdateUserRequest
import dto.user.InternalUpdateUserRequest
import dto.user.UserResponse
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.patch
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.user.UserService

fun Application.configureUserController() {
    val tag = "User"
    val userService: UserService by dependencies

    routing {
        authenticate {
            get<UserResource.Me>({
                description = "Get the authenticated user"
                tags = listOf(tag)
                response {
                    code(HttpStatusCode.OK) {
                        description = "The authenticated user"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                }
            }) {
                val user = call.requireUser(userService)
                call.respond(HttpStatusCode.OK, UserResponse.from(user))
            }

            patch<UserResource.Me>({
                description =
                    "Update the authenticated user. A partial UpdateUserRequest object is allowed and will only update the fields that are included."
                tags = listOf(tag)
                request { body<ExternalUpdateUserRequest>() }
                response {
                    code(HttpStatusCode.OK) {
                        description = "The updated user"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                }
            }) {
                // TODO: Update first and last name + email through keycloak
                val user = call.requireUser(userService)
                val updateRequest = call.receive<InternalUpdateUserRequest>()

                call.respond(
                    HttpStatusCode.OK, UserResponse.from(
                        userService.update(
                            user.id, UpdateUserCommand(
                                accountHolderName = updateRequest.accountHolderName,
                                accountIBAN = updateRequest.accountIBAN,
                                birthdate = updateRequest.birthdate,
                            )
                        )
                    )
                )
            }
        }
    }
}

package controller

import controller.resources.UserResource
import core.extensions.requireUserId
import core.extensions.requireUserRole
import domain.commands.RegisterCommand
import domain.commands.UpdateUserCommand
import dto.user.CreateUserRequest
import dto.user.UpdateUserRequest
import dto.user.UserResponse
import io.github.smiley4.ktoropenapi.resources.post
import io.github.smiley4.ktoropenapi.resources.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.auth.AuthService
import service.user.UserService

fun Application.configureUserController() {
    val tag = "User"
    val authService: AuthService by dependencies
    val userService: UserService by dependencies

    routing {
        authenticate {
            post<UserResource>({
                description = "Create a new user. Only accessible by admins."
                tags = listOf(tag)
                request { body<CreateUserRequest>() }
                response {
                    code(HttpStatusCode.Created) {
                        description = "User successfully created"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.BadRequest) { description = "Invalid request data" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.Forbidden) { description = "Only admin accounts can create users" }
                    code(HttpStatusCode.UnprocessableEntity) { description = "User with this email already exists" }
                }
            }) {
                if (call.requireUserRole() != "admin") return@post call.respond(HttpStatusCode.Forbidden)

                val createUserRequest = call.receive<CreateUserRequest>()
                val entity = authService.register(
                    RegisterCommand(
                        createUserRequest.email,
                        createUserRequest.firstName,
                        createUserRequest.lastName,
                        null,
                        null,
                        createUserRequest.password,
                        createUserRequest.birthdate,
                        createUserRequest.isAdmin,
                    )
                )
                call.respond(HttpStatusCode.Created, UserResponse.from(entity))
            }

            put<UserResource>({
                description = "Update the authenticated user's profile information."
                tags = listOf(tag)
                request { body<UpdateUserRequest>() }
                response {
                    code(HttpStatusCode.OK) {
                        description = "User successfully updated"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.BadRequest) { description = "Invalid request data" }
                    code(HttpStatusCode.Unauthorized) { description = "Missing or invalid token" }
                    code(HttpStatusCode.NotFound) { description = "User not found" }
                }
            }) {
                val updateUserRequest = call.receive<UpdateUserRequest>()
                val updated = userService.update(
                    call.requireUserId(), UpdateUserCommand(
                        updateUserRequest.email,
                        updateUserRequest.firstName,
                        updateUserRequest.lastName,
                        updateUserRequest.accountHolderName,
                        updateUserRequest.accountIBAN,
                        updateUserRequest.birthdate,
                    )
                )
                call.respond(HttpStatusCode.OK, UserResponse.from(updated))
            }
        }
    }
}

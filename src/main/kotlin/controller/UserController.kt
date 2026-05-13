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
                description = "Create a new user."
                tags = listOf(tag)
                request { body<CreateUserRequest>() }
                response {
                    code(HttpStatusCode.OK) { body<UserResponse>() }
                    code(HttpStatusCode.Forbidden) { description = "Only admin accounts can create users" }
                }
            }) {
                if (call.requireUserRole() != "admin") return@post call.respond(HttpStatusCode.Forbidden)

                val createUserRequest = call.receive<CreateUserRequest>()
                try {
                    val entity = authService.register(
                        RegisterCommand(
                            createUserRequest.email,
                            createUserRequest.firstName,
                            createUserRequest.lastName,
                            null,
                            null,
                            createUserRequest.password,
                            createUserRequest.birthDate,
                            createUserRequest.isAdmin,
                        )
                    )
                    call.respond(HttpStatusCode.OK, UserResponse.from(entity))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            put<UserResource>({
                description = "Update an existing user."
                tags = listOf(tag)
                request { body<UpdateUserRequest>() }
                response {
                    code(HttpStatusCode.OK) { body<UserResponse>() }
                }
            }) {
                val updateUserRequest = call.receive<UpdateUserRequest>()
                userService.update(
                    call.requireUserId(), UpdateUserCommand(
                        updateUserRequest.email,
                        updateUserRequest.firstName,
                        updateUserRequest.lastName,
                        updateUserRequest.accountHolderName,
                        updateUserRequest.accountIBAN,
                        updateUserRequest.birthDate,
                    )
                )
            }
        }
    }
}

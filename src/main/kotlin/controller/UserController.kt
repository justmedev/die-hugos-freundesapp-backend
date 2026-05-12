package controller

import dto.user.CreateUserRequest
import dto.user.UserResponse
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.auth.AuthService
import domain.commands.RegisterCommand

fun Application.configureUserController() {
    val tag = "User"
    val authService: AuthService by dependencies

    routing {
        authenticate {
            route("/user") {
                post({
                    description = "Create a new user."
                    tags = listOf(tag)
                    request { body<CreateUserRequest>() }
                    response {
                        code(HttpStatusCode.OK) { body<UserResponse>() }
                        code(HttpStatusCode.Forbidden) { description = "Only admin accounts can create users" }
                    }
                }) {
                    val userRole = call.principal<JWTPrincipal>()?.payload?.claims?.get("role")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Forbidden)
                    if (userRole != "admin") return@post call.respond(HttpStatusCode.Forbidden)

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
            }
        }
    }
}

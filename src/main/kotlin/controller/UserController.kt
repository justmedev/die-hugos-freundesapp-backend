package controller

import controller.resources.UserResource
import core.extensions.requireUser
import dto.user.UserResponse
import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
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
            // TODO: create and update
            // We could either do this here or do it via keycloak and then just get the updated info via webhooks.
        }
    }
}

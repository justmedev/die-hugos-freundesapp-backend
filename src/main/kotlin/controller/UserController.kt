package controller

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import service.auth.AuthService
import service.user.UserService

fun Application.configureUserController() {
    val tag = "User"
    val authService: AuthService by dependencies
    val userService: UserService by dependencies

    routing {
        authenticate {
            // TODO: create and update
            // We could either do this here or do it via authentik and then just get the updated info via webhooks.
        }
    }
}

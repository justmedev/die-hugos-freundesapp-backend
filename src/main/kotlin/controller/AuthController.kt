package controller

import controller.resources.AuthResource
import domain.commands.LoginCommand
import domain.commands.RefreshCommand
import dto.auth.AuthResponse
import dto.auth.LoginRequest
import dto.auth.RefreshRequest
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.auth.AuthService

fun Application.configureAuthController() {
    val tag = "Auth"
    val authService: AuthService by dependencies

    routing {
        post<AuthResource.Login>({
            description = "Authenticate an existing user with email and password"
            tags = listOf(tag)
            request { body<LoginRequest>() }
            response {
                code(HttpStatusCode.OK) {
                    description = "Successfully authenticated"
                    body<AuthResponse>()
                }
                code(HttpStatusCode.BadRequest) { description = "Invalid request format or missing fields" }
                code(HttpStatusCode.Unauthorized) { description = "Wrong email or password" }
            }
        }) {
            val loginRequest = call.receive<LoginRequest>()
            val utp = authService.login(LoginCommand(loginRequest.email, loginRequest.password))
            call.respond(HttpStatusCode.OK, AuthResponse.from(utp, utp.user))
        }

        post<AuthResource.Refresh>({
            description = "Use a refresh token to get a new token pair."
            tags = listOf(tag)
            request { body<RefreshRequest>() }
            response {
                code(HttpStatusCode.OK) {
                    description = "Successfully refreshed tokens"
                    body<AuthResponse>()
                }
                code(HttpStatusCode.Unauthorized) { description = "Invalid refresh token" }
            }
        }) {
            val refreshRequest = call.receive<RefreshRequest>()
            val utp = authService.refresh(RefreshCommand(refreshRequest.refreshToken))
            call.respond(HttpStatusCode.OK, AuthResponse.from(utp, utp.user))
        }
    }
}

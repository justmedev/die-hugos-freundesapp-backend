package controller

import dto.auth.AuthResponse
import dto.auth.LoginRequest
import dto.auth.RefreshRequest
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.auth.AuthService
import service.auth.LoginCommand
import service.auth.RefreshCommand

fun Application.configureAuthController() {
    val tag = "Auth"
    val authService: AuthService by dependencies

    routing {
        route("/auth") {
            post("/login", {
                description = "Authenticate an existing user with email and password"
                tags = listOf(tag)
                request { body<LoginRequest>() }
                response {
                    code(HttpStatusCode.OK) { body<AuthResponse>() }
                    code(HttpStatusCode.Unauthorized) { description = "Wrong email or password" }
                }
            }) {
                val loginRequest = call.receive<LoginRequest>()
                try {
                    val utp = authService.login(LoginCommand(loginRequest.email, loginRequest.password))
                    call.respond(HttpStatusCode.OK, AuthResponse.from(utp, utp.user))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            post("/refresh", {
                description = "Use a refresh token to get a new token pair."
                tags = listOf(tag)
                request { body<RefreshRequest>() }
                response {
                    code(HttpStatusCode.OK) { body<AuthResponse>() }
                    code(HttpStatusCode.Unauthorized) { description = "Invalid refresh token" }
                }
            }) {
                val refreshRequest = call.receive<RefreshRequest>()
                try {
                    val utp = authService.refresh(RefreshCommand(refreshRequest.refreshToken))
                    call.respond(HttpStatusCode.OK, AuthResponse.from(utp, utp.user))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}

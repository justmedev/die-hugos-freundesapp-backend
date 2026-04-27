package controller

import dto.auth.AuthResponse
import dto.auth.LoginRequest
import dto.auth.RefreshRequest
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
    val authService: AuthService by dependencies

    routing {
        route("/auth") {
            post("/login") {
                val loginRequest = call.receive<LoginRequest>()
                try {
                    val utp = authService.login(LoginCommand(loginRequest.email, loginRequest.password))
                    call.respond(HttpStatusCode.OK, AuthResponse.from(utp, utp.user))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            post("/refresh") {
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

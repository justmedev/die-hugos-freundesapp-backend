package plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import service.auth.AuthService

fun Application.configureSecurity() {
    val logger = LoggerFactory.getLogger(this::class.java)

    val authService: AuthService by dependencies

    authentication {
        jwt {
            realm = authService.config.realm
            verifier(authService.jwkProvider, authService.config.issuerUri) {
                acceptLeeway(3)
            }

            validate { return@validate authService.validateCredential(it) }

            challenge { _, realm ->
                logger.warn("Token validation failed for realm: $realm")
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
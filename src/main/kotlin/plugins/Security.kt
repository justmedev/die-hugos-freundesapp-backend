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
    val authService: AuthService by dependencies

    val logger = LoggerFactory.getLogger(this::class.java)
    logger.info("Issuer URI: ${authService.config.issuerUri}")
    logger.info("JWKS URI: ${authService.config.jwksUri}")

    authentication {
        jwt {
            realm = "die-hugos"
            verifier(authService.jwkProvider, authService.config.issuerUri) {
                acceptLeeway(3)
                // withAudience(authService.config.clientId)
            }

            validate { authService.validateCredential(it) }

            challenge { defaultScheme, realm ->
                val abc = authService.jwkProvider.get("IfO50p2Jb9AuzXn8mb9R3EfObhbt0qJ1G_dTZpZQZ1c")
                logger.warn("Token validation failed for realm: $realm $abc")
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
package plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import service.auth.AuthService

fun Application.configureSecurity() {
    val authService: AuthService by dependencies

    authentication {
        jwt {
            verifier(authService.accessTokenVerifier)
            validate { credential ->
                val subject = credential.payload.subject
                return@validate if (subject?.toIntOrNull() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
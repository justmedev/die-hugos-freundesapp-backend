package service.auth

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import domain.models.AuthConfig
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.config.*
import service.user.UserService
import java.net.URI
import java.util.concurrent.TimeUnit

class AuthService(
    private val userService: UserService,
    appConfig: ApplicationConfig,
) {
    val config: AuthConfig = AuthConfig.fromAppConfig(appConfig)

    val jwkProvider: JwkProvider = JwkProviderBuilder((URI(config.jwksUri).toURL()))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    fun validateCredential(credential: JWTCredential): JWTPrincipal? {
        println("Validating credential: ${credential.payload.subject}")
        val keycloakId = credential.payload.subject
        return if (keycloakId != null) {
            JWTPrincipal(credential.payload)
        } else null
    }
}
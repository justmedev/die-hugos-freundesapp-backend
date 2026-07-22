package service.auth

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import core.exceptions.UserNotFound
import core.utils.UpdateProperty
import domain.commands.CreateUserCommand
import domain.commands.UpdateUserCommand
import domain.commands.UpdateUserNameAndEmailCommand
import domain.models.AuthConfig
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import kotlinx.datetime.LocalDate
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

    suspend fun validateCredential(credential: JWTCredential): JWTPrincipal? {
        val keycloakId = credential.payload.subject
        if (keycloakId != null) {
            val kcJWT = KeycloakJWT.from(credential)

            try {
                val user = userService.findByKeycloakId(keycloakId)
                userService.update(
                    user.id, UpdateUserCommand(
                        email = UpdateProperty(kcJWT.email, true),
                        firstName = UpdateProperty(kcJWT.firstName, true),
                        lastName = UpdateProperty(kcJWT.lastName, true),
                    )
                )
            } catch (_: UserNotFound) {
                userService.create(
                    CreateUserCommand(
                        keycloakId = kcJWT.keycloakId,
                        email = kcJWT.email,
                        firstName = kcJWT.firstName,
                        lastName = kcJWT.lastName,
                        birthdate = LocalDate(2000, 1, 1), // TODO: birthdate
                        isAdmin = false, // TODO: roles
                    )
                )
            }

            return JWTPrincipal(credential.payload)
        }

        return null
    }
}
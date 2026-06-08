package domain.models

import io.ktor.server.config.*

data class AuthConfig(
    val base: String,
    val slug: String,
) {
    val issuerUri get() = "$base/application/o/$slug/"
    val jwksUri get() = "$base/application/o/jwks/"

    companion object {
        fun fromAppConfig(config: ApplicationConfig) = AuthConfig(
            base = config.property("auth.base").getString(),
            slug = config.property("auth.slug").getString(),
        )
    }
}
